package com.example.client.fragments

import android.app.Activity
import android.content.Context

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.client.R
import java.util.UUID

data class Product(
    val id: UUID,
    val name: String,
    val euros: Int,
    val cents: Int
)

val listProducts = arrayListOf(
    Product(UUID.randomUUID(), "teste1", 1,0),
    Product(UUID.randomUUID(), "teste2", 2,0),
    Product(UUID.randomUUID(), "teste3", 3,0),
    Product(UUID.randomUUID(), "teste4", 4,0),
    Product(UUID.randomUUID(), "teste5", 5,0),
    Product(UUID.randomUUID(), "teste6", 6,0),
    Product(UUID.randomUUID(), "teste7", 7,0),
    Product(UUID.randomUUID(), "teste8", 8,0)
)

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

class ProductAdapter(private val ctx: Context,
                     private val listProducts: ArrayList<Product>,
                     private val onRemove: (Int) -> Unit
):
    ArrayAdapter<Product>(ctx, R.layout.list_item, listProducts) {
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_item, parent, false)
        with(listProducts[pos]) {
            row.findViewById<TextView>(R.id.tv_name).text = name
            var value: Double = euros + (cents / 100.0)
            row.findViewById<TextView>(R.id.tv_value).text = ctx.getString(R.string.price_format, value)
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                listProducts.removeAt(pos)
                notifyDataSetChanged()
                onRemove(pos)
            }
        }
        return row
    }
}
