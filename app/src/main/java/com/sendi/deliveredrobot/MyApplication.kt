package com.sendi.deliveredrobot

import android.app.Application
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.handler.CrashHandler
import com.tencent.bugly.crashreport.CrashReport

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler().getInstance()?.init(this)
//        BaiduTTSHelper.getInstance()
        //添加腾讯bugly
        //添加腾讯bugly
        CrashReport.initCrashReport(applicationContext, "3f38b69ec1", false)
    }
}