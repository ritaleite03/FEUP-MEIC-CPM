package com.example.client.domain

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.client.R
import com.example.client.base64ToPublicKey
import com.example.client.data.ProductsDB
import com.example.client.utils.Crypto.CRYPTO_RSA_ENC_ALGO
import com.example.client.utils.Crypto.CRYPTO_RSA_KEY_SIZE
import com.example.client.utils.Crypto.CRYPTO_RSA_SIGN_ALGO
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.util.UUID
import javax.crypto.Cipher
import kotlin.collections.get
import kotlin.text.get

/**
 * Database with the products
 */
lateinit var productsDB: ProductsDB

/**
 * Data class representing a product.
 *
 * @property id Unique identifier of the product.
 * @property name Name of the product.
 * @property euros Whole part of the product's price.
 * @property cents Fractional part of the product's price (0-99).
 */
data class Product(
    var id: UUID,
    var name: String,
    var category: String,
    var subCategory: String,
    var price: Float
)

/**
 * Enum class with the types of ordering
 */
enum class OrderProduct {
    ASCENDING_TIME,
    DESCENDING_TIME,
    ASCENDING_NAME,
    DESCENDING_NAME,
    ASCENDING_PRICE,
    DESCENDING_PRICE
}

/**
 * List of all the products in the cart (initially empty).
 */
val listProducts = arrayListOf<Product>()
/**
 * List of all the products in the cart (initially empty), by date of acquisition.
 */
val listProductsTime = arrayListOf<Product>()

/**
 * Order of the products' list (initially by date of acquisition).
 */
var currentOrderProduct = OrderProduct.ASCENDING_TIME

/**
 * Sort the products in listProducts according to the order desired by the user.
 * @param type Specifies the type of order for the products
 */
fun productsSort(type : OrderProduct) {
    when (type) {
        OrderProduct.ASCENDING_TIME -> {
            listProducts.clear()
            listProducts.addAll(listProductsTime)
        }
        OrderProduct.DESCENDING_TIME -> {
            listProducts.clear()
            listProducts.addAll(listProductsTime.reversed())
        }
        OrderProduct.ASCENDING_NAME -> {
            listProducts.sortBy { it.name.lowercase() }
        }
        OrderProduct.DESCENDING_NAME -> {
            listProducts.sortByDescending { it.name.lowercase() }
        }
        OrderProduct.ASCENDING_PRICE -> {
            listProducts.sortBy { it.price }
        }
        OrderProduct.DESCENDING_PRICE -> {
            listProducts.sortByDescending { it.price }
        }
    }
}

/**
 * Decodes message from the qr code
 * @param message Message retrieved from the qr code
 * @param key Key (in string format) of the market
 */
fun productsDecodeMessage(message: ByteArray, key: String) : Product? {
    var clearTextTag = ByteArray(0)
    val numberBytes = CRYPTO_RSA_KEY_SIZE / 8
    val totalSize = numberBytes * 2

    if (message.size < totalSize) return null
    val encryptedTag = message.copyOfRange(0, numberBytes)
    val signature = message.copyOfRange(numberBytes, numberBytes + numberBytes)

    try {
        val key = base64ToPublicKey(key)
        clearTextTag = Cipher.getInstance(CRYPTO_RSA_ENC_ALGO).run {
            init(Cipher.DECRYPT_MODE, key)
            doFinal(encryptedTag)
        }
        val signatureVerifier = Signature.getInstance(CRYPTO_RSA_SIGN_ALGO).run {
            initVerify(key)
            update(encryptedTag)
            verify(signature)
        }
        if(!signatureVerifier) return null
    }
    catch (_: Exception) {
        return null
    }

    val tag = ByteBuffer.wrap(clearTextTag)
    val tagId = tag.int
    val id = UUID(tag.long, tag.long)

    val nameLength = tag.get().toInt()
    val nameBytes = ByteArray(nameLength)
    tag.get(nameBytes)

    val categoryLength = tag.get().toInt()
    val categoryBytes = ByteArray(categoryLength)
    tag.get(categoryBytes)

    val subCategoryLength = tag.get().toInt()
    val subCategoryBytes = ByteArray(subCategoryLength)
    tag.get(subCategoryBytes)

    val price = tag.getFloat()

    val name = String(nameBytes, StandardCharsets.ISO_8859_1)
    val category = String(categoryBytes, StandardCharsets.ISO_8859_1)
    val subCategory = String(subCategoryBytes, StandardCharsets.ISO_8859_1)
    return Product(id, name, category, subCategory, price)
}

/**
 * Updates the list of products and database.
 * @param product Product to be added to the list.
 * @param adapter adapter of the Products List View.
 */
fun productsUpdateList(product : Product, adapter: ProductAdapter) {
    productsDB.insert(product)
    listProductsTime.add(product)
    listProducts.add(product)
    productsSort(currentOrderProduct)
    adapter.notifyDataSetChanged()
}

/**
 * Adapter to bind [Product] objects to an [android.widget.ListView].
 *
 * @param ctx context of the Fragment where the Adapter will be used.
 * @param listProducts list of the products that is going to be displayed and manipulated.
 * @param onRemove callback function to update total value.
 */
class ProductAdapter(
    private val ctx: Context,
    private val listProducts: ArrayList<Product>,
    private val onRemove: (Int) -> Unit
):
    ArrayAdapter<Product>(ctx, R.layout.list_item, listProducts) {
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_item, parent, false)

        // fill with the products' data
        with(listProducts[pos]) {
            row.findViewById<TextView>(R.id.tv_name).text = name
            row.findViewById<TextView>(R.id.tv_value).text = ctx.getString(R.string.price_format, price)
            row.findViewById<TextView>(R.id.tv_value).text = price.toString()

            // define remove action
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                productsDB.delete(listProducts[pos].id)
                listProducts.removeAt(pos)
                listProductsTime.removeIf { it.id ==  listProducts[pos].id}
                notifyDataSetChanged()
                onRemove(pos)
            }
        }
        return row
    }
}
