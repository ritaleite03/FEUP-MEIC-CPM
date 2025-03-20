"use strict";
const path = require("path");
const fs = require("fs");

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
            Id INTEGER PRIMARY KEY AUTOINCREMENT,
            Name TEXT NOT NULL,
            Nick TEXT NOT NULL,
            Pass TEXT NOT NULL
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

    async addNewUser(name, nick, pass) {
        var result;
        try {
            result = await this.db.run(
                "insert into Users(Name, Nick, Pass) values(?,?,?)",
                [name, nick, pass]
            );
            if (result.changes === 0) result.lastID = 0;
        } catch (err) {
            result = err;
        }
        return result;
    }
}

module.exports = new DBOps();
