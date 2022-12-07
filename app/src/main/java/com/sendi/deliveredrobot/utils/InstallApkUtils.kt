package com.sendi.deliveredrobot.utils

import android.annotation.SuppressLint
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author heky
 * @date 2022-04-13
 * @describe APK安装
 */
@SuppressLint("StaticFieldLeak")
object InstallApkUtils {

    private fun isHasFile(path: String): Boolean {
        try {
            val f = File(path)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }


    /**
     * @author 获取列表
     */
    fun executeSuCMD(currentTempFilepath: String) {
        if (isHasFile(currentTempFilepath)) {
            val process: Process?
            var out: OutputStream? = null
            try {
                //请求root
                process = Runtime.getRuntime().exec("su")
                out = process.outputStream
                //调用安装
                out.write("pm install -t -r $currentTempFilepath\n".toByteArray())
                //调起app
                out.write("am start com.sendi.fooddeliveryrobot/com.sendi.deliveredrobot.MainActivity\n".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (out != null) {
                        out.flush()
                        out.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            LogUtil.e("apk is not exist")
        }
    }
}