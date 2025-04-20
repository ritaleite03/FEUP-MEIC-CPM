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