package com.example.generator

/**
 * Contains server-related constants such as IP address, port, and route paths.
 */
object Server {

    /**
     * IP address of the server.
     */
    const val IP = "10.227.156.12" // "192.168.68.125"

    /**
     * Server port.
     */
    const val PORT = "8000"

    /**
     * Path of the informative action route on the server.
     */
    const val INFORM = "/key"
}

/**
 * Contains cryptography-related constants, including parameters RSA keys.
 */
object Crypto {

    /**
     * Android Keystore provider name.
     */
    const val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Serial number associated with the key.
     */
    const val SerialNr = 1234567890L

    /**
     * RSA key size.
     */
    const val KEY_SIZE = 512

    /**
     * RSA key algorithm.
     */
    const val KEY_ALGO = "RSA"

    /**
     * Signature algorithm used for RSA keys.
     */
    const val SIGN_ALGO = "SHA256WithRSA"

    /**
     * RSA encryption algorithm.
     */
    const val ENC_ALGO = "RSA/NONE/PKCS1Padding"

    /**
     * Name used for the RSA key in the keystore.
     */
    const val NAME = "AcmeKey"

    /**
     * Unique identifier (tag) for the key.
     */
    const val tagId = 0x41636D65 // equal to "Acme"

    /**
     * Certificate serial number.
     */
    const val CERT_SERIAL = 12121212

    /**
     * Card processing completion action.
     */
    const val ACTION_CARD_DONE = "CMD_PROCESSING_DONE"
}