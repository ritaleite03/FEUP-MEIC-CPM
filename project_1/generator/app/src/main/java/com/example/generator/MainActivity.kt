package com.example.generator

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.generator.utils.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.generator.utils.Crypto.CRYPTO_NAME
import com.example.generator.Grocery.Companion.parseGroceries
import com.example.generator.utils.configuratorToolbarTitle
import com.example.generator.utils.dpToPx
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

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val searchField by lazy { findViewById<EditText>(R.id.searchField) }
    private val categorySpinner by lazy { findViewById<Spinner>(R.id.categorySpinner) }
    private val sortSpinner by lazy { findViewById<Spinner>(R.id.sortSpinner) }
    private lateinit var adapter: GroceryAdapter
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
        setInsetsPadding(toolbar, top = dpToPx(-8f))

        configuratorToolbarTitle(this, toolbar)

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("All", "Fruit", "Vegetables", "Packages", "Dessert")
        )

        sortSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("None", "Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)")
        )

        // generate keys if they don't exist
        if (entry == null) {
            generateKeys()
        }

        publicKey = getPublicKey(entry)
        privateKey = getPrivateKey(entry)


        lifecycleScope.launch {
            informServer(publicKey)
            val groceries = JSONObject(getGroceries())
            setupRecyclerView(groceries)
        }
    }

    /**
     * Sets up the RecyclerView with the provided groceries data.
     *
     * @param groceries The groceries data to display in the RecyclerView.
     */
    private fun setupRecyclerView(groceries: JSONObject) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GroceryAdapter(parseGroceries(groceries), this)
        recyclerView.adapter = adapter

        setupFilters()
    }

    /**
     * Sets up filtering functionality.
     */
    private fun setupFilters() {
        val applyFilters = {
            val search = searchField.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val sort = sortSpinner.selectedItem.toString()
            adapter.applyFilter(search, category, sort)
        }

        searchField.addTextChangedListener { applyFilters() }

        categorySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        sortSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

}