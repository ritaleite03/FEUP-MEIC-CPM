package com.example.client

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.client.fragments.authentication.LoginFragment
import com.example.client.fragments.authentication.RegisterFragment
import com.example.client.utils.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.client.utils.Crypto.CRYPTO_EC_NAME
import com.example.client.utils.Crypto.CRYPTO_RSA_NAME
import com.example.client.utils.setInsetsPadding
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry

/**
 * Activity used for user login and registration, without the bottom navigation bar.
 *
 * This activity manages the authentication and registration flow, displaying the appropriate fragments depending on the user's authentication state.
 * If the user is already registered, the login fragment will be displayed; otherwise the register fragment will be displayed.
 * Additionally, the activity handles loading encryption keys from the Android Keystore (for RSA and EC).
 */
class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)

        // check if the username is already saved in the shared preferences (SharedPreferences)
        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", null)

        // if the name is empty, it displays the register fragment
        if (name.isNullOrEmpty()) {
            loadFragment(RegisterFragment())
        }

        // if the name is present, it displays the login fragment
        else{
            loadFragment(LoginFragment())
        }
    }

    /**
     * Method used to load a fragment into the Activity.
     *
     * @param fragment fragment to display
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_main, fragment)
            .addToBackStack(null)
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