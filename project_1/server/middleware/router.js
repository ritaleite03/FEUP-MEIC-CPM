const { error } = require("console");
const db = require("../database/dbops");
const crypto = require("crypto");
const KoaRouter = require("koa-router");

const router = new KoaRouter();

let supermarketPublicKey = null;

/**
 * Converts a crypto public key to a Base64-encoded string in DER format.
 * @param {crypto.KeyObject} publicKey
 * @returns {string}
 */
function encodePublicKeyToBase64(publicKey) {
    const publicKeyBytes = publicKey.export({ format: "der", type: "spki" });
    return publicKeyBytes.toString("base64");
}

/**
 * Loads a public key from a Base64-encoded string in DER format.
 * @param {string} base64Key
 * @returns {crypto.KeyObject}
 */
function loadPublicKey(base64Key) {
    const buffer = Buffer.from(base64Key, "base64");
    return crypto.createPublicKey({
        key: buffer,
        format: "der",
        type: "spki",
    });
}

// initialize the database and set up routes
(async () => {
    try {
        await db.init();
        router
            .post("/key", actionMarketKey)
            .post("/users/add", actionRegistration)
            .post("/pay", actionPayment)
            .post("/challenge/vouchers", actionChallengeVouchers)
            .post("/challenge/transaction", actionChallengeTransaction)
            .post("/vouchers", actionGetVouchers);
    } catch (error) {
        console.error("Error initializing database:", error);
    }
})();

/**
 * Receives and stores the supermarket's RSA public key.
 */
async function actionMarketKey(ctx) {
    console.log("in addKey()");
    const { keyRSA } = ctx.request.body;
    supermarketPublicKey = loadPublicKey(keyRSA);
    ctx.body = {};
}

/**
 * Adds a new user to the database with EC and RSA keys.
 */
async function actionRegistration(ctx) {
    console.log("\n---- Action Registration----\n");

    console.log("Loading and verifying the keys.");
    const { keyEC, keyRSA } = ctx.request.body;
    ec_public_key = loadPublicKey(keyEC);
    rsa_public_key = loadPublicKey(keyRSA);

    console.log("Perform registration.");
    const [uuid, result] = await db.actionRegistration(
        encodePublicKeyToBase64(ec_public_key),
        encodePublicKeyToBase64(rsa_public_key)
    );

    // check if it was a bad request
    if ("errno" in result || result.lastID === 0) {
        console.log("Error.");
        ctx.status = 400;
        ctx.body = {};
    } else {
        console.log("Success.");
        ctx.body = {
            Uuid: uuid,
            key: encodePublicKeyToBase64(supermarketPublicKey),
        };
    }

    console.log("\n----------------------------\n");
}

/**
 * Verifies a signed payment message.
 * Extracts user ID, products, and signature from the binary message.
 */
async function actionPayment(ctx) {
    console.log("\n---- Action Payment ----\n");

    const { message } = ctx.request.body;
    if (!message) {
        throw new Error("Message is missing.");
    }

    try {
        console.log("Read payment.");
        const [user, prods, disc, voucher, part, sign] = readPayment(message);

        console.log("Verifying signature.");
        const verifySign = await db.verifySignature(user, sign, part);
        if (verifySign === false) {
            throw new Error("Invalid signature.");
        }

        console.log("Calculating total value to pay.");
        let priceTotal = 0;
        let usedDiscount = 0;
        for (const prod of prods) {
            priceTotal += prod.priceInCents;
        }
        if (disc === true) {
            const discount = await db.getUserDiscount(user);
            if (discount === null) {
                throw new Error("Invalid discount.");
            }
            if (priceTotal > discount) {
                priceTotal -= discount;
                usedDiscount = discount;
            } else {
                usedDiscount = priceTotal;
                priceTotal = 0;
            }
        }

        console.log("Calculating accumulated discount.");
        let accumulated = 0;
        if (voucher !== null) {
            const verifyVoucher = await db.verifyVoucher(user, voucher);
            if (verifyVoucher === false) {
                throw new Error("Invalid voucher.");
            }
            accumulated = priceTotal * 0.15;
        }

        const result = await db.actionPayment(
            user,
            voucher,
            usedDiscount,
            priceTotal,
            accumulated
        );

        if (result === true) {
            ctx.body = { verified: result };
        } else {
            throw new Error("Invalid payment.");
        }
    } catch (error) {
        console.log(error);
        ctx.status = 400;
        ctx.body = {
            error: "Failed in payment transaction",
            details: error.message,
        };
    }
    console.log("\n------------------------\n");
}

