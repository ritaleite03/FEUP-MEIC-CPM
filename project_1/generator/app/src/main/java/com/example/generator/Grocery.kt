package com.example.generator

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isGone
import com.example.generator.utils.collapse
import com.example.generator.utils.expand
import org.json.JSONObject
import java.util.UUID

data class Grocery (
    val name: String,
    val category: String,
    val subCategory: String,
    val description: String,
    val imagePath: String,
    val price: Float
) {
    companion object {
        fun parseGroceries(jsonObject: JSONObject): List<Grocery> {
            val groceryList = mutableListOf<Grocery>()
            val groceriesArray = jsonObject.getJSONArray("groceries")

            for (i in 0 until groceriesArray.length()) {
                val item = groceriesArray.getJSONObject(i)

                val grocery = Grocery(
                    name = item.getString("Name"),
                    category = item.getString("Category"),
                    subCategory = item.getString("SubCategory"),
                    description = item.getString("Description"),
                    price = item.getString("Price").toFloat(),
                    imagePath = item.getString("ImagePath")
                )

                groceryList.add(grocery)
            }
            return groceryList
        }
    }
}

class GroceryAdapter(private val groceries: List<Grocery>, private val mainContext: MainActivity):
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
            val uuid = UUID.randomUUID()
            val encryptedTag = generateTag(mainContext.entry, uuid, grocery)
            Log.d("encryptedTag", encryptedTag.toString())
            mainContext.startActivity(Intent(mainContext, MainActivity2::class.java). apply {
                putExtra("data", encryptedTag)
            })
        }
    }

    override fun getItemCount() = groceries.size
}