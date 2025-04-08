package com.example.client.fragments

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.client.R
import java.util.UUID

/**
 * Data class representing a product
 *
 * @property id unique identifier of the product
 * @property name name of the product
 * @property value value (price) of the product
 */
data class Product(
    val id: UUID,
    val name: String,
    val value: Double
)

/**
 * List of the all the products in the cart (initially empty)
 */
val listProducts = arrayListOf<Product>()

/**
 * Adapter to bind [Product] objects to an [android.widget.ListView]
 *
 * @param ctx context of the Fragment where the Adapter will be used
 * @param listProducts list of the products that is going to be displayed and manipulated
 */
class ProductAdapter(
    private val ctx: Context,
    val listProducts: ArrayList<Product>): ArrayAdapter<Product>(ctx, R.layout.list_item, listProducts
    )
{
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View
    {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_item, parent, false)

        // fill with the products' data
        with(listProducts[pos]) {
            row.findViewById<TextView>(R.id.tv_name).text = name
            row.findViewById<TextView>(R.id.tv_value).text = value.toString()

            // define remove action
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                listProducts.removeAt(pos)
                notifyDataSetChanged()
            }
        }
        return row
    }
}