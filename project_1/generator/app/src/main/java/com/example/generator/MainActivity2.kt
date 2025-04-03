package com.example.generator

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.nio.charset.StandardCharsets
import java.util.Hashtable
import kotlin.collections.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

class MainActivity2 : AppCompatActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar2) }
    private val qrCodeImageview by lazy { findViewById<ImageView>(R.id.img_qr_code) }
    private val tvError by lazy { findViewById<TextView>(R.id.tv_error) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        setInsetsPadding(toolbar, top = dpToPx(-8f))
        setInsetsPadding(tvError, bottom=0)
        val content = intent.getByteArrayExtra("data")!!
        val qrContent = String(content, StandardCharsets.ISO_8859_1)
        Thread {                         // do the creation in a new thread to avoid ANR Exception
            try {
                val bitmap = encodeAsBitmap(qrContent)
                runOnUiThread {              // runOnUiThread method used to do UI task in main thread
                    qrCodeImageview.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                tvError.text = e.message
            }
        }.start()
    }

    private fun encodeAsBitmap(str: String): Bitmap? {
        val DIMENSION = 1000

        val hints = Hashtable<EncodeHintType, String>().also {
            it[EncodeHintType.CHARACTER_SET] = "ISO-8859-1"
        }
        val result = try {
            MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, DIMENSION, DIMENSION, hints)
        } catch (iae: IllegalArgumentException) {
            tvError.text = iae.message
            return null
        }
        val w = result.getWidth()
        val h = result.getHeight()
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) Color.BLUE else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}