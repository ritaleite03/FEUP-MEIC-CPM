package com.example.client.utils

object Server {
    const val IP = "192.168.68.125"
    const val PORT = "8000"
    const val REGISTER = "/users/add"
}

object Crypto {
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val SerialNr = 1234567890L
    // EC
    const val EC_KEY_ALGO = "EC"
    const val EC_CURVE = "secp256r1"
    const val EC_SIGN_ALGO = "SHA256withECDSA"
    const val EC_NAME = "myECDemoKey"
    // RSA
    const val RSA_KEY_SIZE = 512
    const val RSA_KEY_ALGO = "RSA"
    const val RSA_SIGN_ALGO = "SHA256WithRSA"
    const val RSA_ENC_ALGO = "RSA/NONE/PKCS1Padding"
    const val RSA_NAME = "myRSADemoKey"
}