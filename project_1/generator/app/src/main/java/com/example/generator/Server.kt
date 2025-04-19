package com.example.generator

import com.example.generator.Server.SERVER_GROCERIES
import com.example.generator.Server.SERVER_IP
import com.example.generator.Server.SERVER_PORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun getGroceries(): String {
    val url = "http://${SERVER_IP}:${SERVER_PORT}${SERVER_GROCERIES}"
    return sendMessageServer(url)
}

suspend fun sendMessageServer(url: String): String {
    return withContext(Dispatchers.IO) {
        var urlConnection: HttpURLConnection? = null
        var result: String

        try {
            urlConnection = (URL(url).openConnection() as HttpURLConnection).apply {
                doInput = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                useCaches = false
                connectTimeout = 5000
                result = if (responseCode == 200) {
                    readStream(inputStream)
                } else {
                    "Code: $responseCode"
                }
            }
        } catch (e: Exception) {
            result = e.toString()
        }

        urlConnection?.disconnect()
        result
    }
}