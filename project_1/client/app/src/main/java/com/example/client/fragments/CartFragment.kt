package com.example.client.fragments

import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import com.example.client.MainActivity2
import com.example.client.MainActivity3
import com.example.client.MainActivity4
import com.example.client.R
import com.example.client.base64ToPublicKey
import com.example.client.getPrivateKey
import com.example.client.utils.Crypto.CRYPTO_EC_SIGN_ALGO
import com.example.client.utils.Crypto.CRYPTO_RSA_ENC_ALGO
import com.example.client.utils.Crypto.CRYPTO_RSA_KEY_SIZE
import com.example.client.utils.Crypto.CRYPTO_RSA_SIGN_ALGO
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
            openPaymentSelection()
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

        val numberBytes = CRYPTO_RSA_KEY_SIZE / 8
        val totalSize = numberBytes * 2

        if (combined.size < totalSize) return

        val encryptedTag = combined.copyOfRange(0, numberBytes)
        val signature = combined.copyOfRange(numberBytes, numberBytes + numberBytes)

        try {
            val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
            val keyString: String? = sharedPreferences.getString("key", null)

            if (keyString != null) {
                val key = base64ToPublicKey(keyString)

                clearTextTag = Cipher.getInstance(CRYPTO_RSA_ENC_ALGO).run {
                    init(Cipher.DECRYPT_MODE, key)
                    doFinal(encryptedTag)
                }

                val signatureVerifier = Signature.getInstance(CRYPTO_RSA_SIGN_ALGO).run {
                    initVerify(key)
                    update(encryptedTag)
                    verify(signature)
                }

                if(!signatureVerifier) return
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

    /**
     * Generates a checkout message encoded as a ByteArray for payment processing.
     *
     * The generated message includes the following:
     * - User ID (UUID)
     * - A list of products, each with an ID (UUID) and price (Short in cents)
     * - Whether a discount is applied (Boolean)
     * - An optional voucher ID (UUID)
     * - A digital signature (to be added later in the process)
     *
     * @param userId The UUID representing the user's ID.
     * @param products A list of pairs, each containing a product's UUID and its price (in cents).
     * @param voucherId An optional UUID representing the voucher ID (if available).
     * @param useDiscount A boolean flag indicating whether a discount is applied.
     *
     * @return ByteArray representing the generated checkout message. Returns `null` if there is an error during generation.
     */
    private fun generateCheckoutMessage(
        userId: UUID,
        products: List<Pair<UUID, Short>>,
        voucherId: UUID?,
        useDiscount: Boolean
    ): ByteArray? {
        try {
            val limitedProducts = products.take(10)
            val dataLen = 16 + 1 + limitedProducts.size * (16 + 2) + 1 + if (voucherId != null) 16 else 0
            val message = ByteArray(dataLen)

            ByteBuffer.wrap(message, 0, dataLen).apply {
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
            }

            val activity = requireActivity() as MainActivity2
            val entry = activity.fetchEntryEC()

            val signature = Signature.getInstance(CRYPTO_EC_SIGN_ALGO).run {
                initSign(getPrivateKey(entry))
                update(message)
                sign()
            }

            val sigLen = signature.size
            val finalMessage = ByteArray(dataLen + sigLen)
            System.arraycopy(message, 0, finalMessage, 0, dataLen)
            System.arraycopy(signature, 0, finalMessage, dataLen, sigLen)
            return finalMessage

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun openPaymentSelection() {
        val dialogView = layoutInflater.inflate(R.layout.popup_payment, null)

        val dialog = AlertDialog.Builder(this.requireActivity()).setView(dialogView).create()

        val btnQRC: Button = dialogView.findViewById(R.id.btn_option1)
        val btnNFC: Button = dialogView.findViewById(R.id.btn_option2)
        val btnClose: Button = dialogView.findViewById(R.id.btn_close)

        btnQRC.setOnClickListener {
            dialog.dismiss()
            redirectPaymentSelection(MainActivity3::class.java)
        }

        btnNFC.setOnClickListener {
            redirectPaymentSelection(MainActivity4::class.java)
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun redirectPaymentSelection(activityType : Class<out Activity>){
        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences",
            Context.MODE_PRIVATE
        )
        val uuid = sharedPreferences.getString("uuid", null)
        val products: List<Pair<UUID, Short>> = listProducts.map { product ->
            product.id to (product.euros * 100 + product.cents).toInt().toShort()
        }
        if (uuid != null && !products.isEmpty()) {
            var encryptedTag = generateCheckoutMessage(UUID.fromString(uuid), products, null, false)
            startActivity(Intent(this.requireActivity(), activityType).apply {
                putExtra("data", encryptedTag)
            })
        }
    }
}