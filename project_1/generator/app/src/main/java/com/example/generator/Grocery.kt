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
import androidx.core.view.isGone
import com.example.generator.utils.collapse
import com.example.generator.utils.expand

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
        val category: TextView = view.findViewById(R.id.groceryCategory)
        val image: ImageView = view.findViewById(R.id.groceryImage)
        val title: TextView = view.findViewById(R.id.groceryTitle)
        val arrow: ImageView = view.findViewById(R.id.toggleArrow)
        val price: TextView = view.findViewById(R.id.groceryPrice)
        val description: TextView = view.findViewById(R.id.groceryDescription)
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

        holder.arrow.setOnClickListener {
            if (holder.description.isGone) {
                expand(holder.description)
                holder.arrow.animate().rotation(-180f).setDuration(300).start()
            }
            else {
                collapse(holder.description)
                holder.arrow.animate().rotation(0f).setDuration(300).start()
            }
        }

        holder.category.text = grocery.category
        holder.image.setImageResource(resourceID)
        holder.title.text = if (grocery.name == grocery.subCategory) { grocery.name } else { "${grocery.subCategory} (${grocery.name})" }
        holder.price.text = holder.itemView.context.getString(R.string.grocery_price, grocery.price.toString())
        holder.description.text = grocery.description
        holder.cartButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = groceries.size
}