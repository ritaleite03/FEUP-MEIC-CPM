package com.example.client

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.client.fragments.VouchersFragment
import com.example.client.fragments.transactions.TransactionsFragment
import com.example.client.utils.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.client.utils.Crypto.CRYPTO_EC_NAME
import com.example.client.utils.Crypto.CRYPTO_RSA_NAME
import com.example.client.utils.setInsetsPadding
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry

/**
 * Activity used for the user's main navigation, with the bottom navigation bar.
 *
 * This activity contains the logic to display an options menu on the Toolbar and navigate between main content fragments.
 * It is designed to be used after user login or registration.
 * The layout includes a custom toolbar and a container to load content fragments.
 */
class MainActivity2 : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar2)}

    // Android Keystore EC Key
    private var entryEC: PrivateKeyEntry? = null // getting a keystore entry (with KeyName) lazily
        get() {
            if (field == null) {
                field = KeyStore.getInstance(CRYPTO_ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(CRYPTO_EC_NAME, null) as PrivateKeyEntry
                }
            }
            return field
        }

    // Android Keystore RSA Key
    private var entryRSA: PrivateKeyEntry? = null // getting a keystore entry (with KeyName) lazily
        get() {
            if (field == null) {
                field = KeyStore.getInstance(CRYPTO_ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(CRYPTO_RSA_NAME, null) as PrivateKeyEntry
                }
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        setInsetsPadding(toolbar, top = 0)
        toolbar.overflowIcon?.setTint(resources.getColor(R.color.white, theme))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                Toast.makeText(this, "action_profile", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_transactions -> {
                loadFragment(TransactionsFragment())
                true
            }
            R.id.action_vouchers -> {
                loadFragment(VouchersFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Method used to load a fragment into the Activity.
     *
     * @param fragment fragment to display
     */
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_shopping, fragment)
            //.addToBackStack(null)
            .commit()
    }

    /**
     * Returns the EC (Elliptic Curve) key entry from the Android Keystore.
     *
     * @return EC key entry, or null if not found.
     */
    fun fetchEntryEC() : PrivateKeyEntry?{
        return entryEC
    }

    /**
     * Returns the RSA key entry from the Android Keystore.
     *
     * @return RSA key entry, or null if not found.
     */
    fun fetchEntryRSA() : PrivateKeyEntry?{
        return entryRSA
    }
}