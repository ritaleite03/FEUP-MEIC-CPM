package org.feup.apm.callhttp.co

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
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

data class ECKey(var x: ByteArray, var y: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ECKey

        if (!x.contentEquals(other.x)) return false
        if (!y.contentEquals(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.contentHashCode()
        result = 31 * result + y.contentHashCode()
        return result
    }
}

data class PubKey(var modulus: ByteArray, var exponent: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PubKey

        if (!modulus.contentEquals(other.modulus)) return false
        if (!exponent.contentEquals(other.exponent)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modulus.contentHashCode()
        result = 31 * result + exponent.contentHashCode()
        return result
    }
}

/**
 * Generates a pair of EC cryptographic keys and stores them securely in the Android Keystore
 */
fun generateAndStoreKeysEC() : Boolean {
    try {
        val spec = KeyGenParameterSpec.Builder(Crypto.EC_NAME, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
            .setAlgorithmParameterSpec(ECGenParameterSpec(Crypto.EC_CURVE)) // The EC curve name
            .setDigests(KeyProperties.DIGEST_SHA256) // allowed digests for signature
            .setCertificateSubject(X500Principal("CN=" + Crypto.EC_NAME)) // Certificate properties to wrap the public key
            .setCertificateSerialNumber(BigInteger.valueOf(Crypto.SerialNr))
            .setCertificateNotBefore(GregorianCalendar().time)
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
            .build()
        KeyPairGenerator.getInstance(Crypto.EC_KEY_ALGO, Crypto.ANDROID_KEYSTORE).apply {
            initialize(spec)
            generateKeyPair() // the generated keys are stored in the Android Keystore
        }
    }
    catch(ex: Exception) {
        return false
    }
    return true
}

/**
 * Generates a pair of RSA cryptographic keys and stores them securely in the Android Keystore
 */
fun generateAndStoreKeysRSA(): Boolean {
    try {
        val spec = KeyGenParameterSpec.Builder(
            Crypto.RSA_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(Crypto.RSA_KEY_SIZE) // Only for RSA keys
            .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256) // allowed digests for encryption and for signature
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1) // allowed padding schema for encryption
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1) // allowed padding schema for signature
            .setCertificateSubject(X500Principal("CN=" + Crypto.RSA_NAME)) // properties of the certificate containing the public key
            .setCertificateSerialNumber(BigInteger.valueOf(Crypto.SerialNr)) // Some
            .setCertificateNotBefore(GregorianCalendar().time) // Validity from now ...
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time) // ... to now + 10 years
            .build()
        KeyPairGenerator.getInstance(Crypto.RSA_KEY_ALGO, Crypto.ANDROID_KEYSTORE).apply {
            initialize(spec)
            generateKeyPair()   // the generated keys are stored in the Android Keystore
        }
    }
    catch (ex: Exception) {
        return false
    }
    return true
}

/**
 * Get public key of EC pair
 */
fun getPublicKeyEC(entry: KeyStore.Entry?): ECKey {
    val pKey = ECKey(ByteArray(0), ByteArray(0))
    try {
        val puKey = (entry as PrivateKeyEntry).certificate.publicKey as ECPublicKey
        pKey.x = puKey.w.affineX.toByteArray().let { if (it.first() == 0x00.toByte()) it.drop(1).toByteArray() else it }
        pKey.y = puKey.w.affineY.toByteArray().let { if (it.first() == 0x00.toByte()) it.drop(1).toByteArray() else it }
    }
    catch (ex: Exception) {
    }
    return pKey
}

/**
 * Get public key of EC pair
 */
fun getPublicKeyRSA(entry: KeyStore.Entry?): PubKey {
    val pKey = PubKey(ByteArray(0), ByteArray(0))
    try {
        val pub = (entry as KeyStore.PrivateKeyEntry).certificate.publicKey
        pKey.modulus = (pub as RSAPublicKey).modulus.toByteArray()
        pKey.exponent = pub.publicExponent.toByteArray()
    }
    catch (ex: Exception) {
    }
    return pKey
}

/**
 * Get private key of EC pair
 */
fun getPrivateKeyEC(entry: KeyStore.Entry?): ByteArray {
    var sPriv = ByteArray(0)
    try {
        val prKey = (entry as PrivateKeyEntry).privateKey as ECPrivateKey
        sPriv = prKey.s.toByteArray()
    }
    catch (ex: Exception) {
    }
    return sPriv
}

/**
 * Get private key of RSA pair
 */
fun getPrivateKeyRSA(entry: KeyStore.Entry?): ByteArray {
    var exp = ByteArray(0)
    try {
        val priv = (entry as KeyStore.PrivateKeyEntry).privateKey
        exp = (priv as RSAPrivateKey).privateExponent.toByteArray()
    }
    catch (ex: Exception) {
    }
    return exp
}