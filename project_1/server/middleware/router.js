const usersDB = require("../database/dbops");
const crypto = require("crypto");
const KoaRouter = require("koa-router");

const router = new KoaRouter();

let supermarketPublicKey = null;

function encodePublicKeyToBase64(publicKey) {
    const publicKeyBytes = publicKey.export({ format: "der", type: "spki" });
    return publicKeyBytes.toString("base64");
}

function loadPublicKey(base64Key) {
    const buffer = Buffer.from(base64Key, "base64");
    return crypto.createPublicKey({
        key: buffer,
        format: "der",
        type: "spki",
    });
}

(async () => {
    try {
        await usersDB.init();
        console.log("Database initialized successfully");
        router
            .get("/", rootHello)
            .post("/pay", pay)
            .post("/key", addKey)
            .post("/users/get", getUser)
            .post("/users/add", addUser);
    } catch (error) {
        console.error("Error initializing database:", error);
    }
})();

async function rootHello(ctx) {
    console.log("in routed rootHello()");
    const res = await usersDB.dbTest(2);
    ctx.body = "Welcome to this server root API (test 2nd entry: " + res + ")";
}

async function addKey(ctx) {
    console.log("in addKey()");
    const { keyRSA } = ctx.request.body;
    supermarketPublicKey = loadPublicKey(keyRSA);
    ctx.body = {};
}

async function getUser(ctx) {
    console.log("in getUser()");
    const { username, usernick, password } = ctx.request.body;
    var result = await usersDB.getUser(username, usernick, password);
    if (Object.keys(result).length === 0 || "errno" in result) {
        result = {};
        ctx.status = 404; // Not found
    }
    ctx.body = result;
}

async function addUser(ctx) {
    // get keys
    const { keyEC, keyRSA } = ctx.request.body;
    ec_public_key = loadPublicKey(keyEC);
    rsa_public_key = loadPublicKey(keyRSA);

    const [uuid, result] = await usersDB.addNewUser(
        encodePublicKeyToBase64(ec_public_key),
        encodePublicKeyToBase64(rsa_public_key)
    );
    if ("errno" in result || result.lastID === 0) {
        ctx.status = 400; // Bad request
        ctx.body = {};
    } else {
        ctx.body = {
            Uuid: uuid,
            key: encodePublicKeyToBase64(supermarketPublicKey),
        };
    }
}

async function pay(ctx) {
    const { message } = ctx.request.body;

    if (!message) {
        ctx.status = 400;
        ctx.body = { error: "Message missing" };
        return;
    }

    try {
        const buf = Buffer.from(message, "base64");
        let offset = 0;

        let userId = [
            buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
            buf
                .readBigUInt64BE(offset + 8)
                .toString(16)
                .padStart(16, "0"),
        ].join("-");

        offset += 16;
        console.log("UserID -", userId);

        const numProducts = buf.readUInt8(offset);
        offset += 1;
        console.log("Number of Products -", numProducts);

        const products = [];
        console.log("Products");
        for (let i = 0; i < numProducts; i++) {
            const prodId = [
                buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
                buf
                    .readBigUInt64BE(offset + 8)
                    .toString(16)
                    .padStart(16, "0"),
            ].join("-");

            offset += 16;
            const price = buf.readInt16BE(offset);
            offset += 2;
            products.push({ productId: prodId, priceInCents: price });
            console.log("ProductId and Price -", prodId, price);
        }

        const useDiscount = buf.readUInt8(offset) === 1;
        offset += 1;
        console.log("Discount -", useDiscount);

        let voucherId = null;
        if (useDiscount === true) {
            voucherId = [
                buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
                buf
                    .readBigUInt64BE(offset + 8)
                    .toString(16)
                    .padStart(16, "0"),
            ].join("-");
            offset += 16;
            console.log("VoucherId -", voucherId);
        }

        const messageParte = buf.slice(0, offset);
        console.log("messageParte:", messageParte);

        const signature = buf.subarray(offset);
        console.log("signature:", signature);

        const verified = await usersDB.verifyMessage(
            userId,
            signature,
            messageParte
        );

        console.log("verified:", verified);

        if (verified === true) {
            ctx.body = {
                verified,
            };
            return;
        } else {
            ctx.status = 400;
            ctx.body = { error: "Invalid signature" };
            return;
        }
    } catch (err) {
        ctx.status = 500;
        ctx.body = { error: "Failed to parse message", details: err.message };
    }

    ctx.body = { error: "Failed to parse message" };
    return;
}

module.exports = router;
