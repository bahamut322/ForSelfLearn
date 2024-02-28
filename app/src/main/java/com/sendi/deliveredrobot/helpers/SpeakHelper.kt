package com.sendi.deliveredrobot.helpers

import android.util.Log
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import kotlinx.coroutines.MainScope
import java.util.LinkedList

/**
 *   @author: heky
 *   @date: 2021/8/12 14:52
 *   @describe: 语音播报包装类
 */
object SpeakHelper {
    val mainScope = MainScope()
    val list = LinkedList<SpeakModel>()
    val speakCallback = object : SpeakCallback {
        override fun speakFinish(utteranceId: String) {
            Log.i("SpeakHelper", "speakFinish: $utteranceId")
            Log.i("SpeakHelper", "list: ${list.size}")
            playNext(utteranceId)
        }

        override fun progressChange(utteranceId: String, progress: Int) {
            speakUserCallback?.progressChange(utteranceId, progress)
        }
    }
    var id: Long = 0

    var speakUserCallback: SpeakUserCallback? = null

    fun speak(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
            stop()
            AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().voiceVolume)
//            mainScope.launch(Dispatchers.IO) {
                BaiduTTSHelper.getInstance().speak(msg, "")
//            }
        }
    }

    fun speakWithoutStop(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
//            mainScope.launch(Dispatchers.IO) {
            var result = msg
            if(result.length > 60){
                while(result.length > 60){
                    val result1 = result.substring(0,60)
                    result = result.substring(60)
                    list.add(SpeakModel("${id++}", result1))
                }
                if(result.isNotEmpty()){
                   list.add(SpeakModel("${id++}", result))
                }
            }else{
                list.add(SpeakModel("${id++}", result))
            }
            if (!RobotStatus.ttsIsPlaying) {
                playFirst()
            }
        }
    }

    fun stop() {
//        mainScope.launch(Dispatchers.IO) {
            BaiduTTSHelper.getInstance().stop()
            RobotStatus.ttsIsPlaying = false
//        }
    }

    fun playNext(utteranceId: String){
        if(list.size > 0){
            if (list.first?.id == utteranceId) {
                list.pop()
                if (list.size > 0) {
                    BaiduTTSHelper.getInstance().speak(list.first.content, list.first.id)
                } else {
                    RobotStatus.ttsIsPlaying = false
                    speakUserCallback?.speakAllFinish()
                }
            } else {
                list.pop()
                playNext(utteranceId)
            }
        }else{
            RobotStatus.ttsIsPlaying = false
            speakUserCallback?.speakAllFinish()
        }
    }

    private fun playFirst(){
        if(list.size > 0){
            BaiduTTSHelper.getInstance().speak(list.first.content, list.first.id)
        }
    }

    interface SpeakCallback {
        fun speakFinish(utteranceId: String)

        fun progressChange(utteranceId: String, progress: Int)
    }

    data class SpeakModel(
        val id: String,
        val content: String
    )

    interface SpeakUserCallback {
        fun speakAllFinish()

        fun progressChange(utteranceId: String, progress: Int)
    }
}