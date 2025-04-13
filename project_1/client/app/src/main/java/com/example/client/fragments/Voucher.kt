package com.example.client.fragments

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.client.R
import java.util.UUID

data class Voucher(
    val id: UUID,
    val value: Int
)

val listVouchers = arrayListOf<Voucher>()

class VoucherAdapter(
    private val ctx: Context,
    private val listVouchers: ArrayList<Voucher>,
    function: () -> Unit,
):
    ArrayAdapter<Voucher>(ctx, R.layout.list_voucher, listVouchers) {
    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: (ctx as Activity).layoutInflater.inflate(R.layout.list_voucher, parent, false)

        with(listVouchers[pos]) {
            row.findViewById<TextView>(R.id.tv_name).text = id.toString()
            row.findViewById<TextView>(R.id.tv_value).text = value.toString()
        }
        return row
    }
}
