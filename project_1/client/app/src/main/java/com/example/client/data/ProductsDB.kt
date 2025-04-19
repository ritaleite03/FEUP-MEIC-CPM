package com.example.client.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.client.domain.Product
import com.example.client.domain.listProducts
import com.example.client.domain.listProductsTime
import java.util.UUID

private const val DB_NAME = "products.db"
private const val DB_VERSION = 1

/**
 * Database to keep the information of the shopping list
 */
class ProductsDB(ctx: Context): SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    private val tableProducts = "Products"
    private val keyId = "Id"
    private val colUuid = "Uuid"
    private val colName = "Name"
    private val colEuros = "Euros"
    private val colCents = "Cents"

    override fun onCreate(db: SQLiteDatabase) {
        val sqlCreateTable = "CREATE TABLE $tableProducts(" +
                "$keyId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$colUuid TEXT, " +
                "$colName VARCHAR(100), " +
                "$colEuros INTEGER, " +
                "$colCents INTEGER)"
        db.execSQL(sqlCreateTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $tableProducts")
        onCreate(db)
    }

    /**
     * Inserts the new product in the database
     */
    fun insert(product: Product): Long {
        val values = ContentValues().also {
            with (product) {
                it.put(colUuid, id.toString())
                it.put(colName, name)
                it.put(colEuros, euros)
                it.put(colCents, cents)
            }
        }
        return writableDatabase.insert(tableProducts, null, values)
    }

    /**
     * Deletes an existing product from the database
     */
    fun delete(uuid: UUID) {
        writableDatabase.delete(tableProducts, "$colUuid = ?", arrayOf(uuid.toString()))
    }

    /**
     * Deletes all products from the database
     */
    fun deleteAll() {
        writableDatabase.delete(tableProducts, null, null)
    }

    /**
     * Gets all the products from the database
     */
    fun getProducts() {
        val cursor = readableDatabase.rawQuery("SELECT * FROM  $tableProducts", null)
        listProducts.clear()
        listProductsTime.clear()
        while (cursor.moveToNext()) {
            val product = Product(UUID.randomUUID(),"",0,0)
            with (product) {
                id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(colUuid)))
                name = cursor.getString(cursor.getColumnIndexOrThrow(colName))
                euros = cursor.getInt(cursor.getColumnIndexOrThrow(colEuros))
                cents = cursor.getInt(cursor.getColumnIndexOrThrow(colCents))
            }
            listProducts.add(product)
            listProductsTime.add(product)
        }
        cursor.close()
    }
}