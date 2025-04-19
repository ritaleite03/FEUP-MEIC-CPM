package com.example.generator

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.generator.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.generator.Crypto.CRYPTO_NAME
import com.example.generator.Grocery.Companion.parseGroceries
import com.example.generator.utils.setInsetsPadding
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

/**
 * Main activity responsible for generating cryptographic keys and encrypting data.
 * It manages key generation, user input, and encryption of tags to be sent to a second activity.
 */
class MainActivity : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar)}
    private var privateKey: PrivateKey? = null
    private var publicKey: PublicKey? = null

    // lazily retrieves the PrivateKeyEntry from the Android Keystore if it exists.
    var entry: KeyStore.PrivateKeyEntry? = null
        get() {
            if (field == null)
                field = KeyStore.getInstance(CRYPTO_ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(CRYPTO_NAME, null) as KeyStore.PrivateKeyEntry?
                }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setInsetsPadding(toolbar, top = 0)

        Log.d("entry", entry.toString())

        // generate keys if they don't exist
        val (pub, priv) = if (entry == null) { defineKeys(true, entry) }
        else { defineKeys(false, entry) }
        publicKey = pub
        privateKey = priv

        Log.d("pub", publicKey.toString())
        Log.d("priv", privateKey.toString())

        lifecycleScope.launch {
            informServer(publicKey)
            val groceries = JSONObject(getGroceries())
            setupRecyclerView(groceries)
        }
    }

    private fun setupRecyclerView(groceries: JSONObject) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GroceryAdapter(parseGroceries(groceries), this)
        recyclerView.adapter = adapter
    }

}