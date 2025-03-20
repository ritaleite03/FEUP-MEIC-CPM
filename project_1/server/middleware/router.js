const usersDB = require("../database/dbops");
const KoaRouter = require("koa-router");

const router = new KoaRouter();

(async () => {
    try {
        await usersDB.init();
        console.log("Database initialized successfully");
        router
            .get("/", rootHello)
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
    console.log("in addUser()");
    const { username, usernick, password } = ctx.request.body;
    var result = await usersDB.addNewUser(username, usernick, password);
    if ("errno" in result || result.lastID === 0) {
        ctx.status = 400; // Bad request
        ctx.body = {};
    } else {
        ctx.body = { Id: result.lastID, Name: username };
    }
}

module.exports = router;
