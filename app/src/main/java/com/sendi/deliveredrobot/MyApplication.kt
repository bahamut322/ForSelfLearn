package com.sendi.deliveredrobot

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.handler.CrashHandler
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.LogUtil.d
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.fragment.FaceModule
import com.sendi.deliveredrobot.view.fragment.Utils
import com.tencent.bugly.crashreport.CrashReport
import jni.Usbcontorl
import org.litepal.LitePal
import java.io.File

class MyApplication : Application() {
    companion object {
        var instance: Application? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var faceModule: FaceModule
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
        CrashHandler().getInstance()?.init(this)
        BaiduTTSHelper.getInstance() //初始化LitePal
        LitePal.initialize(this)//数据库实例
        listener = object : DownloadBill.DownloadListener {
            override fun onProgress(progress: Int) {
                // 更新进度条
                Log.e("TAG", "onProgress: $progress 剩余任务数：${DownloadBill.getInstance().taskCount}")
                DialogHelper.robotUpDataDialog.show()


            }
            override fun onFinish() {
                // 下载完成
                Log.e("TAG", "DownLoad FinishOnce")
                if (DownloadBill.getInstance().taskCount == 0) {
                    DialogHelper.robotUpDataDialog.dismiss()
                    Log.e("TAG", "onProgress: FinishAll")
                    UpdateReturn().method(Universal.mapType.value!!)
                    RobotStatus.newUpdata.postValue(1)
                }
            }
            override fun onError(e: Exception) {
                // 下载出错
                ToastUtil.show("下载失败")
                Log.e("TAG", "downLoad Error: $e")
            }
        }

        //添加腾讯bugly
        CrashReport.initCrashReport(applicationContext, "3f38b69ec1", false)
        //初始化人脸识别&检测
//        Thread {
            if (Usbcontorl.isload) {
                Usbcontorl.usb3803_mode_setting(1) //打开5V
            }
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
//            faceModule =
//                FaceModule(faceDetectionModelPath, ageAndGenderModelPath, faceRecognizermodelPath)
//            d("人脸识别库初始化完成")
//        }.start()

    }
}