package com.example.generator.utils

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.generator.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Configuration of the title in the toolbar to be black if dark mode is on.
 * @param activity Activity where the toolbar is defined.
 * @param toolbar Toolbar.
 */
fun configuratorToolbarTitle(activity : AppCompatActivity, toolbar: Toolbar){
    val darkModeFlags = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    if(darkModeFlags == Configuration.UI_MODE_NIGHT_YES) {
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.black));
    }
}

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