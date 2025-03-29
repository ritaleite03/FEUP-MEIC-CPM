"use strict";
const path = require("path");
const fs = require("fs");
const { v4: uuidv4 } = require("uuid");

const DB_PATH = path.resolve("database/users.db");

class DBOps {
    constructor() {
        this.db = null;
    }

    async init() {
        if (!this.db) {
            const { Database } = await import("sqlite-async");
            const dbExists = fs.existsSync(DB_PATH);
            this.db = await Database.open(DB_PATH);
            console.log("Connected to users.db");
            if (!dbExists) {
                await this.createTables();
            }
        }
    }

    async createTables() {
        const sql = `
        CREATE TABLE IF NOT EXISTS Users (
            Uuid UUID PRIMARY KEY, 
            KeyEC TEXT NOT NULL, 
            KeyRSA TEXT NOT NULL
        );
    `;
        await this.db.run(sql);
        console.log("Created table 'Users'");
    }

    async dbTest(id) {
        const result = await this.db.get("select Name from Users where Id=?", [
            id,
        ]);
        console.log(result);
        return result.Name;
    }

    async getUser(name, nick, pass) {
        var result;
        try {
            result = await this.db.get(
                "select Name from Users where Name=? and Nick=? and Pass=?",
                [name, nick, pass]
            );
            if (result == null) result = {};
        } catch (err) {
            result = err;
        }
        return result;
    }

    async addNewUser(keyEC, keyRSA) {
        let result;
        let uuid;
        try {
            uuid = uuidv4();
            result = await this.db.run(
                "insert into Users(Uuid, KeyEC, KeyRSA) values(?,?,?)",
                [uuid, keyEC, keyRSA]
            );
            if (result.changes === 0) result.lastID = 0;
        } catch (err) {
            result = err;
        }
        return [uuid, result];
    }
}

module.exports = new DBOps();
