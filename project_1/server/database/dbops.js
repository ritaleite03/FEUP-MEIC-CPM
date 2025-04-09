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
        console.log("in verifyMessage");

        let uuidWithoutHyphen = userId.replace("-", "");

        // Passo 2: Formatar como UUID com hífens na posição correta
        let formattedUUID = uuidWithoutHyphen.replace(
            /([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})/,
            "$1-$2-$3-$4-$5"
        );

        // Formatar ambos os UUIDs para o formato correto
        console.log(formattedUUID);

        try {
            const row = await this.db.get(
                `SELECT KeyEC FROM Users WHERE Uuid = ?`,
                [formattedUUID]
            );
            console.log(1);
            const rows = await this.db.all(`SELECT * FROM Users`);
            console.log("Registos encontrados:", rows.length);
            rows.forEach((row, i) => {
                console.log(`${i + 1}:`, row);
            });

            if (!row) {
                throw new Error("Utilizador não encontrado");
            }

            // const rows = await db.all(`SELECT Uuid FROM Users`);
            // rows.forEach((row) => {
            //     console.log(row.Uuid);
            // });

            //console.log(row);
            const publicKeyString = row.KeyEC;
            console.log(2);

            const buffer = Buffer.from(publicKeyString, "base64");
            console.log(3);

            const publicKey = crypto.createPublicKey({
                key: buffer,
                format: "der",
                type: "spki",
            });
            console.log(4);

            console.log(messageContent);
            const verified = crypto.verify(
                "sha256",
                messageContent, // conteúdo sem assinatura
                publicKey, // objeto já criado com crypto.createPublicKey()
                signature // assinatura (Buffer)
            );

            return verified;
        } catch (err) {
            console.log(err);
        }
    }
}

module.exports = new DBOps();
