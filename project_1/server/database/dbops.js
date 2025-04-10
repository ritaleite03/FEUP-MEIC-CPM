"use strict";
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
const { v4: uuidv4 } = require("uuid");

// path to the SQLite database
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

    /**
     * Creates the 'Users' table if it doesn't exist in the database.
     * The table stores the UUID, EC public key, and RSA public key of users.
     * @returns {Promise<void>} - Returns a Promise that resolves when the tables are created.
     */
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

    /**
     * Adds a new user to the database with their EC and RSA public keys.
     * @param {string} keyEC The EC public key encoded in Base64.
     * @param {string} keyRSA The RSA public key encoded in Base64.
     * @returns {Promise<[string, Object]>} - Returns the user's UUID and the result of the database operation.
     */
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

    /**
     * Verifies the ECDSA signature of a payment message.
     * Uses the EC public key stored in the database to verify the signature.
     * @param {string} userId The UUID of the user making the payment.
     * @param {Buffer} signature The signature of the message.
     * @param {Buffer} messageContent The content of the message that was signed.
     * @returns {Promise<boolean>} Returns 'true' if the signature is valid, 'false' otherwise.
     */
    async verifyMessage(userId, signature, messageContent) {
        // remove hyphens from the user UUID and format it correctly
        let uuidWithoutHyphen = userId.replace("-", "");
        let formattedUUID = uuidWithoutHyphen.replace(
            /([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})/,
            "$1-$2-$3-$4-$5"
        );

        try {
            // retrieve the EC public key from the database for the specified user
            const row = await this.db.get(
                `SELECT KeyEC FROM Users WHERE Uuid = ?`,
                [formattedUUID]
            );
            if (!row) throw new Error("User not found!");

            const publicKeyString = row.KeyEC;
            const buffer = Buffer.from(publicKeyString, "base64");

            // verify the signature using the public key and message content
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
            console.log("Error in signature verification:", err);
            throw new Error("Error in signature verification");
        }
    }
}

module.exports = new DBOps();
