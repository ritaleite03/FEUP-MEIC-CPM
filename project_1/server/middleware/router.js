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
        // Decodifica a message de base64 para Buffer
        const buf = Buffer.from(message, "base64");

        let offset = 0;

        // UUID do user (16 bytes)
        const userId = [
            buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
            buf
                .readBigUInt64BE(offset + 8)
                .toString(16)
                .padStart(16, "0"),
        ].join("-");
        offset += 16;

        console.log(userId.toString());

        // Número de produtos (1 byte)
        const numProducts = buf.readUInt8(offset);
        offset += 1;

        console.log(numProducts.toString());

        // Produtos: UUID (16 bytes) + preço (2 bytes) por produto
        const products = [];
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
        }

        for (let product of products) {
            console.log(
                product.productId.toString(),
                product.priceInCents.toString()
            );
        }

        // useDiscount (1 byte)
        const useDiscount = buf.readUInt8(offset) === 1;
        offset += 1;

        console.log(useDiscount.toString());

        // voucherId (opcional, 16 bytes se presente)
        if (useDiscount === true) {
            console.log("1");
            let voucherId = null;
            if (buf.length - offset > 64) {
                // heurística: há 64 bytes de assinatura EC?
                voucherId = [
                    buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
                    buf
                        .readBigUInt64BE(offset + 8)
                        .toString(16)
                        .padStart(16, "0"),
                ].join("-");
                offset += 16;
            }
            console.log(voucherId.toString());
        }

        const messageParte = buf.slice(0, offset);

        const signature = buf.subarray(offset);
        console.log("2");

        const verified = await usersDB.verifyMessage(
            userId,
            signature,
            messageParte
        );

        console.log(verified);

        ctx.body = {
            userId,
            products,
            useDiscount,
            voucherId,
        };
    } catch (err) {
        ctx.status = 500;
        ctx.body = { error: "Failed to parse message", details: err.message };
    }
}

module.exports = router;
