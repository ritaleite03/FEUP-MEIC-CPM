package com.example.client

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.client.fragments.authentication.LoginFragment
import com.example.client.fragments.authentication.RegisterFragment
import com.example.client.utils.Crypto
import com.example.client.utils.setInsetsPadding
import java.security.KeyStore

/**
 * Activity used for login and register (without bottom bar)
 */
class MainActivity : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar_main)}

    // keys
    private var entryEC: KeyStore.Entry? = null // getting a keystore entry (with KeyName) lazily
        get() {
            if (field == null) {
                field = KeyStore.getInstance(Crypto.ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(Crypto.EC_NAME, null)
                }
            }
            return field
        }
    private var entryRSA: KeyStore.Entry? = null    // getting a keystore entry (with KeyName) lazily
        get() {
            if (field == null) {
                field = KeyStore.getInstance(Crypto.ANDROID_KEYSTORE).run {
                    load(null)
                    getEntry(Crypto.RSA_NAME, null)
                }
            }
            return field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setInsetsPadding(toolbar, top = 0)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", null)

        // register
        if (name.isNullOrEmpty()) {
            loadFragment(RegisterFragment())
        }

        // login
        else{
            loadFragment(LoginFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_main, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun fetchEntryEC() : KeyStore.Entry?{
        return entryEC
    }

    fun fetchEntryRSA() : KeyStore.Entry?{
        return entryRSA
    }
}