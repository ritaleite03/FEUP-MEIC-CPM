package com.example.client

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.client.utils.setInsetsPadding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.nio.charset.StandardCharsets
import java.util.Hashtable
import kotlin.collections.set
import androidx.core.graphics.createBitmap
import com.example.client.domain.productsDB

/**
 * Activity that handles the QR code scanning.
 */
class MainActivity3 : AppCompatActivity() {

    // UI elements
    private val qrCodeImageview by lazy { findViewById<ImageView>(R.id.img_qr_code) }
    private val tvError by lazy { findViewById<TextView>(R.id.tv_error) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main3)
        setInsetsPadding(tvError, bottom=0)

        var btnBack = findViewById<Button>(R.id.btn_go_back)
        btnBack.setOnClickListener { finish() }

        var btnConclude = findViewById<Button>(R.id.btn_conclude)
        btnConclude.setOnClickListener {
            productsDB.deleteAll()
            val intent = Intent(this, MainActivity2::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            this.finish()
        }

        // get the encrypted data passed from the previous activity
        val content = intent.getByteArrayExtra("data")!!
        val qrContent = String(content, StandardCharsets.ISO_8859_1)

        // create the QR code in a separate thread to avoid ANR (Application Not Responding)
        Thread {
            try {
                val bitmap = encodeAsBitmap(qrContent)
                runOnUiThread {
                    qrCodeImageview.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                // display error if something goes wrong
                tvError.text = e.message
                Log.d("Error", e.message.toString())
            }
        }.start()
    }

    /**
     * Converts the given string into a QR code and returns a Bitmap representing the generated QR code.
     *
     * @param str The text to be encoded into the QR code.
     * @return The generated QR code as a Bitmap, or `null` if an error occurs during generation.
     */
    private fun encodeAsBitmap(str: String): Bitmap? {
        // define the dimensions of the QR code (1000x1000 pixels)
        val dimension = 1000

        // define encoding parameters, including the character set used
        val hints = Hashtable<EncodeHintType, String>().also {
            it[EncodeHintType.CHARACTER_SET] = "ISO-8859-1"
        }

        // try to generate the QR code from the provided text
        val result = try {
            MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, dimension, dimension, hints)
        } catch (iae: IllegalArgumentException) {
            tvError.text = iae.message
            return null
        }

        // convert the result to a Bitmap
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
            }
        }

        // create and return the Bitmap
        val bitmap = createBitmap(w, h)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}