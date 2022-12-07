package com.sendi.deliveredrobot

import android.app.Application
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.handler.CrashHandler

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler().getInstance()?.init(this)
        BaiduTTSHelper.getInstance()
    }
}