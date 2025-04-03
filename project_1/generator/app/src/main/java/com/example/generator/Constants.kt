package com.example.generator

object Server {
    const val IP = "192.168.68.125"
    const val PORT = "8000"
    const val INFORM = "/key"
}

object Crypto {
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val SerialNr = 1234567890L

    // RSA
    const val KEY_SIZE = 512
    const val KEY_ALGO = "RSA"
    const val SIGN_ALGO = "SHA256WithRSA"
    const val ENC_ALGO = "RSA/NONE/PKCS1Padding"
    const val NAME = "AcmeKey"
    const val tagId = 0x41636D65 // equal to "Acme"
    const val CERT_SERIAL = 12121212
    const val ACTION_CARD_DONE = "CMD_PROCESSING_DONE"
}