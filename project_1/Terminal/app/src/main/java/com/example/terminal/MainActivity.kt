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

private var pubKey: PublicKey? = null         // will hold the public key (as long as the app is in memory)
private const val keysize = 512               // the public key will come with 512 bits
const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
private val products = arrayOf(                       // the types of products (the first is type 1)
    "Oranges",
    "Mandarins",
    "Peaches",
    "Pears",
    "Apples",
    "Pineapples",
    "Plums",
    "Grapes"
)

/* Utility top-level function */
//fun byteArrayToHex(ba: ByteArray): String {
//    val sb = StringBuilder(ba.size * 2)
//    for (b in ba) sb.append(String.format("%02x", b))
//    return sb.toString()
//}

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

    override fun onResume() {
        super.onResume()
        nfc.enableReaderMode(this, nfcReader, READER_FLAGS, null)
    }

    override fun onPause() {
        super.onPause()
        nfc.disableReaderMode(this)
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

    // callback to receive a key or product list from the NFC reader
    private fun nfcReceived(type: Int, content: ByteArray) {
        runOnUiThread {
            when (type) {
                1 -> showAndStoreKey(content)
                2 ->
                    lifecycleScope.launch {
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

    // when receiving a public key
    private fun showAndStoreKey(modulus: ByteArray) {
        var error = ""
        try {
            val keyRSAPub = RSAPublicKeySpec(BigInteger(modulus), BigInteger("65537"))   // the key raw values (as BigIntegers) are used to build an appropriate KeySpec
            pubKey = KeyFactory.getInstance("RSA").generatePublic(keyRSAPub)         // to build a key object we need a KeyFactory object
        }
        catch (ex: Exception) {
            error = ex.toString()
        }
        val sb = StringBuilder("Public Key:\nModulus (")
            .append(modulus.size)
            .append("):\n")
            .append(byteArrayToHex(modulus))
            .append("\nExponent: 010001\n\n")
            .append(error)
        tvContent.text = sb.toString()               // show the raw values of the key components (in hex)
    }

    // when receiving a list (comes with a signature)
    private suspend fun showCheckoutMessage(order: ByteArray) {
        var error = ""
        var validated = false
        val sb = StringBuilder()

        try {
            // Estrutura esperada
            val bb: ByteBuffer = ByteBuffer.wrap(order)

            // Primeiro, lemos o userId (16 bytes)
            val userIdMostSigBits = bb.long
            val userIdLeastSigBits = bb.long
            val userId = UUID(userIdMostSigBits, userIdLeastSigBits)

            // Número de produtos (1 byte)
            val numberOfProducts = bb.get().toInt()

            // Lista de produtos (id, preço)
            val products = mutableListOf<Pair<UUID, Short>>()
            for (i in 0 until numberOfProducts) {
                val productIdMostSigBits = bb.long
                val productIdLeastSigBits = bb.long
                val price = bb.short
                val productId = UUID(productIdMostSigBits, productIdLeastSigBits)
                products.add(Pair(productId, price))
            }

            // Se o desconto foi aplicado (1 byte)
            val useDiscount = bb.get().toInt() == 1

            // Se houver um voucherId (16 bytes)
            val voucherId: UUID? = if (bb.remaining() >= 16) {
                val voucherIdMostSigBits = bb.long
                val voucherIdLeastSigBits = bb.long
                UUID(voucherIdMostSigBits, voucherIdLeastSigBits)
            } else {
                null
            }

            // Agora lemos a assinatura (assumindo que a assinatura está no final da mensagem)
            val signature = ByteArray(bb.remaining())
            bb.get(signature)

//            // Validação da assinatura
//            val activity = requireActivity() as MainActivity2
//            val entry = activity.fetchEntryEC()
//            val pubKey = entry?.certificate?.publicKey  // Obtém a chave pública da atividade ou keystore
//
//            // Verificar a assinatura
//            try {
//                validated = Signature.getInstance(Crypto.EC_SIGN_ALGO).run {
//                    initVerify(pubKey)
//                    update(order.copyOfRange(0, order.size - signature.size)) // A parte sem a assinatura
//                    verify(signature)
//                }
//            } catch (ex: Exception) {
//                error = "Erro na validação da assinatura: ${ex.message}"
//            }

            // Exibir a informação lida
            sb.append("User ID: $userId\n")
            sb.append("Products: \n")
            for (product in products) {
                sb.append(" - Produto ID: ${product.first}, Preço: ${product.second / 100.0}€\n")
            }
            sb.append("Desconto aplicado: $useDiscount\n")
            voucherId?.let { sb.append("Voucher ID: $it\n") }
            sb.append("\nValidação da assinatura: $validated")
            sb.append("\nErro: $error")

        } catch (ex: Exception) {
            sb.append("Erro ao processar a mensagem: ${ex.message}")
        }

        // Exibe a mensagem na UI
        val result = pay(order)
        sb.append(result)

        tvContent.text = sb.toString()

    }

}
