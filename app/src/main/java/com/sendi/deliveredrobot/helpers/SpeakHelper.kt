package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.MainScope
import java.util.LinkedList

/**
 *   @author: heky
 *   @date: 2021/8/12 14:52
 *   @describe: 语音播报包装类
 */
object SpeakHelper {
    private const val TYPE_BAIDU = 0
    private const val TYPE_XTTS = 1
    private var SPEAK_TYPE = TYPE_XTTS
    val mainScope = MainScope()
    val list = LinkedList<SpeakModel>()
    val speakCallback = object : SpeakCallback {
        override fun speakFinish(utteranceId: String) {
//            Log.i("SpeakHelper", "speakFinish: $utteranceId")
//            Log.i("SpeakHelper", "list: ${list.size}")
            when (SPEAK_TYPE) {
                TYPE_BAIDU -> {
                    playNext(utteranceId)
                }
                TYPE_XTTS -> {
                    // todo
                }
            }
        }

        override fun progressChange(utteranceId: String, progress: Int) {
            when (SPEAK_TYPE) {
                TYPE_BAIDU -> {
                    var actualProgress = progress
                    if (startId != null) {
                        try {
                            val utteranceIdTrans = (utteranceId.toLongOrNull())
                            if (utteranceIdTrans != null) {
                                val offset = utteranceIdTrans - (startId as Long)
                                actualProgress = (offset * 60 + progress).toInt()
                            }
                        }catch (e: ClassCastException){
                            LogUtil.e("utteranceId转换异常")
                        }
                    }
                    speakUserCallback?.progressChange(utteranceId, actualProgress)
                }
                TYPE_XTTS -> {
                    // todo
                }
            }

        }
    }
    var id: Long = 0

    var speakUserCallback: SpeakUserCallback? = null

    var startId: Long? = null // 用于记录speakWithoutStop时的开始id

    fun initTTS(){
        when(SPEAK_TYPE){
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance()
            }
            TYPE_XTTS -> {
                // todo
            }
        }
    }

    fun speak(msg: String, utteranceId: String = "") {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
            when(SPEAK_TYPE){
                TYPE_BAIDU -> {
                    stop()
                    BaiduTTSHelper.getInstance().speak(msg, utteranceId)
                }
                TYPE_XTTS -> {
                    // todo
                }
            }
        }
    }


    fun speakWithoutStop(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
//            mainScope.launch(Dispatchers.IO) {
            when(SPEAK_TYPE){
                TYPE_BAIDU -> {
                    startId = id
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

                TYPE_XTTS -> {
                    // todo
                }
            }
        }
    }

    fun speaks(text: String?){
        when (SPEAK_TYPE) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().speaks(text)
            }

            TYPE_XTTS -> {
                // todo
            }
        }

    }

    fun stop() {
        when (SPEAK_TYPE) {
            TYPE_BAIDU -> {
                list.clear()
                BaiduTTSHelper.getInstance().stop()
                RobotStatus.ttsIsPlaying = false
            }
            TYPE_XTTS -> {
                // todo
            }
        }

    }

    fun pause() {
        when (SPEAK_TYPE) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().pause()
            }
            TYPE_XTTS -> {
                // todo
            }
        }
    }

    fun resume() {
        when (SPEAK_TYPE) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().resume()
            }
            TYPE_XTTS -> {
                // todo
            }
        }
    }

    fun setParam(params: Map<String, String>?, voiceType: String?){
        when (SPEAK_TYPE) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().setParam(params, voiceType)
            }
            TYPE_XTTS -> {
                // todo
            }
        }
    }

    private fun playNext(utteranceId: String){
        if(list.size > 0){
            if (list.first?.id == utteranceId) {
                list.pop()
                if (list.size > 0) {
                    BaiduTTSHelper.getInstance().speak(list.first.content, list.first.id)
                } else {
                    RobotStatus.ttsIsPlaying = false
                    utteranceId.isNotEmpty().let {
                        try {
                            utteranceId.toLong()
                            speakUserCallback?.speakAllFinish()
                        } catch (e: Exception) {

                        }
                    }
                    resetStartId()
                }
            } else {
                list.pop()
                playNext(utteranceId)
            }
        }else{
            RobotStatus.ttsIsPlaying = false
            utteranceId.isNotEmpty().let {
                try {
                    utteranceId.toLong()
                    speakUserCallback?.speakAllFinish()
                } catch (e: Exception) {

                }
            }
            resetStartId()
        }
    }

    fun setUserCallback(callback: SpeakUserCallback){
        speakUserCallback = callback
    }

    fun releaseUserCallback(){
        speakUserCallback = null
    }

    private fun playFirst(){
        if(list.size > 0){
            BaiduTTSHelper.getInstance().speak(list.first.content, list.first.id)
        }
    }

    private fun resetStartId(){
        startId = null
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