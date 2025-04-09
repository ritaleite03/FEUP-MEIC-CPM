package com.example.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.client.utils.Card
import com.example.client.utils.Crypto

class MainActivity4 : AppCompatActivity() {
    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            Toast.makeText(this@MainActivity4, "NFC link lost", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main4)
    }

    override fun onResume() {
        super.onResume()
        Card.contentMessage = intent.getByteArrayExtra("message") ?: ByteArray(0)       // message to send via card emulation
        Card.type = intent.getIntExtra("valueType", 0)                            // type of message (1: list, 2: key)
        val intentFilter = IntentFilter(Crypto.ACTION_CARD_DONE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)  // to receive 'link loss'
    }

    override fun onPause() {
        super.onPause()
        Card.type = 0  // allow sending only when this Activity is running
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)
    }
}