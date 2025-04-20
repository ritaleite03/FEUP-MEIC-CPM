package com.example.client.domain

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.client.R
import com.example.client.data.ProductsDB
import java.util.UUID

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
 * List of the all the products in the cart (initially empty).
 */
val listProducts = arrayListOf<Product>()

/**
 * Adapter to bind [Product] objects to an [android.widget.ListView].
 *
 * @param ctx context of the Fragment where the Adapter will be used.
 * @param listProducts list of the products that is going to be displayed and manipulated.
 * @param onRemove callback function to update total value
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
            row.findViewById<TextView>(R.id.tv_name).text = if (name == subCategory) name else "$subCategory ($name)"
            row.findViewById<TextView>(R.id.tv_value).text = ctx.getString(R.string.price_format, price)

            // define remove action
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                productsDB.delete(listProducts[pos].id)
                listProducts.removeAt(pos)
                notifyDataSetChanged()
                onRemove(pos)
            }
        }
        return row
    }
}
