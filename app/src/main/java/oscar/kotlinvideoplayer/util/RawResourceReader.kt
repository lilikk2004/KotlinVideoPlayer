package oscar.kotlinvideoplayer.util

import android.content.Context

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object RawResourceReader {
    fun readTextFileFromRawResource(context: Context,
                                    resourceId: Int): String? {
        val inputStream = context.resources.openRawResource(
                resourceId)
        val inputStreamReader = InputStreamReader(
                inputStream)
        val bufferedReader = BufferedReader(
                inputStreamReader)

        var nextLine: String?
        val body = StringBuilder()

        try {
            while (true) {
                nextLine = bufferedReader.readLine()
                if(nextLine == null){
                    break
                }
                body.append(nextLine)
                body.append('\n')
            }
        } catch (e: IOException) {
            return null
        }

        return body.toString()
    }
}
