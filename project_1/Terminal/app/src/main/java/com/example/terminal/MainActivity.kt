package com.example.terminal

import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.UUID
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.terminal.utils.NFC.READER_FLAGS
import com.example.terminal.fragments.ErrorFragment
import com.example.terminal.fragments.SuccessFragment
import com.example.terminal.utils.Lightness
import com.example.terminal.utils.dpToPx
import com.example.terminal.utils.setInsetsPadding
import com.example.terminal.utils.setStatusBarIconColor

/**
 * Main activity for handling NFC and QR code scanning.
 */
class MainActivity : AppCompatActivity() {

    // UI elements
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val tvContent by lazy { findViewById<TextView>(R.id.tv_content) }
    private val btClear by lazy { findViewById<Button>(R.id.bt_clear) }

    // NFC and scanner setup
    private val nfc by lazy { NfcAdapter.getDefaultAdapter(applicationContext) }
    private val nfcReader by lazy { NFCReader(::nfcReceived) }

    // QR code scan result handler
    private val scanCodeLauncher = registerForActivityResult(ScanContract()) {
        if (it != null && it.contents != null) {
            lifecycleScope.launch {
                showCheckoutMessage(it.contents.toByteArray(Charsets.ISO_8859_1))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setInsetsPadding(toolbar, top = dpToPx(-8f))
        setStatusBarIconColor(window, Lightness.LIGHT)

        tvContent.setText(R.string.tv_waiting)
        btClear.setOnClickListener { tvContent.setText(R.string.tv_waiting) }
    }

//    override fun onResume() {
//        super.onResume()
//        nfc.enableReaderMode(this, nfcReader, READER_FLAGS, null)
//    }

    /**
     * Enables NFC reader mode when activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) nfc.enableReaderMode(this, nfcReader, READER_FLAGS, null)
        else Log.d("NFC", "The device does not have NFC or is not activated.")
    }


//    override fun onPause() {
//        super.onPause()
//        nfc.disableReaderMode(this)
//    }

    /**
     * Disables NFC reader mode when activity is paused.
     */
    override fun onPause() {
        super.onPause()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) nfcAdapter.disableReaderMode(this)
        else Log.d("NFC", "The device does not have NFC (nothing to deactivate).")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mn_qr -> {
                scanOrderQR()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called when NFC data is received.
     * Triggers message parsing and UI update.
     */
    private fun nfcReceived(type: Int, content: ByteArray) {
        runOnUiThread {
            lifecycleScope.launch {
                showCheckoutMessage(content)
            }
        }
//        runOnUiThread {
//            when (type) {
//                1 -> {
//                    lifecycleScope.launch{
//                        showCheckoutMessage(content)
//                    }
//                }
//                2 ->
//                    lifecycleScope.launch{
//                    showCheckoutMessage(content)
//                }
//            }
//        }
    }

    /**
     * Launches QR code scanner.
     */
    private fun scanOrderQR() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        scanCodeLauncher.launch(options)
    }

    /**
     * Parses and displays order information from a byte array.
     * This function expects a specific binary structure in the `order` parameter, which it decodes in the following format:
     *
     * 1. 16 bytes: User ID (UUID - two longs)
     * 2. 1 byte: Number of products
     * 3. For each product:
     *    - 16 bytes: Product ID (UUID - two longs)
     *    - 2 bytes: Price (in cents, as Short)
     * 4. 1 byte: Discount flag (0 or 1)
     * 5. Optional 16 bytes: Voucher ID (UUID) if present
     * 6. Remaining bytes: Signature (byte array)
     *
     * @param order Byte array containing the encoded order data.
     */
    private suspend fun showCheckoutMessage(order: ByteArray) {
        val sb = StringBuilder()
        try {
            val bb: ByteBuffer = ByteBuffer.wrap(order)

            // Parse user ID from two long values
            val userIdMostSigBits = bb.long
            val userIdLeastSigBits = bb.long
            val userId = UUID(userIdMostSigBits, userIdLeastSigBits) // val userId = UUID(bb.long, bb.long)

            // Parse number of products
            val numberOfProducts = bb.get().toInt()

            // Parse each product's data
            val products = mutableListOf<Pair<UUID, Short>>()
            // for (i in 0 until numberOfProducts)
            (0 until numberOfProducts).forEach { i ->
                val productIdMostSigBits = bb.long
                val productIdLeastSigBits = bb.long
                val productId = UUID(productIdMostSigBits, productIdLeastSigBits) // val productId = UUID(bb.long, bb.long)
                val price = bb.short
                products.add(Pair(productId, price))
            }

            // Parse discount flag (1 means discount applied)
            val useDiscount = bb.get().toInt() == 1

            // Optionally parse voucher ID if available
            val voucherId: UUID? = if (bb.remaining() >= 16) {
                val voucherIdMostSigBits = bb.long
                val voucherIdLeastSigBits = bb.long
                UUID(voucherIdMostSigBits, voucherIdLeastSigBits)
            } else {
                null
            }

            // Extract the remaining bytes as the digital signature
            val signature = ByteArray(bb.remaining())
            bb.get(signature)

            // Build the display message
            sb.append("User ID: $userId\n")
            sb.append("Products: \n")
            for (product in products) {
                sb.append(" - Product ID: ${product.first}, Price: ${product.second / 100.0}€\n")
            }
            sb.append("Discount Applied: $useDiscount\n")
            voucherId?.let { sb.append("Voucher ID: $it\n") }
            val result = pay(order)

            Log.d("test",result)
            if(result == "True") {
                loadFragment(SuccessFragment())
            }
            else {
                loadFragment(ErrorFragment())
            }

        } catch (ex: Exception) {
            sb.append("Error in message processing: ${ex.message}")
        }

        tvContent.text = sb.toString()
    }

    /**
     * Method used to load a fragment into the Activity.
     *
     * @param fragment fragment to display
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
