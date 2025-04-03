package com.example.client

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.client.utils.Crypto
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.util.Calendar
import java.util.GregorianCalendar
import javax.security.auth.x500.X500Principal

/**
 * Generates a pair of EC cryptographic keys and stores them securely in the Android Keystore
 */
fun generateEC() : Boolean {
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
    catch(_: Exception) {
        return false
    }
    return true
}

/**
 * Generates a pair of RSA cryptographic keys and stores them securely in the Android Keystore
 */
fun generateRSA(): Boolean {
    try {
        val spec = KeyGenParameterSpec.Builder(Crypto.RSA_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
            .setKeySize(Crypto.RSA_KEY_SIZE)
            .setDigests(KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=" + Crypto.RSA_NAME))
            .setCertificateSerialNumber(BigInteger.valueOf(Crypto.SerialNr))
            .setCertificateNotBefore(GregorianCalendar().time)
            .setCertificateNotAfter(GregorianCalendar().apply { add(Calendar.YEAR, 10) }.time)
            .build()
        KeyPairGenerator.getInstance(Crypto.RSA_KEY_ALGO, Crypto.ANDROID_KEYSTORE).apply {
            initialize(spec)
            generateKeyPair()
        }
    }
    catch (_: Exception) {
        return false
    }
    return true
}

fun getPublicKey(entry: KeyStore.PrivateKeyEntry?): PublicKey? {
    return entry?.certificate?.publicKey
}

fun getPrivateKey(entry: KeyStore.PrivateKeyEntry?): PrivateKey? {
    return entry?.privateKey
}