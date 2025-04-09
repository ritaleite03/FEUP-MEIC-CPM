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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.RSAPublicKeySpec
import java.util.UUID
import android.util.Log

private var pubKey: PublicKey? = null         // will hold the public key (as long as the app is in memory)
private const val keysize = 512               // the public key will come with 512 bits
const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

fun hexStringToByteArray(s: String): ByteArray {
    val data = ByteArray(s.length/2)
    for (k in 0 until s.length/2)
        data[k] = ((Character.digit(s[2*k], 16) shl 4) + Character.digit(s[2*k+1], 16)).toByte()
    return data
}

class MainActivity : AppCompatActivity() {
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val tvContent by lazy { findViewById<TextView>(R.id.tv_content) }
    private val btClear by lazy { findViewById<Button>(R.id.bt_clear) }

    private val nfc by lazy { NfcAdapter.getDefaultAdapter(applicationContext) }
    private val nfcReader by lazy { NFCReader(::nfcReceived) }
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
        setInsetsPadding(toolbar, top=dpToPx(-8f))
        setStatusBarIconColor(window, Lightness.LIGHT)
        tvContent.setText(R.string.tv_waiting)
        btClear.setOnClickListener { tvContent.setText(R.string.tv_waiting) }
    }

//    override fun onResume() {
//        super.onResume()
//        nfc.enableReaderMode(this, nfcReader, READER_FLAGS, null)
//    }

    override fun onResume() {
        super.onResume()

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            val options = Bundle()
            nfcAdapter.enableReaderMode(
                this,
                NFCReader { type, data ->
                    // trata os dados recebidos
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        } else {
            Log.d("NFC", "Dispositivo sem NFC. Modo NFC não ativado.")
            // Se quiseres, podes esconder botões ou avisar o utilizador aqui
        }
    }


//    override fun onPause() {
//        super.onPause()
//        nfc.disableReaderMode(this)
//    }

    override fun onPause() {
        super.onPause()

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this)
        } else {
            Log.d("NFC", "Dispositivo sem NFC. Nada para desativar.")
        }
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

    private fun nfcReceived(type: Int, content: ByteArray) {
        runOnUiThread {
            when (type) {
                1 -> {
                    lifecycleScope.launch{
                        showCheckoutMessage(content)
                    }
                }
                2 ->
                    lifecycleScope.launch{
                    showCheckoutMessage(content)
                }
            }
        }
    }

    // reading with a QR code (menu option)
    private fun scanOrderQR() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        scanCodeLauncher.launch(options)
    }


    // when receiving a list (comes with a signature)
    private suspend fun showCheckoutMessage(order: ByteArray) {
        Log.d("test", "showCheckoutMessage")
        val sb = StringBuilder()

        try {
            val bb: ByteBuffer = ByteBuffer.wrap(order)

            val userIdMostSigBits = bb.long
            val userIdLeastSigBits = bb.long
            val userId = UUID(userIdMostSigBits, userIdLeastSigBits)

            val numberOfProducts = bb.get().toInt()

            val products = mutableListOf<Pair<UUID, Short>>()
            for (i in 0 until numberOfProducts) {
                val productIdMostSigBits = bb.long
                val productIdLeastSigBits = bb.long
                val price = bb.short
                val productId = UUID(productIdMostSigBits, productIdLeastSigBits)
                products.add(Pair(productId, price))
            }

            val useDiscount = bb.get().toInt() == 1

            val voucherId: UUID? = if (bb.remaining() >= 16) {
                val voucherIdMostSigBits = bb.long
                val voucherIdLeastSigBits = bb.long
                UUID(voucherIdMostSigBits, voucherIdLeastSigBits)
            } else {
                null
            }

            val signature = ByteArray(bb.remaining())
            bb.get(signature)

            sb.append("User ID: $userId\n")
            sb.append("Products: \n")
            for (product in products) {
                sb.append(" - Produto ID: ${product.first}, Preço: ${product.second / 100.0}€\n")
            }
            sb.append("Desconto aplicado: $useDiscount\n")
            voucherId?.let { sb.append("Voucher ID: $it\n") }

            val result = pay(order)
            sb.append(result)

        } catch (ex: Exception) {
            sb.append("Erro ao processar a mensagem: ${ex.message}")
        }

        //val result = pay(order)
        //sb.append(result)
        tvContent.text = sb.toString()
    }

}
