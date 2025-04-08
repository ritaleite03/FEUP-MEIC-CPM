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

data class Product(
    val id: UUID,
    val name: String,
    val value: Double
)

val listProducts = arrayListOf(
    Product(UUID.randomUUID(), "teste1", 1.0),
    Product(UUID.randomUUID(), "teste2", 1.0),
    Product(UUID.randomUUID(), "teste3", 1.0),
    Product(UUID.randomUUID(), "teste4", 1.0),
    Product(UUID.randomUUID(), "teste5", 1.0),
    Product(UUID.randomUUID(), "teste6", 1.0),
    Product(UUID.randomUUID(), "teste7", 1.0),
    Product(UUID.randomUUID(), "teste8", 1.0)
)


class ProductAdapter(private val ctx: Context, val listProducts: ArrayList<Product>): ArrayAdapter<Product>(ctx,
    R.layout.list_item, listProducts) {
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_item, parent, false)
        with(listProducts[pos]) {
            row.findViewById<TextView>(R.id.tv_name).text = name
            row.findViewById<TextView>(R.id.tv_value).text = value.toString()
            row.findViewById<ImageButton>(R.id.bt_remove).setOnClickListener {
                listProducts.removeAt(pos)
                notifyDataSetChanged()
            }
        }
        return row
    }
}
