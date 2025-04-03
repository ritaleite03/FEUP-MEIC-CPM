package com.example.generator

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.example.generator.Crypto.ANDROID_KEYSTORE
import com.example.generator.Crypto.CERT_SERIAL
import com.example.generator.Crypto.KEY_ALGO
import com.example.generator.Crypto.KEY_SIZE
import com.example.generator.Crypto.NAME
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

fun publicKeyToBase64(publicKey: PublicKey?): String {
    if (publicKey == null)
        return ""
    return Base64.getEncoder().encodeToString(publicKey.encoded)
}

fun generateKeys() {
    try {
        val spec = KeyGenParameterSpec.Builder(
            NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(KEY_SIZE)
            .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=" + NAME))           // for the certificate containing the public key
            .setCertificateSerialNumber(BigInteger.valueOf(CERT_SERIAL.toLong()))  // some serial number ...
            .setCertificateNotBefore(GregorianCalendar().time)
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
            .build()
        KeyPairGenerator.getInstance(KEY_ALGO, ANDROID_KEYSTORE).run {
            initialize(spec)
            generateKeyPair()
        }
    }
    catch (_: Exception) {
        return
    }
}

suspend fun informServer(publicKeyRSA: PublicKey?): String {
    val publicKeyRSAString = publicKeyToBase64(publicKeyRSA)
    val url = URL("http://${Server.IP}:${Server.PORT}${Server.INFORM}")
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

fun getPublicKey(entry: KeyStore.PrivateKeyEntry?): PublicKey? {
    return entry?.certificate?.publicKey
}

fun getPrivateKey(entry: KeyStore.PrivateKeyEntry?): PrivateKey? {
    return entry?.privateKey
}