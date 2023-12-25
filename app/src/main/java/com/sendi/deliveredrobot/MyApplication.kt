package com.sendi.deliveredrobot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.handler.CrashHandler
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.interfaces.DownLoadListener
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.ToastUtil
import com.tencent.bugly.crashreport.CrashReport
import jni.Usbcontorl
import org.litepal.LitePal
import org.xutils.x
import java.io.File

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var listener: DownloadBill.DownloadListener? = null
    }

    fun getInstance(): MyApplication {
        checkNotNull(instance) { "Application is not created." }
        return instance as MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = baseContext
        x.Ext.init(this)//初始化xUtils3
        CrashHandler().getInstance()?.init(this)
        BaiduTTSHelper.getInstance()
        LitePal.initialize(this)//数据库实例
        listener = object : DownloadBill.DownloadListener {
            override fun onProgress(progress: Int) {
                // 更新进度条
                DownLoadListener.setProgress(progress)
                Log.d("TAG", "onProgress: $progress 剩余任务数：${DownloadBill.getInstance().taskCount}")
                //避免资源过多消耗
                if (!DialogHelper.robotUpDataDialog.isShowing) {
                    DialogHelper.robotUpDataDialog.show()
                    //设置一个大屏幕默认的图片
                    RobotStatus.newUpdata.postValue(3)
                }
            }
            override fun onFinish() {
                // 下载完成
                Log.d("TAG", "DownLoad FinishOnce")
                if (DownloadBill.getInstance().taskCount == 0) {
                    DialogHelper.robotUpDataDialog.dismiss()
                    Log.d("TAG", "onProgress: FinishAll")
                    UpdateReturn().method(Universal.mapType.value!!)
                    RobotStatus.newUpdata.postValue(1)
                }
            }
            override fun onError(e: Exception) {
                // 下载出错
                ToastUtil.show("下载失败")
                Log.i("TAG", "downLoad Error: $e")
            }
        }

        //添加腾讯bugly
        CrashReport.initCrashReport(applicationContext, "3f38b69ec1", false)
        //初始化人脸识别&检测
//        Thread {
            if (Usbcontorl.isload) {
                Usbcontorl.usb3803_mode_setting(1) //打开5V
            }
//        CommonHelper.executeRebootVoiceRecord()


    }
}