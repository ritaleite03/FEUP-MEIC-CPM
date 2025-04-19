package com.example.generator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.generator.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.generator.Crypto.CRYPTO_ENC_ALGO
import com.example.generator.Crypto.CRYPTO_NAME
import com.example.generator.Crypto.CRYPTO_SIGN_ALGO
import com.example.generator.Crypto.CRYPTO_TAG_ID
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.UUID
import javax.crypto.Cipher

/**
 * Main activity responsible for generating cryptographic keys and encrypting data.
 * It manages key generation, user input, and encryption of tags to be sent to a second activity.
 */
class MainActivity : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar)}
    private var privateKey: PrivateKey? = null
    private var publicKey: PublicKey? = null

    // lazily retrieves the PrivateKeyEntry from the Android Keystore if it exists.
    private var entry: KeyStore.PrivateKeyEntry? = null
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

        // generate keys if they don't exist
        if (entry == null) defineKeys(true)
        else defineKeys(false)

        lifecycleScope.launch {
            val groceries = JSONObject(getGroceries())
            setupRecyclerView(groceries)
        }

        // set up button click listener to capture user input and encrypt data
//        button.setOnClickListener {
//            var uuid = UUID.randomUUID()
//            var name = findViewById<TextInputEditText>(R.id.input_name).text.toString()
//            var euro = findViewById<TextInputEditText>(R.id.input_euros).text.toString()
//            var cent = findViewById<TextInputEditText>(R.id.input_cents).text.toString()
//
//            if (!name.isEmpty() && !euro.isEmpty() && !cent.isEmpty()) {
//                var encryptedTag = generateTag(uuid, name, euro, cent)
//                startActivity(Intent(this, MainActivity2::class.java).apply {
//                    putExtra("data", encryptedTag)
//                })
//            }
//        }

    }

    private fun setupRecyclerView(groceries: JSONObject) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GroceryAdapter(parseGroceries(groceries), this)
        recyclerView.adapter = adapter
    }

    private fun parseGroceries(jsonObject: JSONObject): List<Grocery> {
        val groceryList = mutableListOf<Grocery>()
        val groceriesArray = jsonObject.getJSONArray("groceries")

        for (i in 0 until groceriesArray.length()) {
            val item = groceriesArray.getJSONObject(i)

            val grocery = Grocery(
                name = item.getString("Name"),
                category = item.getString("Category"),
                subCategory = item.getString("SubCategory"),
                description = item.getString("Description"),
                price = item.getString("Price").toFloat(),
                imagePath = item.getString("ImagePath")
            )

            groceryList.add(grocery)
        }

        return groceryList
    }

    /**
     * Defines the keys by either generating them or loading them from the keystore.
     *
     * @param generate Boolean flag indicating whether to generate new keys or load existing ones.
     */
    private fun defineKeys(generate : Boolean){
        if (generate) {
            generateKeys()
        }
        privateKey = entry?.privateKey
        publicKey = entry?.certificate?.publicKey
        lifecycleScope.launch {
            informServer(publicKey)
        }
    }

    /**
     * Generates a cryptographic tag from the provided data (UUID, name, euros, cents).
     * The tag is then encrypted using the private key stored in the Android Keystore.
     *
     * @param uuid UUID of the tag.
     * @param name Name associated with the tag.
     * @param euro Amount of euros.
     * @param cent Amount of cents.
     * @return The encrypted tag as a byte array, or null if encryption fails.
     */
    private fun generateTag(uuid : UUID, name : String, euro : String, cent : String) : ByteArray? {
        val subName = if (name.length > 29) name.substring(0, 29) else name
        // length of (tagID, UUID, euros(short), cents(byte), nr_bytes(name)(byte), name)
        val len = 4 + 16 + 2 + 1 + 1 + subName.length

        val tag = ByteBuffer.allocate(len).apply {
            putInt(CRYPTO_TAG_ID)
            putLong(uuid.mostSignificantBits)
            putLong(uuid.leastSignificantBits)
            putShort(euro.toShort())
            put(cent.toByte())
            put(subName.length.toByte())
            put(subName.toByteArray(StandardCharsets.ISO_8859_1))
        }

        try {
            val encryptedTag = Cipher.getInstance(CRYPTO_ENC_ALGO).run {
                init(Cipher.ENCRYPT_MODE, getPrivateKey(entry))
                doFinal(tag.array())
            }

            val signature = Signature.getInstance(CRYPTO_SIGN_ALGO).run {
                initSign(getPrivateKey(entry))
                update(encryptedTag)
                sign()
            }

            val combined = ByteBuffer.allocate(encryptedTag.size + signature.size).apply {
                put(encryptedTag)
                put(signature)
            }.array()

            return combined
        }
        catch (_: Exception) {
            return null
        }
    }
}