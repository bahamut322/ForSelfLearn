package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.G_KEY
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection

/**
 * @describe:获取IP地址
 */
object WeatherHelper {
    /**
     * 通过高德web-api获取天气
     *
     * @param @return
     * @return String
     * @throws
     * @Title: getWeather
     * @Description:
     */
    fun getWeather(city: String): String {
        val infoUrl: URL?
        var inStream: InputStream? = null
        var result = ""
        var httpConnection: HttpURLConnection? = null
        try {
            infoUrl =
                URL("https://restapi.amap.com/v3/weather/weatherInfo?city=${city}&key=$G_KEY")
            val connection: URLConnection = infoUrl.openConnection()
            httpConnection = connection as HttpURLConnection
            val responseCode: Int = httpConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.inputStream
                val reader = BufferedReader(
                    InputStreamReader(inStream, "utf-8")
                )
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(
                        """
                        $line
                        """.trimIndent()
                    )
                }
                result = sb.toString()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inStream?.close()
                httpConnection?.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return result
    }
}