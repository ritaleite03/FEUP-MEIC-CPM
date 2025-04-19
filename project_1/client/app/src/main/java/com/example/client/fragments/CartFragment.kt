package com.example.client.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import com.example.client.R
import com.example.client.data.ProductsDB
import com.example.client.domain.OrderProduct
import com.example.client.domain.ProductAdapter
import com.example.client.domain.currentOrderProduct
import com.example.client.domain.listProducts
import com.example.client.domain.productsDB
import com.example.client.domain.productsDecodeMessage
import com.example.client.domain.productsSort
import com.example.client.domain.productsUpdateList
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.nio.charset.StandardCharsets

/**
 * Fragment to display the shopping cart, allowing viewing and adding products from a QR Code.
 *
 * The cart displays a list of products and has the ability to scan a QR Code to add a new product to the cart.
 * The QR Code contains encrypted information about the product that will be decoded and added to the list.
 */
class CartFragment : Fragment() {

    private lateinit var productListView: ListView
    private lateinit var empty: TextView
    private lateinit var totalTextView: TextView

    private lateinit var spinnerOrder: Spinner
    private lateinit var btQR: Button
    private lateinit var btEnd: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // configuration of the ListView to display the products
        productsDB = ProductsDB(requireActivity().applicationContext)
        productListView = view.findViewById<ListView>(R.id.lv_items)
        empty = view.findViewById(R.id.empty)
        totalTextView = view.findViewById(R.id.tv_total_value)
        currentOrderProduct = OrderProduct.ASCENDING_TIME

        // configuration of buttons
        spinnerOrder = view.findViewById<Spinner>(R.id.order_spinner)
        btQR = view.findViewById<Button>(R.id.bottom_button_qr)
        btEnd = view.findViewById<Button>(R.id.bottom_button_end)

        configuratorProductsListView()
        configuratorBottomButtons()
        configuratorSpinnerOrder()
    }

    fun configuratorProductsListView(){
        productsDB.getProducts()
        productListView.run {
            emptyView = empty
            adapter = ProductAdapter(requireContext(), listProducts) { updateTotal() }
        }
        updateTotal()
        registerForContextMenu(productListView)
    }

    fun configuratorBottomButtons() {
        btQR.setOnClickListener { scanQRCode() }
        btEnd.setOnClickListener { openCheckout(this) }
    }

    fun configuratorSpinnerOrder() {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            OrderProduct.entries.map { it.name.replace("_", " ").lowercase().replaceFirstChar(Char::uppercaseChar) }
        )
        spinnerOrder.adapter = adapter
        spinnerOrder.setSelection(0)
        spinnerOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val orderType = OrderProduct.entries[position]
                productsSort(orderType)
                (productListView.adapter as ProductAdapter).notifyDataSetChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Sum the price of all products.
     */
    private fun updateTotal(){
        val totalValue = listProducts.sumOf{ it.euros + (it.cents / 100.0) }
        totalTextView.text = getString(R.string.price_format, totalValue)
    }

    /**
     * Starts the process of scanning a QR Code.
     */
    private fun scanQRCode() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        scanCodeLauncher.launch(options)
    }

    /**
     * Initializes the scanner launch and processes the QR Code result.
     */
    private val scanCodeLauncher = registerForActivityResult(ScanContract()) {
        if (it != null && it.contents != null) {
            val result = it.contents
            decodeAndShow(result.toByteArray(StandardCharsets.ISO_8859_1))
        }
    }

    /**
     * Decodes the encrypted QR Code tag and displays the new product in the list.
     *
     * The product is identified using a UUID ID, name and value.
     * This data is extracted from the encrypted tag and converted back to the original format.
     *
     * @param combined Encrypted tag obtained from the QR Code.
     */
    private fun decodeAndShow(combined: ByteArray) {
        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val keyString: String? = sharedPreferences.getString("key", null)

        if (keyString != null) {
            val product = productsDecodeMessage(combined, keyString)
            if (product != null) {
                productsUpdateList(product, (productListView.adapter as ProductAdapter))
                updateTotal()
            }
        }
    }
}