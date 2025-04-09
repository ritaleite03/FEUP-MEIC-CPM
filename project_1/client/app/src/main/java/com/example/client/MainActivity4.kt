package com.example.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.client.utils.Card
import com.example.client.utils.Crypto
import android.util.Log
import com.example.client.utils.Crypto.ACTION_CARD_DONE

class MainActivity4 : AppCompatActivity() {
    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            Toast.makeText(this@MainActivity4, "NFC link lost", Toast.LENGTH_LONG).show()
            finish()
        }
//            override fun onReceive(ctx: Context, intent: Intent) {
//                Toast.makeText(this@MainActivity4, "NFC link lost", Toast.LENGTH_SHORT).show()
//                Handler(Looper.getMainLooper()).postDelayed({
//                    finish()
//                }, 1000)
//            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main4)
    }

    override fun onResume() {
        super.onResume()
        Card.contentMessage = intent.getByteArrayExtra("data") ?: ByteArray(0)       // message to send via card emulation
        Card.type = 2                           // type of message (1: list, 2: key)

        Log.d("test", (byteArrayOf(Card.type.toByte()) + Card.contentMessage + Card.OK_SW).size.toString())
        val intentFilter = IntentFilter(ACTION_CARD_DONE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)  // to receive 'link loss'
    }

    override fun onPause() {
        super.onPause()
        Card.type = 0  // allow sending only when this Activity is running
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)
    }
}