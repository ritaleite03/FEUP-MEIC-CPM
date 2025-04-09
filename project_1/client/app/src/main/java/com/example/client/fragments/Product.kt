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
 * Data class representing a product.
 *
 * @property id Unique identifier of the product.
 * @property name Name of the product.
 * @property euros Whole part of the product's price.
 * @property cents Fractional part of the product's price (0-99).
 */
data class Product(
    val id: UUID,
    val name: String,
    val euros: Int,
    val cents: Int
)

/**
 * List of the all the products in the cart (initially empty).
 */
val listProducts = arrayListOf<Product>()

//class CartViewModel : ViewModel() {
//    private val _products = MutableLiveData<ArrayList<Product>>(arrayListOf())
//    val products: LiveData<ArrayList<Product>> = _products
//
//    fun addProduct(product: Product) {
//        val updatedList = _products.value ?: arrayListOf()
//        updatedList.add(product)
//        _products.value = updatedList
//    }
//
//    fun removeProductAt(index: Int) {
//        val updatedList = _products.value ?: return
//        updatedList.removeAt(index)
//        _products.value = updatedList
//    }
//
//    fun getTotal(): Double {
//        return _products.value?.sumOf { it.value } ?: 0.00
//    }
//}

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
            row.findViewById<TextView>(R.id.tv_name).text = name
            var value: Double = euros + (cents / 100.0)
            row.findViewById<TextView>(R.id.tv_value).text = ctx.getString(R.string.price_format, value)
            row.findViewById<TextView>(R.id.tv_value).text = value.toString()

            // define remove action
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                listProducts.removeAt(pos)
                notifyDataSetChanged()
                onRemove(pos)
            }
        }
        return row
    }
}
