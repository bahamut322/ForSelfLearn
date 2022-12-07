package com.sendi.deliveredrobot.utils

import android.annotation.SuppressLint
import android.os.Environment
import com.apkfuns.log2file.LogFileEngineFactory
import com.apkfuns.logutils.LogUtils
import com.apkfuns.logutils.file.LogFileEngine
import com.sendi.deliveredrobot.MyApplication
import java.text.SimpleDateFormat
import java.util.*


object LogUtil {
    private const val TAG = "SENDI_ROBOT"
    @SuppressLint("SimpleDateFormat")
    private var previousDay: Int? = null
    private lateinit var engine: LogFileEngine

    init {
        LogUtils.getLogConfig()
            .configFormatTag(TAG)
            .configAllowLog(true)
            .configShowBorders(false)
            .configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")
        val path = "${Environment.getExternalStorageDirectory().path}/logger"
        engine = LogFileEngineFactory(MyApplication.instance!!)
        LogUtils.getLog2FileConfig()
            .configLog2FileEnable(true) // targetSdkVersion >= 23 需要确保有写sdcard权限
            .configLog2FilePath(path)
            .configLog2FileNameFormat("%d{yyyyMMdd}.txt")
            .configLogFileEngine(engine)
    }

    fun i(msg: String) {
        LogUtils.i(msg)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (currentDay != previousDay){
            previousDay = currentDay
            refreshFile()
        }
    }

    fun d(msg: String) {
        LogUtils.d(msg)
    }

    fun w(msg: String) {
        LogUtils.w(msg)
    }

    fun e(msg: String) {
        LogUtils.e(msg)
    }

    private fun refreshFile(){
        val log2FileConfigImpl = LogUtils::class.java.getDeclaredField("log2FileConfig")
        val customFormatName = (log2FileConfigImpl.genericType as Class<*>).getDeclaredField("customFormatName")
        customFormatName.isAccessible = true
        customFormatName.set(LogUtils.getLog2FileConfig(),null)
        val buffer = LogFileEngineFactory::class.java.getDeclaredField("buffer")
        buffer.isAccessible = true
        buffer.set(engine, null)
    }
}