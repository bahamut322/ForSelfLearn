package com.sendi.deliveredrobot

import android.app.Application
import android.content.Context
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.handler.CrashHandler
import com.tencent.bugly.crashreport.CrashReport
import org.litepal.LitePal

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
        lateinit var context: Context
    }

    fun getInstance(): MyApplication {
        checkNotNull(instance) { "Application is not created." }
        return instance as MyApplication
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        context = baseContext
        CrashHandler().getInstance()?.init(this)
        BaiduTTSHelper.getInstance()
        //初始化LitePal
        LitePal.initialize(this)//数据库实例
        //添加腾讯bugly
        CrashReport.initCrashReport(applicationContext, "3f38b69ec1", false)
    }
}