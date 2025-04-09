package com.example.generator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.UUID
import javax.crypto.Cipher
import kotlin.text.isEmpty

/**
 * Main activity responsible for generating cryptographic keys and encrypting data.
 * It manages key generation, user input, and encryption of tags to be sent to a second activity.
 */
class MainActivity : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar)}
    private val button by lazy {findViewById<Button>(R.id.button)}
    private var privateKey: PrivateKey? = null
    private var publicKey: PublicKey? = null

    // lazily retrieves the PrivateKeyEntry from the Android Keystore if it exists.
    private var entry: KeyStore.PrivateKeyEntry? = null
        get() {
            if (field == null)
                field = KeyStore.getInstance(Crypto.ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(Crypto.NAME, null) as KeyStore.PrivateKeyEntry?
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
        if (entry == null) {
            Log.d("DEBUG", "if")
            defineKeys(true)
        }
        else {
            Log.d("DEBUG", "else")
            defineKeys(false)
        }

        // set up button click listener to capture user input and encrypt data
        button.setOnClickListener {
            var uuid = UUID.randomUUID()
            var name = findViewById<TextInputEditText>(R.id.input_name).text.toString()
            var euro = findViewById<TextInputEditText>(R.id.input_euros).text.toString()
            var cent = findViewById<TextInputEditText>(R.id.input_cents).text.toString()

            if (!name.isEmpty() && !euro.isEmpty() && !cent.isEmpty()) {
                var encryptedTag = generateTag(uuid, name, euro, cent)
                startActivity(Intent(this, MainActivity2::class.java).apply {
                    putExtra("data", encryptedTag)
                })
            }
        }
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
        var subName = if (name.length > 29) name.substring(0, 29) else name
        // length of (tagID, UUID, euros(short), cents(byte), nr_bytes(name)(byte), name)
        val len = 4 + 16 + 2 + 1 + 1 + subName.length

        val tag = ByteBuffer.allocate(len).apply {
            putInt(Crypto.tagId)
            putLong(uuid.mostSignificantBits)
            putLong(uuid.leastSignificantBits)
            putShort(euro.toShort())
            put(cent.toByte())
            put(subName.length.toByte())
            put(subName.toByteArray(StandardCharsets.ISO_8859_1)) // 1 byte per char without code translation
        }

        try {
            var encryptedTag = Cipher.getInstance(Crypto.ENC_ALGO).run {
                init(Cipher.ENCRYPT_MODE, getPrivateKey(entry))
                doFinal(tag.array())
            }

            var signature = Signature.getInstance(Crypto.SIGN_ALGO).run {
                initSign(getPrivateKey(entry))
                update(encryptedTag)
                sign()
            }

            var combined = ByteBuffer.allocate(encryptedTag.size + signature.size).apply {
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