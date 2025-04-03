package com.example.generator

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

fun readStream(input: InputStream): String {
    var reader: BufferedReader? = null
    var line: String?
    val response = StringBuilder()
    try {
        reader = BufferedReader(InputStreamReader(input))
        while (reader.readLine().also{ line = it } != null)
            response.append(line)
    }
    catch (e: IOException) {
        response.clear()
        response.append("readStream: ${e.message}")
    }
    reader?.close()
    return response.toString()
}

fun byteArrayToHex(ba: ByteArray): String {
    val sb = StringBuilder(ba.size * 2)
    for (b in ba) sb.append(String.format("%02x", b))
    return sb.toString()
}

fun hexStringToByteArray(s: String): ByteArray {
    val data = ByteArray(s.length / 2)
    var k = 0
    while (k < s.length) {
        data[k/2] = ((Character.digit(s[k], 16) shl 4) + Character.digit(s[k+1], 16)).toByte()
        k += 2
    }
    return data
}