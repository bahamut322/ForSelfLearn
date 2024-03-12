package com.sendi.fooddeliveryrobot

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.Stack
import kotlin.concurrent.thread


@SuppressLint("MissingPermission")
abstract class BaseVoiceRecorder {
    private val sampleRate = 16000
    protected var audioRecorder: AudioRecord? = null
    private var fvad: FvadWrapper? = null
    private var isRecording = false
    private var baseAudioChannel: BaseAudioChannel? = null

    private val minBufferSize: Int
    var recordCallback: ((conversation: String, pinyinString: String, takeTime:Float)-> Unit)? = null
        set(value) {
            baseAudioChannel?.callback = value
            field = value
        }

    var talkingCallback: ((talking: Boolean) -> Unit)? = null

    var recordStatusCallback: ((startRecord: Boolean) -> Unit)? = null

    init {
        fvad = FvadWrapper()
        fvad?.setMode(3)
        fvad?.setSampleRate(16000)
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        println("permission granted")
        //双通道应该传的值
        val channelConfig = AudioFormat.CHANNEL_IN_STEREO
        //读取麦克风的数据
//        audioRecorder = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            sampleRate, channelConfig,
//            AudioFormat.ENCODING_PCM_16BIT, minBufferSize
//        )
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate, channelConfig,
            AudioFormat.ENCODING_PCM_16BIT, minBufferSize
        ).apply {

        }
        baseAudioChannel = initAudioChannel()
    }

    abstract fun initAudioChannel(): BaseAudioChannel?

    @SuppressLint("MissingPermission")
    fun startRecording() {
        Log.i("AudioChannel", "startRecording")
        thread {
            audioRecorder?.startRecording()
            isRecording = true
            val audioBuffer = ByteArray(minBufferSize)
//            print("minBufferSize:$minBufferSize")
            val trueStack = Stack<Boolean>()
            val falseStack = Stack<Boolean>()
            while (isRecording) {
                Log.i("AudioChannel", "isRecording")
                val result = audioRecorder?.read(audioBuffer, 0, minBufferSize) ?: 0
                if (result == AudioRecord.ERROR_INVALID_OPERATION || result == AudioRecord.ERROR_BAD_VALUE) {
                    continue
                }
                val shortArray = ShortArray(320)
                for ((index) in audioBuffer.withIndex()) {
                    if(index >= 640){
                        break
                    }
                    if (index % 2 == 1) {
                        shortArray[index / 2] = (audioBuffer[index - 1].toInt() and 0xff or (audioBuffer[index].toInt() shl 8)).toShort()
                    }
                }
                if (recordCallback != null) {
                    when (fvad?.process(shortArray) ?: -1) {
                        0 -> {
//                            Log.i("AudioChannel", "人声 no")
                            talkingCallback?.invoke(false)
                            if (baseAudioChannel?.initialized == true) {
                                falseStack.push(false)
//                                val totalSize = trueStack.size * 0.7 + falseStack.size
                                if (trueStack.size > 0) {
//                                    val percentFalseTotal = (falseStack.size * 1f) / totalSize
                                    if(falseStack.size > 20){
//                                        println("trueSize:${trueStack.size}")
//                                        println("falseSize:${falseStack.size}")
//                                        println("totalSize: $totalSize")
//                                        println("percentFalseTotal: $percentFalseTotal")
                                        recordStatusCallback?.invoke(false)
                                        baseAudioChannel?.stopRecord()
                                        trueStack.clear()
                                        falseStack.clear()
                                    }
                                }
                            }
                        }
                        1 -> {
                            Log.i("AudioChannel", "人声 yes")
                            talkingCallback?.invoke(true)
                            trueStack.push(true)
                            falseStack.clear()
                            if (!ttsIsPlaying && baseAudioChannel?.initialized == false && recordCallback != null) {
                                recordStatusCallback?.invoke(true)
                                baseAudioChannel?.initRecord()
                            }

                        }
                        else -> {
                            Log.i("AudioChannel", "人声 unknown")
                        }
                    }
                }
                if (!ttsIsPlaying && baseAudioChannel?.initialized == true && recordCallback != null) {
                    baseAudioChannel?.writeRecord(audioBuffer)
                }
            }
            Log.i("AudioChannel", "stopRecording")
        }

    }
    fun stopRecording() {
        isRecording = false
        audioRecorder?.stop()
        audioRecorder?.release()
        fvad?.destroy()
    }

    fun removeCallback(){
        recordCallback = null
        recordStatusCallback = null
    }

    fun clearCache(){
        baseAudioChannel?.clearCache()
    }

    companion object{
        private var _instance: BaseVoiceRecorder? = null
        var ttsIsPlaying = false
        const val VOICE_RECORD_TYPE_SENDI = 0
        const val VOICE_RECORD_TYPE_AIXIAOYUE = 1
        var VOICE_RECORD_TYPE = -1
        fun getInstance():BaseVoiceRecorder?{
            if(_instance == null){
                _instance = when (VOICE_RECORD_TYPE) {
                    VOICE_RECORD_TYPE_SENDI -> SendiVoiceRecorder()
                    VOICE_RECORD_TYPE_AIXIAOYUE -> AiXiaoYueVoiceRecorder()
                    else -> null
                }
            }
            return _instance
        }

        fun release(){
            _instance = null
        }
    }
}