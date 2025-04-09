"use strict";
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
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

    async verifyMessage(userId, signature, messageContent) {
        console.log("In verifyMessage");

        let uuidWithoutHyphen = userId.replace("-", "");
        let formattedUUID = uuidWithoutHyphen.replace(
            /([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})/,
            "$1-$2-$3-$4-$5"
        );

        try {
            const row = await this.db.get(
                `SELECT KeyEC FROM Users WHERE Uuid = ?`,
                [formattedUUID]
            );

            if (!row) {
                throw new Error("Utilizador não encontrado");
            }

            const publicKeyString = row.KeyEC;
            const buffer = Buffer.from(publicKeyString, "base64");

            const publicKey = crypto.createPublicKey({
                key: buffer,
                format: "der",
                type: "spki",
            });

            const verified = crypto.verify(
                "sha256",
                messageContent,
                publicKey,
                signature
            );

            return verified;
        } catch (err) {
            console.log("Erro na verificação da assinatura:", err);
            throw new Error("Falha na verificação da assinatura");
        }
    }
}

module.exports = new DBOps();
