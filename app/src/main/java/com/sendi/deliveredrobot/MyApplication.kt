package com.sendi.deliveredrobot

import android.app.Application
import android.content.Context
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.handler.CrashHandler
import com.sendi.deliveredrobot.utils.LogUtil.d
import com.sendi.deliveredrobot.view.fragment.FaceModule
import com.sendi.deliveredrobot.view.fragment.Utils
import com.tencent.bugly.crashreport.CrashReport
import org.litepal.LitePal
import java.io.File

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
        lateinit var context: Context
        lateinit var faceModule:FaceModule
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
        BaiduTTSHelper.getInstance() //初始化LitePal
        LitePal.initialize(this)//数据库实例
        //添加腾讯bugly
        CrashReport.initCrashReport(applicationContext, "3f38b69ec1", false)
        //初始化人脸识别&检测
        Thread {
            val faceDetectionModelPath =
                instance!!.cacheDir
                    .absolutePath + File.separator + "yolov5n_shuffle_256x320_quan.mnn"
            val ageAndGenderModelPath =
                instance!!.cacheDir
                    .absolutePath + File.separator + "ageAndGender.mnn"
            val faceRecognizermodelPath =
                instance!!.cacheDir
                    .absolutePath + File.separator + "resnet18_110.mnn"
            Utils.copyFileFromAsset(
                instance,
                "yolov5n_shuffle_256x320_quan.mnn",
                faceDetectionModelPath
            )
            Utils.copyFileFromAsset(
                instance,
                "ageAndGender.mnn",
                ageAndGenderModelPath
            )
            Utils.copyFileFromAsset(
                instance,
                "resnet18_110.mnn",
                faceRecognizermodelPath
            )
            faceModule =
                FaceModule(faceDetectionModelPath, ageAndGenderModelPath, faceRecognizermodelPath)
            d("人脸识别库初始化完成")
        }.start()

    }
}