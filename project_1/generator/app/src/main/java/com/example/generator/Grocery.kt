package com.example.generator

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

data class Grocery (
    val name: String,
    val category: String,
    val subCategory: String,
    val description: String,
    val imagePath: String,
    val price: Float
)

class GroceryAdapter(private val groceries: List<Grocery>, private val mainContext: Activity):
    RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    class GroceryViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.groceryImage)
        val title: TextView = view.findViewById(R.id.groceryTitle)
        val price: TextView = view.findViewById(R.id.groceryPrice)
        val cartButton: Button = view.findViewById(R.id.addToCartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grocery_item, parent, false)
        return GroceryViewHolder(view)
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val grocery = groceries[position]

        val resourceID = mainContext.resources.getIdentifier(
            grocery.imagePath,
            "drawable",
            mainContext.packageName
        )

        holder.title.text = if (grocery.name == grocery.subCategory) {
            grocery.name
        }
        else {
            "${grocery.subCategory} (${grocery.name})"
        }
        holder.image.setImageResource(resourceID)
        holder.price.text = holder.itemView.context.getString(R.string.grocery_price, grocery.price.toString())
        holder.cartButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = groceries.size
}