package com.example.apidemo

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


object HttpRequest {

    @JvmStatic
    fun executeGet(targetURL: String?): String? {
        val url: URL
        var connection: HttpURLConnection? = null
        return try {
            url = URL(targetURL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val `is`: InputStream

            //retrieve data from url
            val status = connection.responseCode
            `is` =
                if (status != HttpURLConnection.HTTP_OK) connection.errorStream else connection.inputStream
            val rd = BufferedReader(InputStreamReader(`is`))
            var line: String?
            val response = StringBuffer()
            while (rd.readLine().also { line = it } != null) {
                response.append(line)
                response.append('\r')
            }
            rd.close()
            response.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }

}