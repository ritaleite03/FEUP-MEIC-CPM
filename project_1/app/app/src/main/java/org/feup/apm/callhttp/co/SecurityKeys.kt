package org.feup.apm.callhttp.co

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Calendar
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal

// General top-level utility functions and data classes
fun byteArrayToHex(ba: ByteArray): String {
    val sb = StringBuilder(ba.size * 2)
    for (b in ba) sb.append(String.format("%02x", b))
    return sb.toString()
}

data class ECKey(var x: ByteArray, var y: ByteArray)

/**
 * Generates a pair of EC (Elliptic Curve) cryptographic keys and stores them securely in the Android Keystore
 */
private fun generateAndStoreECKeys() : Boolean {
    try {
        val spec = KeyGenParameterSpec.Builder(Crypto.KeyName, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
            .setAlgorithmParameterSpec(ECGenParameterSpec(Crypto.EC_CURVE)) // The EC curve name
            .setDigests(KeyProperties.DIGEST_SHA256) // allowed digests for signature
            .setCertificateSubject(X500Principal("CN=" + Crypto.KeyName)) // Certificate properties to wrap the public key
            .setCertificateSerialNumber(BigInteger.valueOf(Crypto.SerialNr))
            .setCertificateNotBefore(GregorianCalendar().time)
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
            .build()
        KeyPairGenerator.getInstance(Crypto.KEY_ALGO, Crypto.ANDROID_KEYSTORE).apply {
            initialize(spec)
            generateKeyPair() // the generated keys are stored in the Android Keystore
        }
    }
    catch(ex: Exception) {
        return false
    }
    return true
}