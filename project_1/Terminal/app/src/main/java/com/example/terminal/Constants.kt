package com.example.terminal

/**
 * Contains server-related constants such as IP address, port, and route paths.
 */
object Server {

    /**
     * IP address of the server.
     */
    const val IP = "192.168.68.125" // "192.168.68.125"

    /**
     * Server port.
     */
    const val PORT = "8000"

    /**
     * Path of the user registration route on the server.
     */
    const val PAY = "/pay"
}

object NFC {

    const val CARD_AID = "F010203040"
    const val CMD_SEL_AID = "00A40400"
}