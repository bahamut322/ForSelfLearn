package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iflytek.vtncaetest.engine.EngineConstants
import com.iflytek.vtncaetest.engine.WakeupEngine
import com.iflytek.vtncaetest.engine.WakeupListener
import com.iflytek.vtncaetest.recorder.AudioRecorder
import com.iflytek.vtncaetest.recorder.RecorderFactory
import com.iflytek.vtncaetest.recorder.SystemRecorder
import com.iflytek.vtncaetest.utils.CopyAssetsUtils
import com.iflytek.vtncaetest.utils.ErrorCode
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import kotlin.concurrent.thread

open class BaseFragment: Fragment(){
    /**
     * 唤醒回调
     */
    private var wakeupListener: WakeupListener? = null

    private var recorder: AudioRecorder? = null

    override fun onResume() {
        super.onResume()
        wakeupListener = WakeupListener { angle, beam, score, keyWord ->
            LogUtil.i("angle:$angle,beam:$beam,score:$score,keyWord:$keyWord")
            quitFragment()
            findNavController().navigate(R.id.conversationFragment)
        }
        thread {
//            DialogHelper.loadingDialog.show()
//            Thread.sleep(2000)
            CopyAssetsUtils.portingFile(MyApplication.context)
            initSDK()
//            Thread.sleep(5000)
            startRecord()
//            DialogHelper.loadingDialog.dismiss()
        }
    }

    private fun initSDK() {
        //状态初始化
        EngineConstants.isRecording = false
        //注意事项1: sn每台设备需要唯一！！！！WakeupEngine的sn和AIUI的sn要一致
        //注意事项2: 获取的值要保持稳定，否则会重复授权，浪费授权量
        EngineConstants.serialNumber = "sendi-${RobotStatus.SERIAL_NUMBER}"
//        EngineConstants.serialNumber = "iflytek-test"
        LogUtil.i("sn : " + EngineConstants.serialNumber)
        //对音频的处理为降噪唤醒再送去识别,
        SystemRecorder.AUDIO_TYPE_ASR = false
        //初始化wakeupEngine(降噪+唤醒)
        val initResult = WakeupEngine.getInstance(wakeupListener)
        if (initResult == 0) {
            LogUtil.i("wakeupEngine初始化成功")
        } else {
            LogUtil.i(
                "wakeupEngine初始化失败，错误码$initResult " + ErrorCode.getError(
                    initResult
                ) + "  \n错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol")
            LogUtil.i( "wakeupEngine初始化失败")
        }

        //初始化录音
        if (recorder == null) {
            recorder = RecorderFactory.getRecorder()
        }
        if (recorder != null) {
            LogUtil.i("录音机初始化成功")
        } else {
            LogUtil.i("录音机初始化失败")
        }
    }

    private fun startRecord() {
        if (recorder != null) {
            val ret = recorder!!.startRecord()
            if (0 == ret) {
                LogUtil.i("开启录音成功！")
            } else if (111111 == ret) {
                LogUtil.i("异常,AlsaRecorder is null ...")
            } else {
                LogUtil.i("开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！")
                destroyRecord()
            }
        }
    }

    private fun stopRecord() {
        if (recorder != null) {
            recorder!!.stopRecord()
            LogUtil.i("停止录音")
        }
    }

    private fun destroyRecord() {
        stopRecord()
        recorder = null
        LogUtil.i("destroy is Done!")
    }

    protected fun quitFragment(){
        thread {
            LogUtil.i("quitFragment")
            if (EngineConstants.isRecording) {
                stopRecord()
            }
            if (recorder != null) {
                recorder!!.destroyRecord()
                recorder = null
            }
            if (wakeupListener != null) {
                wakeupListener = null
            }
            WakeupEngine.destroy()
            LogUtil.i("quitFragment is Done!")
        }
    }

    protected fun navigateToFragment(fragmentId: Int, args: Bundle? = null){
        quitFragment()
        findNavController().navigate(fragmentId,args)
    }
}