package com.example.client.domain

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.client.R
import com.example.client.data.VouchersDB
import java.util.UUID

lateinit var vouchersDB : VouchersDB

/**
 * Data class representing a voucher.
 *
 * @property id Unique identifier of the product.
 * @property value Value of the voucher (15%).
 */
data class Voucher(
    var id: UUID,
    var value: Int
)

/**
 * List of the all the vouchers (initially empty).
 */
val listVouchers = arrayListOf<Voucher>()

/**
 * Adapter to bind [Voucher] objects to an [android.widget.ListView].
 * @param ctx context of the Fragment where the Adapter will be used.
 * @param listVouchers list of the vouchers that is going to be displayed.
 * @param function
 */
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
