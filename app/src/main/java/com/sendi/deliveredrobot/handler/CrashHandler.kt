package com.sendi.deliveredrobot.handler

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Field


/**
 *   @author: heky
 *   @date: 2021/7/14 10:16
 *   @describe: 全部捕获异常
 */
class CrashHandler : Thread.UncaughtExceptionHandler {
    private var instance: CrashHandler? = null
    fun getInstance(): CrashHandler? {
        if (instance == null) {
            synchronized(CrashHandler::class.java) {
                if (instance == null) {
                    instance = CrashHandler()
                }
            }
        }
        return instance
    }

    private var mContext: Context? = null
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    fun init(context: Context?) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        Handler(Looper.getMainLooper()).post(Runnable {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    handlerCrash(e)
                }
            }
        })
    }


    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
//        if (!handlerCrash(ex) && mDefaultHandler != null) {
//            // 没有处理还交给系统默认的处理器
//            mDefaultHandler!!.uncaughtException(thread, ex)
//        } else {
//            // 已经处理，结束进程
////            Process.killProcess(Process.myPid())
//            exitProcess(1)
//        }
    }

    /**
     * 自定义处理策略
     * @return true：已处理
     */
    private fun handlerCrash(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }

        // 收集设备信息、版本信息、异常信息
        val info = collectDeviceInfo(mContext, ex)
        // 本地固化存储
        saveInfo(info)
        //发生异常则停止机器人
        BillManager.billList().clear()
        MainScope().launch {
//            var result = ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//            while (!result) {
//                result = ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//            }
            ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
        }
        // 上传服务器（该功能可以独立到外边，定时上传或者进入应用时检测上传）
        return true
    }

    /**
     * 收集设备信息
     * @param mContext2
     * @param ex
     * @param infos
     */
    private fun collectDeviceInfo(c: Context?, ex: Throwable): String {
        val info: MutableMap<String, String> = HashMap()
        // 收集版本信息
        try {
            val pm: PackageManager = c!!.packageManager
            val pi: PackageInfo =
                pm.getPackageInfo(c.packageName, PackageManager.GET_ACTIVITIES)
            val versionCode: String = pi.versionCode.toString() + ""
            val versionName =
                if (TextUtils.isEmpty(pi.versionName)) "没有版本名称" else pi.versionName
            info["versionCode"] = versionCode
            info["versionName"] = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        // 收集设备信息
        val fields: Array<Field> = Build::class.java.getDeclaredFields()
        for (field in fields) {
            try {
                field.isAccessible = true
                info[field.name] = field.get(null).toString()
            } catch (e: Exception) {
            }
        }

        // 收集异常信息
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result: String = writer.toString()

        // 转化为字符串
        val sb = StringBuffer()
        for ((key, value) in info) {
            sb.append("$key=$value\n")
        }
        sb.append(result)
        return sb.toString()
    }


    /**
     * 保存异常信息到本地
     * @param info
     */
    private fun saveInfo(info: String) {
        // 把采集到的信息写入到本地文件
        LogUtil.e(info)
        // 打印错误
//        if (BuildConfig.IS_DEBUG) {
//            ToastUtil.show(info)
//        }
    }

}