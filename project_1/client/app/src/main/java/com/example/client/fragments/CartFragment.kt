package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.client.R
import com.example.client.base64ToPublicKey
import com.example.client.utils.Crypto.RSA_ENC_ALGO
import com.example.client.utils.byteArrayToHex
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.crypto.Cipher


class CartFragment : Fragment() {

    private lateinit var productListView: ListView
    private lateinit var empty: TextView
    private lateinit var totalTextView: TextView

    //private val viewModel: CartViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btQR = view.findViewById<Button>(R.id.bottom_button_qr)

        btQR.setOnClickListener { scanQRCode() }

        productListView = view.findViewById<ListView>(R.id.lv_items)
        empty = view.findViewById(R.id.empty)
        totalTextView = view.findViewById(R.id.tv_total_value)

        //productListView.emptyView = empty

        //viewModel.products.observe(viewLifecycleOwner) { products ->
        //    productListView.adapter = ProductAdapter(requireContext(), products) { pos ->
        //        viewModel.removeProductAt(pos)
        //    }
        //
        //    totalTextView.text = viewModel.getTotal().toString()
        //}

        productListView.run {
            emptyView = empty
            adapter = ProductAdapter(requireContext(), listProducts){
                updateTotal()
            }
        }
        updateTotal()
        //registerForContextMenu(productListView)

    }

    private fun updateTotal(){
        val totalValue = listProducts.sumOf{ it.euros + (it.cents / 100.0) }
        totalTextView.text = getString(R.string.price_format, totalValue)
    }

    private fun scanQRCode() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        scanCodeLauncher.launch(options)
    }

    private val scanCodeLauncher = registerForActivityResult(ScanContract()) {
        if (it != null && it.contents != null) {
            val result = it.contents
            decodeAndShow(result.toByteArray(StandardCharsets.ISO_8859_1))
        }
    }

    private fun decodeAndShow(encTag: ByteArray) {
        var clearTextTag: ByteArray = ByteArray(0)

        try {
            val sharedPreferences =
                requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val keyString: String? = sharedPreferences.getString("key", null)

            if (keyString != null) {
                val key = base64ToPublicKey(keyString)
                clearTextTag = Cipher.getInstance(RSA_ENC_ALGO).run {
                    init(Cipher.DECRYPT_MODE, key)
                    doFinal(encTag)
                }
            }
        }
        catch (e: Exception) {
            return
        }

        val tag = ByteBuffer.wrap(clearTextTag)
        val tId = tag.int
        val id = UUID(tag.long, tag.long)
        val euros = tag.short.toInt()

        val cents = tag.get().toInt()
        val bName = ByteArray(tag.get().toInt())
        tag[bName]
        val name = String(bName, StandardCharsets.ISO_8859_1)

        val newProduct = Product(id, name, euros, cents)

        listProducts.add(newProduct)

        (productListView.adapter as ProductAdapter).notifyDataSetChanged()
        updateTotal()

    }

}