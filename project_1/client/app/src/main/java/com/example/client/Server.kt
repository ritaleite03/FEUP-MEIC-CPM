package com.example.client

import com.example.client.utils.Server
import com.example.client.utils.readStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

fun publicKeyToBase64(publicKey: PublicKey?): String {
    if (publicKey == null)
        return ""
    return Base64.getEncoder().encodeToString(publicKey.encoded)
}

fun base64ToPublicKey(base64Key: String): PublicKey? {
    return try {
        val keyBytes = Base64.getDecoder().decode(base64Key) // Decodifica Base64 para ByteArray
        val keySpec = X509EncodedKeySpec(keyBytes) // Cria a especificação da chave
        val keyFactory = KeyFactory.getInstance("RSA") // Defina o algoritmo correto (RSA, EC, etc.)
        keyFactory.generatePublic(keySpec) // Constrói a chave pública
    } catch (e: Exception) {
        e.printStackTrace()
        null // Retorna null em caso de erro
    }
}

/**
 * Perform action of registration on server
 */
suspend fun register(publicEC: PublicKey?, publicRSA: PublicKey?): String {
    val publicStringEC = publicKeyToBase64(publicEC)
    val publicStringRSA = publicKeyToBase64(publicRSA)

    val url = URL("http://${Server.IP}:${Server.PORT}${Server.REGISTER}")
    val payload = "{\"keyEC\": \"$publicStringEC\", \"keyRSA\": \"$publicStringRSA\"}"

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
