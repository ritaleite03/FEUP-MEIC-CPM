package com.example.client.utils

/**
 * Contains server-related constants such as IP address, port, and route paths.
 */
object Server {

    /**
     * IP address of the server.
     */
    const val IP = "192.168.68.125"

    /**
     * Server port.
     */
    const val PORT = "8000"

    /**
     * Path of the user registration route on the server.
     */
    const val REGISTER = "/users/add"
}

/**
 * Contains cryptography-related constants, including parameters for EC (Elliptic Curve) and RSA keys.
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

    // EC (Elliptic Curve) key related parameters

    /**
     * EC (Elliptic Curve) key algorithm.
     */
    const val EC_KEY_ALGO = "EC"

    /**
     * EC curve used for EC key generation.
     */
    const val EC_CURVE = "secp256r1"

    /**
     * Signature algorithm for EC keys.
     */
    const val EC_SIGN_ALGO = "SHA256withECDSA"

    /**
     * Name used for the EC key in the keystore.
     */
    const val EC_NAME = "myECDemoKey"

    // RSA key related parameters

    /**
     * RSA key size.
     */
    const val RSA_KEY_SIZE = 512

    /**
     * RSA key algorithm.
     */
    const val RSA_KEY_ALGO = "RSA"

    /**
     * Signature algorithm used for RSA keys.
     */
    const val RSA_SIGN_ALGO = "SHA256WithRSA"

    /**
     * RSA encryption algorithm.
     */
    const val RSA_ENC_ALGO = "RSA/NONE/PKCS1Padding"

    /**
     * Name used for the RSA key in the keystore.
     */
    const val RSA_NAME = "myRSADemoKey"
}