package com.sendi.deliveredrobot.utils

import android.annotation.SuppressLint
import com.sendi.deliveredrobot.MyApplication
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

    /**
     * @description 通过packageManager查询包名是否存在
     */
    fun isPackageExist(packageName: String): Boolean {
        val packageManager = MyApplication.context.packageManager
        val packagesInfo = packageManager.getInstalledPackages(0)
        for (i in packagesInfo.indices) {
            if (packagesInfo[i].packageName.equals(packageName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * @description 通过pageName卸载应用
     */
    fun uninstallApk(packageName: String): Boolean {
        var result: Boolean
        if (!isPackageExist(packageName)) {
            return false
        }
        if(packageName == "com.sendi.fooddeliveryrobot"){
            return false
        }
        val process: Process?
        var out: OutputStream? = null
        try {
            //请求root
            process = Runtime.getRuntime().exec("su")
            out = process.outputStream
            //调用安装
            out.write("pm uninstall $packageName\n".toByteArray())
        } catch (e: IOException) {
            return false
        } catch (e: Exception) {
            return false
        } finally {
            try {
                if (out != null) {
                    out.flush()
                    out.close()
                }
                result = true
            } catch (e: IOException) {
                result = false
            }
        }
        return result
    }
}