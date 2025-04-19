package com.example.generator

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.generator.Crypto.CRYPTO_ANDROID_KEYSTORE
import com.example.generator.Crypto.CRYPTO_CERT_SERIAL
import com.example.generator.Crypto.CRYPTO_KEY_ALGO
import com.example.generator.Crypto.CRYPTO_KEY_SIZE
import com.example.generator.Crypto.CRYPTO_NAME
import com.example.generator.Server.SERVER_INFORM
import com.example.generator.Server.SERVER_IP
import com.example.generator.Server.SERVER_PORT
import com.example.generator.utils.readStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import java.util.Calendar
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal

/**
 * Converts a public key of type `PublicKey` into a Base64 encoded string.
 *
 * @param publicKey Public key to convert.
 * @return Public key in Base64 format as a string. Returns an empty string if key is null.
 */
fun publicKeyToBase64(publicKey: PublicKey?): String {
    if (publicKey == null)
        return ""
    return Base64.getEncoder().encodeToString(publicKey.encoded)
}

/**
 * Generates a pair of RSA cryptographic keys and stores them securely in the Android Keystore.
 *
 * This method creates a private key and a public key of type RSA with the key size defined in `Crypto.RSA_KEY_SIZE`.
 * The generated key is stored in the Android Keystore.
 *
 * @return Returns `true` if key generation and storage were successful, or `false` on error.
 */
fun generateKeys() {
    try {
        val spec = KeyGenParameterSpec.Builder(
            CRYPTO_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(CRYPTO_KEY_SIZE)
            .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=$CRYPTO_NAME"))
            .setCertificateSerialNumber(BigInteger.valueOf(CRYPTO_CERT_SERIAL.toLong()))
            .setCertificateNotBefore(GregorianCalendar().time)
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
            .build()
        KeyPairGenerator.getInstance(CRYPTO_KEY_ALGO, CRYPTO_ANDROID_KEYSTORE).run {
            initialize(spec)
            generateKeyPair()
        }
    }
    catch (_: Exception) {
        return
    }
}

/**
 * Sends the RSA public key to the server to perform an informational action.
 *
 * This function converts the RSA public key into a Base64 format and sends it via POST to the specified server.
 * The server will then process this key according to the logic defined in the "/key" route.
 *
 * @param publicKeyRSA RSA public key to send to the server.
 * @return Response from the server, or an error message if the connection failed. The response is received in text format.
 */
suspend fun informServer(publicKeyRSA: PublicKey?): String {
    val publicKeyRSAString = publicKeyToBase64(publicKeyRSA)
    val url = URL("http://${SERVER_IP}:${SERVER_PORT}${SERVER_INFORM}")
    val payload = "{\"keyRSA\": \"$publicKeyRSAString\"}"

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

/**
 * Retrieves the public key from a key entry of type `PrivateKeyEntry` in the Android Keystore.
 *
 * @param entry Key entry (of type `PrivateKeyEntry`) that contains the public and private key.
 * @return Public key associated with the given key entry.
 */
fun getPublicKey(entry: KeyStore.PrivateKeyEntry?): PublicKey? {
    return entry?.certificate?.publicKey
}

/**
 * Retrieves the private key from a key entry of type `PrivateKeyEntry` in the Android Keystore.
 *
 * @param entry Key entry (of type `PrivateKeyEntry`) that contains the public and private key.
 * @return Private key associated with the given key entry.
 */
fun getPrivateKey(entry: KeyStore.PrivateKeyEntry?): PrivateKey? {
    return entry?.privateKey
}