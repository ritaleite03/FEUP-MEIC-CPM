package org.feup.apm.callhttp.co

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

private fun readStream(input: InputStream): String {
  var reader: BufferedReader? = null
  var line: String?
  val response = StringBuilder()
  try {
    reader = BufferedReader(InputStreamReader(input))
    while (reader.readLine().also{ line = it } != null)
      response.append(line)
  }
  catch (e: IOException) {
    response.clear()
    response.append("readStream: ${e.message}")
  }
  reader?.close()
  return response.toString()
}

//**************************************************************************
// Function to call REST operation GetUser
suspend fun getUser(act: MainActivity, baseAddress: String, port: Int, userName: String, userNick: String, userPass: String): String {
  val urlRoute = "/users/get"
  val url = URL("http://$baseAddress:$port$urlRoute")
  val payload = "{\"username\": \"$userName\", \"usernick\": \"$userNick\", \"password\": \"$userPass\"}"

  act.setResponse("POST ${url.toExternalForm()}")
  act.appendResponse("Payload: $payload")

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
      }
    } catch (e: Exception) {
      result = e.toString()
    }
    urlConnection?.disconnect()
    result
  }
}

//**************************************************************************
// Function to call REST operation AddUser
suspend fun addUser(act: MainActivity, baseAddress: String, port: Int, userName: String, userNick: String, userPass: String): String {
  val urlRoute = "/users/add"
  val url = URL("http://$baseAddress:$port$urlRoute")
  val payload = "{\"username\": \"$userName\", \"usernick\": \"$userNick\", \"password\": \"$userPass\"}"

  act.setResponse("POST ${url.toExternalForm()}")
  act.appendResponse("Payload: $payload")

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
      }
    } catch (e: Exception) {
      result = e.toString()
    }
    urlConnection?.disconnect()
    result
  }
}
