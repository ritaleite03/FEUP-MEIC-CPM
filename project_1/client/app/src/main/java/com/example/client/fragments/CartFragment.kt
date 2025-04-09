package com.example.client.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.client.MainActivity
import com.example.client.MainActivity2
import com.example.client.MainActivity3
import com.example.client.R
import com.example.client.base64ToPublicKey
import com.example.client.getPrivateKey
import com.example.client.utils.Crypto
import com.google.android.material.textfield.TextInputEditText
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.util.UUID
import javax.crypto.Cipher

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

    //private val viewModel: CartViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // configuration of the button to scan QR Code
        val btQR = view.findViewById<Button>(R.id.bottom_button_qr)
        btQR.setOnClickListener { scanQRCode() }

        val btEnd = view.findViewById<Button>(R.id.bottom_button_end)
        btEnd.setOnClickListener {

            val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences",
                Context.MODE_PRIVATE
            )
            val uuid = sharedPreferences.getString("uuid", null)
            val products: List<Pair<UUID, Short>> = listProducts.map { product ->
                product.id to (product.euros * 100 + product.cents).toInt().toShort()
            }


            if (uuid != null && !products.isEmpty()) {

                var encryptedTag = generateCheckoutMessage(UUID.fromString(uuid), products, null, false)
                startActivity(Intent(this.requireActivity(), MainActivity3::class.java).apply {
                    putExtra("data", encryptedTag)
                })
            }
        }

        // configuration of the ListView to display the products
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

        // if the product list is empty, display the "empty" message
        productListView.run {
            emptyView = empty
            adapter = ProductAdapter(requireContext(), listProducts){
                updateTotal()
            }
        }
        updateTotal()
        //registerForContextMenu(productListView)

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
        var clearTextTag = ByteArray(0)

        val numberBytes = Crypto.RSA_KEY_SIZE / 8
        val totalSize = numberBytes * 2

        if (combined.size < totalSize) {
            Log.e("decodeAndShow", "Error")
            return
        }

        val encryptedTag = combined.copyOfRange(0, numberBytes)
        val signature = combined.copyOfRange(numberBytes, numberBytes + numberBytes)
        try {
            val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val keyString: String? = sharedPreferences.getString("key", null)

            if (keyString != null) {
                val key = base64ToPublicKey(keyString)

                clearTextTag = Cipher.getInstance(Crypto.RSA_ENC_ALGO).run {
                    init(Cipher.DECRYPT_MODE, key)
                    doFinal(encryptedTag)
                }

                val signatureVerifier = Signature.getInstance("SHA256withRSA").run {
                    initVerify(key)
                    update(encryptedTag)
                    verify(signature)
                }

                if(!signatureVerifier) {
                    Log.e("decodeAndShow", "Error")
                    return
                }
            }
        }
        catch (_: Exception) {
            return
        }

        val tag = ByteBuffer.wrap(clearTextTag)
        val tagId = tag.int
        val id = UUID(tag.long, tag.long)

        val euros = tag.short.toInt()
        val cents = tag.get().toInt()

        val nameLength = tag.get().toInt()
        val nameBytes = ByteArray(nameLength)
        tag.get(nameBytes)

        val name = String(nameBytes, StandardCharsets.ISO_8859_1)
        val newProduct = Product(id, name, euros, cents)

        listProducts.add(newProduct)
        (productListView.adapter as ProductAdapter).notifyDataSetChanged()

        updateTotal()
    }


    private fun generateCheckoutMessage(
        userId: UUID,
        products: List<Pair<UUID, Short>>, // Pair<productId, priceInCents>
        voucherId: UUID?,                  // pode ser null
        useDiscount: Boolean
    ): ByteArray? {
        try {
            val limitedProducts = products.take(10)

            // length of userId (16), number of products (1), for each product its id and price (16 + 2), use of discount (1) and voucherId (16)
            val len = 16 + 1 + limitedProducts.size * (16 + 2) + 1 + if (voucherId != null) 16 else 0

            val message = ByteBuffer.allocate(len).apply {
                putLong(userId.mostSignificantBits)
                putLong(userId.leastSignificantBits)
                put(limitedProducts.size.toByte())
                for ((id, price) in limitedProducts) {
                    putLong(id.mostSignificantBits)
                    putLong(id.leastSignificantBits)
                    putShort(price)
                }
                put(if (useDiscount) 1 else 0)
                voucherId?.let {
                    putLong(it.mostSignificantBits)
                    putLong(it.leastSignificantBits)
                }
            }.array()

            val activity = requireActivity() as MainActivity2
            val entry = activity.fetchEntryEC()

            val signature = Signature.getInstance(Crypto.EC_SIGN_ALGO).run {
                initSign(getPrivateKey(entry))
                update(message)
                sign()
            }

            return ByteBuffer.allocate(message.size + signature.size).apply {
                put(message)
                put(signature)
            }.array()

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}