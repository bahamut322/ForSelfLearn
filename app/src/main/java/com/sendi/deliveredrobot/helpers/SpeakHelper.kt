package com.sendi.deliveredrobot.helpers

import com.iflytek.aikitdemo.ability.AbilityCallback
import com.iflytek.aikitdemo.ability.AbilityConstant
import com.iflytek.aikitdemo.ability.tts.TtsHelper
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.BasicModel
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
    private const val PATTERN = "(((htt|ft|m)ps?):\\/\\/)?([\\da-zA-Z\\.-]+)\\.?([a-z]{2,6})(:\\d{1,5})?([\\/\\w\\.-]*)*\\/?([#=][\\S]+)?"
    const val TYPE_BAIDU = "0"
    const val TYPE_XTTS = "1"
    private var SPEAK_TYPE = ""
    val mainScope = MainScope()
    val list = LinkedList<SpeakModel>()
    val speakCallback = object : SpeakCallback {
        override fun speakFinish(utteranceId: String) {
//            Log.i("SpeakHelper", "speakFinish: $utteranceId")
//            Log.i("SpeakHelper", "list: ${list.size}")
            when (getType()) {
                TYPE_BAIDU -> {
                    playNext(utteranceId)
                }
                TYPE_XTTS -> {
                    speakUserCallback?.speakAllFinish()
                }
            }
        }

        override fun progressChange(utteranceId: String, progress: Int) {
            when (getType()) {
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
                    speakUserCallback?.progressChange("", progress)
                }
            }

        }
    }
    var id: Long = 0

    var speakUserCallback: SpeakUserCallback? = null

    var startId: Long? = null // 用于记录speakWithoutStop时的开始id

    private var aiSoundHelper: TtsHelper? = null
    private var basicModel: BasicModel? = null

    fun getType(): String{
        return SPEAK_TYPE
    }

    fun setType(ttsType: String){
        SPEAK_TYPE = ttsType
    }
    fun initTTS(){
        when(getType()){
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance()
            }
            TYPE_XTTS -> {
                basicModel = QuerySql.QueryBasic()
                initXTTS()
            }
        }
    }

    fun speak(msg: String, utteranceId: String = "") {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
            when(getType()){
                TYPE_BAIDU -> {
                    stop()
                    BaiduTTSHelper.getInstance().speak(msg, utteranceId)
                }
                TYPE_XTTS -> {
                    xttsSpeech(msg)
                }
            }
        }
    }


    fun speakWithoutStop(msg: String) {
        if(RobotStatus.currentStatus == TYPE_EXCEPTION) return
        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
        if (BuildConfig.IS_SPEAK) {
//            mainScope.launch(Dispatchers.IO) {
            when(getType()){
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
                    xttsSpeech(msg)
                }
            }
        }
    }

    fun speaks(text: String){
        when (getType()) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().speaks(text)
            }

            TYPE_XTTS -> {
                if (text.isEmpty()) return
                Universal.explainTextLength = text.length
                xttsSpeech(text)
            }
        }

    }

    fun stop() {
        when (getType()) {
            TYPE_BAIDU -> {
                list.clear()
                BaiduTTSHelper.getInstance().stop()
                RobotStatus.ttsIsPlaying = false
            }
            TYPE_XTTS -> {
                aiSoundHelper?.stop()
                RobotStatus.ttsIsPlaying = false
            }
        }

    }

    fun pause() {
        when (getType()) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().pause()
            }
            TYPE_XTTS -> {
                aiSoundHelper?.pause()
            }
        }
    }

    fun resume() {
        when (getType()) {
            TYPE_BAIDU -> {
                BaiduTTSHelper.getInstance().resume()
            }
            TYPE_XTTS -> {
                aiSoundHelper?.resume()
            }
        }
    }

    fun setParam(params: Map<String, String>?, voiceType: Int?){
        when (getType()) {
            TYPE_BAIDU -> {
                if (params == null || voiceType == null) return
                val voiceStr = when (voiceType) {
                    1 -> OfflineResource.VOICE_FEMALE
                    2 -> OfflineResource.VOICE_MALE
                    3 -> OfflineResource.VOICE_DUYY
                    4 -> OfflineResource.VOICE_FEMALE
                    else -> OfflineResource.VOICE_FEMALE
                }
                BaiduTTSHelper.getInstance().setParam(params, voiceStr)
            }
            TYPE_XTTS -> {
                val vcn = when (voiceType) {
                    1 -> "xiaoyan"
                    2 -> "xiaofeng"
                    3 -> "xiaoyan"
                    4 -> "xiaomei"
                    else -> "xiaoyan"
                }
                setVCN(vcn)
            }
        }
    }

    private fun setVCN(vcn: String){
        when (getType()) {
            TYPE_BAIDU -> {
            }
            TYPE_XTTS -> {
                aiSoundHelper?.setVCN(vcn)
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

    private fun initXTTS() {
        if (aiSoundHelper != null) {
            aiSoundHelper!!.release()
        }
        aiSoundHelper = TtsHelper().apply {
            val callback = object : AbilityCallback {

                override fun onAbilityBegin() {
                    LogUtil.i("xtts开始合成数据")
                }

                override fun onAbilityResult(result: String) {
//                    startTime = System.currentTimeMillis()
                }

                override fun onAbilityError(code: Int, error: Throwable?) {
                    LogUtil.i("xtts合成失败:${error?.message}")
                }

                override fun onAbilityEnd() {
                    LogUtil.i("xtts播放结束=====\n")
                    speakUserCallback?.speakAllFinish()
                    if (aiSoundHelper != null) {
                        aiSoundHelper!!.stop()
                    }
                }
            }
            onCreate(AbilityConstant.XTTS_ID, callback, object: TtsHelper.XTTSCallback{
                override fun speakAllFinish() {
                    speakUserCallback?.speakAllFinish()
                }

                override fun progressChange(progress: Int) {
                    speakUserCallback?.progressChange("", progress)
                }
            })
        }

    }

    private fun xttsSpeech(text: String){
        aiSoundHelper?.apply {
            AudioMngHelper(MyApplication.instance).setVoice100(50)
            var speed = basicModel?.speechSpeed?.times(7f)?.toInt()?:50
            speed = speed.let {
                if(it > 100) 100
                else it
            }
            setSpeed(speed)
            setVolume(basicModel?.voiceVolume?:50)
            setPitch(50)
            speechText(text.replace(Regex(PATTERN),""))
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