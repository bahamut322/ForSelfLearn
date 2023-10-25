package com.sendi.fooddeliveryrobot

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.Stack
import kotlin.concurrent.thread


@SuppressLint("MissingPermission")
class VoiceRecorder {
    private val sampleRate = 44100
    private var audioRecorder: AudioRecord? = null
    private var fvad: FvadWrapper? = null

    private var isRecording = false
    private var audioChannel: AudioChannel? = null

    private val minBufferSize: Int
    var callback: ((conversation: String, pinyinString: String)-> Unit)? = null
        set(value) {
            audioChannel?.callback = value
            field = value
        }

    init {
        fvad = FvadWrapper()
        fvad?.setMode(3)
        fvad?.setSampleRate(16000)
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        println("permission granted")
        //双通道应该传的值
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        //读取麦克风的数据
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate, channelConfig,
            AudioFormat.ENCODING_PCM_16BIT, minBufferSize
        )
        audioChannel = AudioChannel(audioRecorder!!)
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        println("startRecording")
        audioRecorder?.startRecording()
        isRecording = true
        thread {
            val audioBuffer2 = ByteArray(minBufferSize)
            println("startRecording thread")
            val booleanStack = Stack<Boolean>()
            while (isRecording) {
                val result = audioRecorder?.read(audioBuffer2, 0, minBufferSize) ?: 0
                if (result == AudioRecord.ERROR_INVALID_OPERATION || result == AudioRecord.ERROR_BAD_VALUE) {
                    continue
                }
                val shortArray = ShortArray(320)
                for ((index) in audioBuffer2.withIndex()) {
                    if(index >= 640){
                        break
                    }
                    if (index % 2 == 1) {
                        shortArray[index / 2] = (audioBuffer2[index - 1].toInt() and 0xff or (audioBuffer2[index].toInt() shl 8)).toShort()
                    }
                }
                when (fvad?.process(shortArray) ?: -1) {
                    0 -> {
//                        Log.e("VoiceRecorder", "人声 no")
                        if (AudioChannel.isRecoding) {
                            booleanStack.push(false)
                            if (booleanStack.size > 100) {
                                var falseCount = 0f
                                var trueCount = 0f
                                booleanStack.map {
                                    if (it) {
                                        trueCount++
                                    } else {
                                        falseCount++
                                    }
                                }
                                if (falseCount / trueCount > 0.8f) {
                                    audioChannel?.stopRecord()
                                    booleanStack.clear()
                                }
                            }
                        }
                    }
                    1 -> {
//                        Log.i("VoiceRecorder", "人声 yes")
                        booleanStack.push(true)
                        audioChannel?.startRecord(audioBuffer2)
                    }
                    else -> {
//                        Log.e("VoiceRecorder", "人声 unknown")
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

    companion object{
        private var _instance: VoiceRecorder? = null

        fun getInstance():VoiceRecorder{
            if(_instance == null){
                _instance = VoiceRecorder()
            }
            return _instance!!
        }
    }
}