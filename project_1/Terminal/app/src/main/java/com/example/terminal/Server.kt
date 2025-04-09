package com.example.terminal

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.PublicKey

suspend fun pay(message : ByteArray): String {

    val url = URL("http://${Server.IP}:${Server.PORT}${Server.PAY}")
    val messageBase64 = Base64.encodeToString(message, Base64.NO_WRAP)
    val payload = "{\"message\": \"$messageBase64\"}"
    //val payload = "{\"message\": \"$message\"}"

    return withContext(Dispatchers.IO) {
        var urlConnection: HttpURLConnection? = null
        var result: String
        try {
            urlConnection = (url.openConnection() as HttpURLConnection).apply {
                doOutput = true
                doInput = true
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                useCaches = false
                connectTimeout = 5000
                with(outputStream) {
                    write(payload.toByteArray())
                    flush()
                    close()
                }
                // get response
                result = if (responseCode == 200)
                    readStream(inputStream)
                else
                    "Code: $responseCode"

                val jsonResponse = JSONObject(result)
                val verified = jsonResponse.getBoolean("verified")

                if (verified) {
                    result = "A verificação foi bem-sucedida"
                } else {
                    result = "A verificação falhou"
                }
            }
        } catch (e: Exception) {
            result = e.toString()
        }
        urlConnection?.disconnect()
        result
    }
}
