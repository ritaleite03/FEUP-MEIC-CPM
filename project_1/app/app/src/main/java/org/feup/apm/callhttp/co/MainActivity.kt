package org.feup.apm.callhttp.co

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
  private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
  private val inside by lazy { findViewById<LinearLayout>(R.id.inside) }
  private val tvResponse by lazy { findViewById<TextView>(R.id.tv_response) }
  // server
  private val edtIp by lazy { findViewById<EditText>(R.id.edt_IP) }
  private val edtPort by lazy { findViewById<EditText>(R.id.edt_Port)}
  // user
  private val edtName by lazy { findViewById<EditText>(R.id.edt_name) }
  private val edtNick by lazy { findViewById<EditText>(R.id.edt_nick) }
  private val edtPass by lazy { findViewById<EditText>(R.id.edt_pass) }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
    setStatusBarIconColor(window, Lightness.LIGHT)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    setInsetsPadding(toolbar, top=dpToPx(-8f), left=0, right=0)
    setInsetsPadding(inside, left=dpToPx(5f), right=dpToPx(5f))
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.getuser -> {
        lifecycleScope.launch {
          val result = getUser(this@MainActivity, edtIp.text.toString(), edtPort.text.toString().toInt(), edtName.text.toString(), edtNick.text.toString(), edtPass.text.toString())
          appendResponse(result)
        }
        return true
      }
      R.id.adduser -> {
        lifecycleScope.launch {
          val result = addUser(this@MainActivity, edtIp.text.toString(), edtPort.text.toString().toInt(), edtName.text.toString(), edtNick.text.toString(), edtPass.text.toString())
          appendResponse(result)
        }
        return true
      }
      R.id.clear -> {
        tvResponse.setText(R.string.tv_start_value)
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  suspend fun appendResponse(value: String) {
    withContext(Dispatchers.Main) {
      val newValue = "${tvResponse.text}\n$value"
      tvResponse.text = newValue
    }
  }

  suspend fun setResponse(value: String) {
    withContext(Dispatchers.Main) {
      tvResponse.text = value
    }
  }
}