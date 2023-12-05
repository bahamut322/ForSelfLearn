package com.sendi.fooddeliveryrobot

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.Stack
import kotlin.concurrent.thread


@SuppressLint("MissingPermission")
class VoiceRecorder {
    private val sampleRate = 16000
    private var audioRecorder: AudioRecord? = null
    private var fvad: FvadWrapper? = null

    private var isRecording = false
    private var baseAudioChannel: BaseAudioChannel? = null

    private val minBufferSize: Int
    var recordCallback: ((conversation: String, pinyinString: String)-> Unit)? = null
        set(value) {
            baseAudioChannel?.callback = value
            field = value
        }

    var talkingCallback: ((talking: Boolean) -> Unit)? = null

    init {
        fvad = FvadWrapper()
        fvad?.setMode(3)
        fvad?.setSampleRate(16000)
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        println("permission granted")
        //双通道应该传的值
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        //读取麦克风的数据
//        audioRecorder = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            sampleRate, channelConfig,
//            AudioFormat.ENCODING_PCM_16BIT, minBufferSize
//        )
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate, channelConfig,
            AudioFormat.ENCODING_PCM_16BIT, minBufferSize
        )
        baseAudioChannel = initAudioChannel()
    }

    abstract fun initAudioChannel(): BaseAudioChannel?{

        return null
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        println("startRecording")
        thread {
            audioRecorder?.startRecording()
            isRecording = true
            val audioBuffer = ByteArray(minBufferSize)
            val trueStack = Stack<Boolean>()
            val falseStack = Stack<Boolean>()
            while (isRecording) {
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
                if (!ttsIsPlaying && baseAudioChannel?.initialized == true && recordCallback != null) {
                    baseAudioChannel?.writeRecord(audioBuffer)
                }
                if (recordCallback != null) {
                    when (fvad?.process(shortArray) ?: -1) {
                        0 -> {
                            talkingCallback?.invoke(false)
                            if (baseAudioChannel?.initialized == true) {
                                falseStack.push(false)
                                val totalSize = trueStack.size * 1.5 + falseStack.size
                                if (trueStack.size > 0) {
                                    val percentFalseTotal = (falseStack.size * 1f) / totalSize
                                    if (percentFalseTotal > 0.5f) {
                                        println("trueSize:${trueStack.size}")
                                        println("falseSize:${falseStack.size}")
                                        println("totalSize: $totalSize")
                                        println("percentFalseTotal: $percentFalseTotal")
                                        baseAudioChannel?.stopRecord()
                                        trueStack.clear()
                                        falseStack.clear()
                                    }
                                }
                            }
                        }
                        1 -> {
//                        Log.i("VoiceRecorder", "人声 yes")
                            talkingCallback?.invoke(true)
                            trueStack.push(true)
                            if (baseAudioChannel?.initialized == false && recordCallback != null) {
                                baseAudioChannel?.initRecord(audioBuffer)
                            }

                        }
                        else -> {
                            println("talking 未知")
                        }
                    }
                }
            }
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
    }

    companion object{
        private var _instance: VoiceRecorder? = null
        var ttsIsPlaying = false
        fun getInstance():VoiceRecorder{
            if(_instance == null){
                _instance = VoiceRecorder()
            }
            return _instance!!
        }
    }
}