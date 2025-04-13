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
        try {
            const tableUser = `
            CREATE TABLE IF NOT EXISTS Users (
            Uuid UUID PRIMARY KEY, 
            KeyEC TEXT NOT NULL, 
            KeyRSA TEXT NOT NULL,
            Current REAL DEFAULT 0,
            Discount REAL DEFAULT 0
        );
        `;
            const tableVoucher = `
            CREATE TABLE IF NOT EXISTS Vouchers (
            Uuid UUID,
            UserUuid UUID,
            PRIMARY KEY (Uuid, UserUuid),
            FOREIGN KEY (UserUuid) REFERENCES Users(Uuid) ON DELETE CASCADE
        );
        `;

            const tableNonce = `
            CREATE TABLE IF NOT EXISTS Nonce (
            Uuid UUID,
            UserUuid UUID,
            Type TEXT NOT NULL,
            PRIMARY KEY (Uuid, UserUuid, Type),
            FOREIGN KEY (UserUuid) REFERENCES Users(Uuid) ON DELETE CASCADE
        );
        `;
            await this.db.run(tableUser);
            await this.db.run(tableVoucher);
            await this.db.run(tableNonce);
            console.log("Success in the database initialization!");

            const check = await this.db.all(
                "SELECT name FROM sqlite_master WHERE type='table'"
            );
            console.log("Tables in DB:", check);
        } catch (error) {
            console.log("Failure in the database initialization!", error);
        }
    }

    async actionGetUser(user) {
        try {
            const row = await this.db.get(
                `SELECT * FROM Users WHERE Uuid = ?`,
                [user]
            );
            if (!row) {
                throw new Error("User not found!");
            }
            return [true, null];
        } catch (error) {
            return [false, error];
        }
    }

    async getUserDiscount(userId) {
        try {
            // retrieve the EC public key from the database for the specified user
            const row = await this.db.get(
                `SELECT Discount FROM Users WHERE Uuid = ?`,
                [userId]
            );
            if (!row) {
                console.log("User not found!");
                return null;
            }
            return row.Discount;
        } catch (error) {
            console.log("Error in discount fetch!");
            return null;
        }
    }

    async actionGetVouchers(user, message) {
        try {
            const [success, result] = await this.verifyNonce(
                message,
                user,
                "VOUCHER"
            );

            if (success === false) {
                throw new Error(result);
            }

            // check if there are vouchers to fetch
            const rows = await this.db.all(
                `SELECT * FROM Vouchers WHERE UserUuid = ?`,
                [user]
            );
            console.log(1);

            if (!rows || rows.length === 0) {
                return [true, []];
            }
            console.log(1);

            let vouchers = [];
            for (const row of rows) {
                vouchers.push({ uuid: row.Uuid });
            }
            return [true, vouchers];
        } catch (error) {
            console.log("Error in voucher fetch!", error);
            return [false, error];
        }
    }

    async actionAddNonce(user, type) {
        try {
            const uuid = uuidv4();
            await this.db.run(
                `
                INSERT OR IGNORE INTO Nonce (UserUuid, Uuid, Type)
                VALUES (?, ?, ?)
            `,
                [user, uuid, type]
            );

            const row = await this.db.get(
                `
                SELECT Uuid FROM Nonce WHERE UserUuid = ? AND Type = ?
            `,
                [user, type]
            );
            return [true, row.Uuid];
        } catch (error) {
            return [false, error];
        }
    }

    /**
     * Adds a new user to the database with their EC and RSA public keys.
     * @param {string} keyEC The EC public key encoded in Base64.
     * @param {string} keyRSA The RSA public key encoded in Base64.
     * @returns {Promise<[string, Object]>} - Returns the user's UUID and the result of the database operation.
     */
    async actionRegistration(keyEC, keyRSA) {
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

    async actionPayment(user, voucher, usedDiscount, total, discount) {
        console.log("\nStart Payment Transaction.");
        await this.db.run("BEGIN");
        try {
            // delete voucher if it was used
            if (voucher !== null) {
                console.log("Deleting used voucher.");
                await this.db.run(
                    `DELETE FROM Vouchers WHERE Uuid = ? AND UserUuid = ?`,
                    [voucher, user]
                );
            }

            // update user according with the use of discount
            console.log("Update user.");
            await this.db.run(
                `UPDATE Users SET Current = Current + ? , Discount = Discount - ? + ? WHERE Uuid = ?`,
                [total, usedDiscount, discount, user]
            );

            await this.db.run("COMMIT");
            console.log("Success in payment transaction 1!");
        } catch (error) {
            await this.db.run("ROLLBACK");
            console.log("Failure in payment transaction 1!", error);
            return false;
        }

        await this.db.run("BEGIN");
        try {
            // check if id current divisible by 100
            const row = await this.db.get(
                `SELECT Current FROM Users WHERE Uuid = ?`,
                [user]
            );
            if (!row) throw new Error("User not found!");

            const current = row.Current / 100;
            const numberVouchers = Math.floor(current / 100);

            if (numberVouchers > 0) {
                console.log("Add new voucher.");
                // add new vouchers to the user
                for (let i = 0; i < numberVouchers; i++) {
                    const result = await this.db.run(
                        "insert into Vouchers(Uuid, UserUuid) values(?,?)",
                        [uuidv4(), user]
                    );
                    if (result.changes === 0)
                        throw new Error("result.changes === 0");
                }

                // update current in the user
                await this.db.run(
                    `UPDATE Users SET Current = ? WHERE Uuid = ?`,
                    [current % 100, user]
                );
            }
            await this.db.run("COMMIT");
            console.log("Success in payment transaction 2!");
            console.log("End Payment Transaction.\n");
            return true;
        } catch (error) {
            await this.db.run("ROLLBACK");
            console.log("Failure in payment transaction 2!", error);
            return false;
        }
    }

    /**
     * Verifies the ECDSA signature of a payment message.
     * Uses the EC public key stored in the database to verify the signature.
     * @param {string} userId The UUID of the user making the payment.
     * @param {Buffer} signature The signature of the message.
     * @param {Buffer} messageContent The content of the message that was signed.
     * @returns {Promise<boolean>} Returns 'true' if the signature is valid, 'false' otherwise.
     */
    async verifySignature(userId, signature, messageContent) {
        try {
            // retrieve the EC public key from the database for the specified user
            const row = await this.db.get(
                `SELECT KeyEC FROM Users WHERE Uuid = ?`,
                [userId]
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
        } catch (error) {
            console.log("Error in signature verification!");
            return false;
        }
    }

    async verifyVoucher(userId, voucherId) {
        try {
            const row = await this.db.get(
                `SELECT Uuid FROM Vouchers WHERE Uuid = ? AND UserUuid = ?`,
                [voucherId, userId]
            );
            if (!row) throw new Error("No match");
            return true;
        } catch (error) {
            console.log("Error in voucher verification!");
            return false;
        }
    }

    async verifyNonce(message, user, type) {
        console.log("\n---- In Verify Nonce----\n");
        try {
            console.log("Getting Public Key.");
            const row = await this.db.get(
                `SELECT KeyRSA FROM Users WHERE Uuid = ?`,
                [user]
            );

            if (!row) throw new Error("User not found!");

            console.log("Building Public Key.");
            const publicKeyString = row.KeyRSA;
            const buffer = Buffer.from(publicKeyString, "base64");
            const publicKey = crypto.createPublicKey({
                key: buffer,
                format: "der",
                type: "spki",
            });

            console.log("Getting Nonce.");

            const row1 = await this.db.get(
                `SELECT Uuid FROM Nonce WHERE UserUuid = ? AND type = ?`,
                [user, type]
            );
            if (!row1) throw new Error("No match");
            const nonce = row1.Uuid;

            console.log("Verifying Nonce.");

            const decrypted = crypto
                .publicDecrypt(
                    {
                        key: publicKey,
                        padding: crypto.constants.RSA_PKCS1_PADDING,
                    },
                    Buffer.from(message, "base64")
                )
                .toString("hex");

            const decryptedUuid = [
                decrypted.slice(0, 8),
                decrypted.slice(8, 12),
                decrypted.slice(12, 16),
                decrypted.slice(16, 20),
                decrypted.slice(20),
            ].join("-");

            if (decryptedUuid.toString("hex") !== nonce) {
                throw new Error("Nonce is incorrect");
            }

            console.log("\n------------------------\n");
            return [true, null];
        } catch (error) {
            console.log(error);
            console.log("\n------------------------\n");
            return [false, error];
        }
    }
}

module.exports = new DBOps();
