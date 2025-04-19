package com.example.generator.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Reads the contents of an InputStream and returns it as a string.
 *
 * This function reads line by line from an input stream and stores the contents in a string.
 * If an error occurs while trying to read from the stream, the function catches the exception and returns an error message in string.
 *
 * @param input Input stream (InputStream) to read from.
 * @return Contents read from the input stream as a string. If there is an error, it returns an error message.
 */
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

/**
 * Converts a byte array to a hexadecimal representation.
 *
 * This function takes an array of bytes and converts each byte to its corresponding hexadecimal value.
 * The final format is a string containing the hexadecimal values, with no spaces between them.
 *
 * @param ba ByteArray to convert to a hexadecimal string.
 * @return String representing the hexadecimal values of the given byte array.
 */
fun byteArrayToHex(ba: ByteArray): String {
    val sb = StringBuilder(ba.size * 2)
    for (b in ba) sb.append(String.format("%02x", b))
    return sb.toString()
}

/**
 * Converts a hexadecimal string to a byte array.
 *
 * This function takes a string representing data in hexadecimal format and converts it back to a byte array.
 * Each pair of hexadecimal characters (e.g. "1A", "FF") is converted to a corresponding byte.
 * The string must have an even number of characters.
 *
 * @param s Hexadecimal string to convert to a byte array.
 * @return Byte array equivalent to the given hexadecimal string.
 */
fun hexStringToByteArray(s: String): ByteArray {
    val data = ByteArray(s.length / 2)
    var k = 0
    while (k < s.length) {
        data[k/2] = ((Character.digit(s[k], 16) shl 4) + Character.digit(s[k+1], 16)).toByte()
        k += 2
    }
    return data
}