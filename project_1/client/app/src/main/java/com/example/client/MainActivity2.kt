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
import com.example.client.utils.setInsetsPadding

/**
 * Activity used for login and register (with bottom bar)
 */
class MainActivity2 : AppCompatActivity() {

    private val toolbar by lazy {findViewById<Toolbar>(R.id.toolbar2)}

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
                Toast.makeText(this, "action_transactions", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_vouchers -> {
                Toast.makeText(this, "action_vouchers", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_shopping, fragment)
            .addToBackStack(null)
            .commit()
    }
}