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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.client.utils.Card
import com.example.client.utils.NFC.NFC_ACTION_CARD_DONE

/**
 * This activity handles the NFC communication.
 */
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
        Card.contentMessage = intent.getByteArrayExtra("data") ?: ByteArray(0)
        Card.type = 2
        val intentFilter = IntentFilter(NFC_ACTION_CARD_DONE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        Card.type = 0
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiver)
    }
}