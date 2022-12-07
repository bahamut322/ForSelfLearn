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
object LocationHelper {
    /**
     * 获取外网的IP(要访问Url，要放到后台线程里处理)
     *
     * @param @return
     * @return String
     * @throws
     * @Title: GetNetIp
     * @Description:
     */
    fun getLocation(ip: String): String {
        val infoUrl: URL
        var inStream: InputStream? = null
        var httpConnection: HttpURLConnection? = null
        var result = ""
        try {
            infoUrl =
                URL("https://restapi.amap.com/v3/ip?ip=${ip}&key=$G_KEY")
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