async function actionChallengeVouchers(ctx) {
    console.log("\n---- Action Challenge Vouchers ----\n");
    try {
        console.log("Checking user.");
        let { user } = ctx.request.body;
        let [success, result] = await db.actionGetUser(user);

        if (success === true) {
            console.log("Perform adding of nonce.");
            [success, result] = await db.actionAddNonce(user, "VOUCHER");
            if (success === true) {
                ctx.body = { nonce: result };
            } else {
                console.log("Failure in perform adding of nonce.");
                throw new Error(result);
            }
        } else {
            console.log("Failure in checking user.");
            throw new Error(result);
        }
    } catch (error) {
        console.log(error);
        ctx.status = 400;
        ctx.body = {};
    }
    console.log("\n-----------------------------------\n");
}

async function actionChallengeTransaction(ctx) {
    // TODO
}

async function actionGetVouchers(ctx) {
    try {
        let { user, message } = ctx.request.body;
        const [success, result] = await db.actionGetVouchers(user, message);
        if (success === false) {
            ctx.body = { error: result };
        } else {
            ctx.body = {
                vouchers: result,
            };
        }
    } catch (error) {
        console.log("Get Vouchers - ", error);
        ctx.status = 400;
        ctx.body = { error: error };
        return;
    }
}

function readPayment(message) {
    const buf = Buffer.from(message, "base64");
    let offset = 0;

    // read UUID (16 bytes)
    let user = [
        buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
        buf
            .readBigUInt64BE(offset + 8)
            .toString(16)
            .padStart(16, "0"),
    ].join("-");
    offset += 16;
    console.log("\nUserID -", user);

    // read product count (1 byte)
    const numberProducts = buf.readUInt8(offset);
    offset += 1;

    // read each product UUID and price (16 + 2 bytes)
    const products = [];
    console.log("\nProducts");
    for (let i = 0; i < numberProducts; i++) {
        const productId = [
            buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
            buf
                .readBigUInt64BE(offset + 8)
                .toString(16)
                .padStart(16, "0"),
        ].join("-");
        offset += 16;
        const price = buf.readInt16BE(offset);
        offset += 2;
        products.push({ productId: productId, priceInCents: price });
        console.log("   - ProductId and Price -", productId, price);
    }

    // discount flag (1 byte)
    const discount = buf.readUInt8(offset) === 1;
    offset += 1;
    console.log("\nDiscount -", discount);

    const useVoucher = buf.readUInt8(offset) === 1;
    offset += 1;
    console.log("\nUse voucher -", useVoucher);

    // voucher UUID (optional)
    let voucher = null;
    if (useVoucher === true) {
        voucher = [
            buf.readBigUInt64BE(offset).toString(16).padStart(16, "0"),
            buf
                .readBigUInt64BE(offset + 8)
                .toString(16)
                .padStart(16, "0"),
        ].join("-");
        offset += 16;
        console.log("Voucher -", voucher);
    }

    // extract message portion and signature portion
    const part = buf.slice(0, offset);
    const sign = buf.subarray(offset);

    // format user and voucher UUID
    user = formatStringToUUID(user);
    if (voucher !== null) voucher = formatStringToUUID(voucher);

    return [user, products, discount, voucher, part, sign];
}

function formatStringToUUID(str) {
    return str
        .replace("-", "")
        .replace(
            /([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})/,
            "$1-$2-$3-$4-$5"
        );
}

module.exports = router;
