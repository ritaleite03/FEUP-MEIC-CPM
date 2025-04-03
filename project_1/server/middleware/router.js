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

module.exports = router;
