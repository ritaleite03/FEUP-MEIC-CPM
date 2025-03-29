package org.feup.apm.callhttp.co

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.KeyStore
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
  private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
  private val inside by lazy { findViewById<LinearLayout>(R.id.inside) }
  private val tvResponse by lazy { findViewById<TextView>(R.id.tv_response) }
  private val edtIp by lazy { findViewById<EditText>(R.id.edt_IP) }
  private val edtPort by lazy { findViewById<EditText>(R.id.edt_Port)}
  private val edtName by lazy { findViewById<EditText>(R.id.edt_name) }
  private val edtNick by lazy { findViewById<EditText>(R.id.edt_nick) }
  private val edtPass by lazy { findViewById<EditText>(R.id.edt_pass) }

  // button
  private val regBt by lazy { findViewById<Button>(R.id.reg_bt) }

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
    setStatusBarIconColor(window, Lightness.LIGHT)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    setInsetsPadding(toolbar, top=dpToPx(-8f), left=0, right=0)
    setInsetsPadding(inside, left=dpToPx(5f), right=dpToPx(5f))

      // check if already register
      val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
      val name = sharedPreferences.getString("name", null)

      if (!name.isNullOrEmpty()) {
          val intent = Intent(this@MainActivity, MainActivity2::class.java)
          startActivity(intent)
      }

      regBt.setOnClickListener {
        lifecycleScope.launch {
            try {

                // generate keys
                val generatedEC = generateAndStoreKeysEC()
                val generatedRSA = generateAndStoreKeysRSA()

                // send register request to server
                var ip = edtIp.text.toString()
                var port = edtPort.text.toString().toInt()
                var publicEC = getPublicKeyEC(entryEC).toString()
                var publicRSA = getPublicKeyRSA(entryRSA).toString()
                val result = addUser(this@MainActivity, ip, port, publicEC, publicRSA)

                // check uuid
                try {
                    var uuid: String? = JSONObject(result).optString("Uuid", null)
                    if (uuid != null) {

                        var name = edtName.text.toString()
                        var nick = edtNick.text.toString()
                        var pass = edtPass.text.toString()

                        if(name != "" && nick != "" && pass != ""){

                            // save info
                            val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
                            sharedPreferences.edit() {
                                putString("name", name)
                                putString("nick", nick)
                                putString("pass", pass)
                            }

                            appendResponse("Success")
                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@MainActivity, MainActivity2::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    appendResponse("Error: Server Down")
                }
            }
            catch (ex: Exception){
                appendResponse("Error: Missing Fields")
            }
        }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
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