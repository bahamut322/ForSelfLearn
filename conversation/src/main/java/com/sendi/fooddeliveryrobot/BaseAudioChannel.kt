package com.sendi.fooddeliveryrobot

import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.sendi.fooddeliveryrobot.util.PcmToWavUtil
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

abstract class BaseAudioChannel(audioRecord: AudioRecord) {
    private var audioRecord: AudioRecord? = null
    private var writer: FileOutputStream? = null
    protected var time: String? = null
    var initialized = false
    protected val gson = Gson()
    private var baseAudioChannel: BaseAudioChannel? = null
    protected var retrofit: Retrofit? = null
    protected var body:RequestBody? = null
    var callback: ((conversation: String, pinyinString: String, takeTime:Float)-> Unit)? = null
    protected var url: String = ""

    init {
        this.audioRecord = audioRecord
        initRetrofit()
    }

    protected abstract fun initRetrofit()

    private fun write(byteArray: ByteArray) {
        if (initialized){
            writer?.write(byteArray,0,byteArray.size)
        }
    }


    private fun init() {
        synchronized(this){
            Log.i("AudioChannel", "init")
            initialized = true
            time = "record"
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = FileOutputStream(
                    Environment.getExternalStorageDirectory().toString() + "/" + time + ".pcm", true
                )

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopWrite(){
        synchronized(this){
            Log.i("AudioChannel", "stopWrite")
            initialized = false
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            PcmToWavUtil(
                audioRecord?.sampleRate?:44100, audioRecord?.channelConfiguration?:AudioFormat.CHANNEL_IN_MONO,
                audioRecord?.channelCount?:2, AudioFormat.ENCODING_PCM_16BIT
            ).pcmToWav(
                Environment.getExternalStorageDirectory().toString() + "/" + time + ".pcm",
                Environment.getExternalStorageDirectory().toString() + "/" + time + ".wav"
            )
            call()
        }
//        Log.i("AudioChannel", "AudioChannel run finish ")
    }

    protected abstract fun call()

    private fun stopLive() {
        stopWrite()
    }

    fun writeRecord(byteArray: ByteArray?) {
        if (byteArray == null || byteArray.isEmpty()) {
            Log.e("AudioChannel", "byteArray is null")
            return
        }
        write(byteArray)
    }

    fun stopRecord(){
        stopLive()
        baseAudioChannel = null
    }

    fun clearCache(){
        synchronized(this) {
            Log.i("AudioChannel", "clearCache")
            initialized = false
            writer?.close()
            val filePcm = Environment.getExternalStorageDirectory().toString() + "/" + time + ".pcm"
            val fileWav = Environment.getExternalStorageDirectory().toString() + "/" + time + ".wav"
            val filePcmFile = File(filePcm)
            val fileWavFile = File(fileWav)
            if (filePcmFile.exists()) {
                filePcmFile.delete()
            }
            if (fileWavFile.exists()) {
                fileWavFile.delete()
            }
        }
    }

    fun initRecord(byteArray: ByteArray){
        if (!initialized) {
            init()
            writeRecord(byteArray)
        }
    }
}