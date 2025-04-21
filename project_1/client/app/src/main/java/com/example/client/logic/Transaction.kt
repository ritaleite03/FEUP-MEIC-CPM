package com.example.client.logic

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.client.R
import java.util.UUID

data class Transaction (
    val id: UUID,
    val price: Double,
    val date: String
)

val listTransactions = arrayListOf<Transaction>()

class TransactionAdapter(
    private val ctx: Context,
    private val listTransactions: ArrayList<Transaction>,
    function: () -> Unit
):
        ArrayAdapter<Transaction>(ctx, R.layout.list_transaction, listTransactions) {
            override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
                val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_transaction, parent, false)

                with(listTransactions[pos]) {
                    row.findViewById<TextView>(R.id.tv_date).text = date
                    row.findViewById<TextView>(R.id.tv_price).text = price.toString()
                }

                return row
            }
        }