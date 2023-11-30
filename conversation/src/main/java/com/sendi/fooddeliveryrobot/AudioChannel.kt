package com.sendi.fooddeliveryrobot

import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.sendi.fooddeliveryrobot.util.HanziToPinyin
import com.sendi.fooddeliveryrobot.util.PcmToWavUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioChannel(audioRecord: AudioRecord) {
    private var audioRecord: AudioRecord? = null
    private var writer: FileOutputStream? = null
    private var time: String? = null
    var initialized = false
    private val gson = Gson()
    private var audioChannel: AudioChannel? = null
    private var retrofit: Retrofit? = null
    private var body:RequestBody? = null
    var callback: ((conversation: String, pinyinString: String)-> Unit)? = null

    init {
        this.audioRecord = audioRecord
        retrofit = Retrofit.Builder() //设置网络请求BaseUrl地址
            .baseUrl("http://192.168.60.203:7721/") //设置数据解析器
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        body = RequestBody.create(MediaType.parse("text/plain"), "普通话")
    }

    private fun write(byteArray: ByteArray) {
        if (initialized){
            writer?.write(byteArray,0,byteArray.size)
        }
    }


    private fun init() {
        synchronized(this){
            Log.i("AudioChannel", "init")
            initialized = true
            time = "${System.currentTimeMillis()}"
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

            val filePcm = File(Environment.getExternalStorageDirectory().toString() + "/" + time + ".pcm")
            val fileWav = File(Environment.getExternalStorageDirectory().toString() + "/" + time + ".wav")
            val fileBody = RequestBody.create(MediaType.parse("audio/*"), fileWav)
            val part = MultipartBody.Part.createFormData("file", fileWav.name, fileBody)
            val call = retrofit?.create(ApiService::class.java)?.uploadFile(body, part)
            call?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        val s = response.body()?.string()
                        if (!s.isNullOrEmpty()) {
                            Log.i("AudioChannel", s)
                            val audioTransTextModel = gson.fromJson(s, AudioTransTextModel::class.java)
                            val textProcessed = audioTransTextModel.text_postprocessed?:""
                            val resultList = HanziToPinyin.instance?.get(textProcessed)
                            val stringBuilder = StringBuilder()
                            resultList?.map {
                                Log.i("AudioChannel", it.toString())
                                stringBuilder.append(it.target)
                            }
                            callback?.invoke(textProcessed, stringBuilder.toString())
                        }
                    } catch (e: IOException) {
//                        throw RuntimeException(e)
                    }finally {
                        fileWav.delete()
                        filePcm.delete()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("AudioChannel", t.message!!)
                    fileWav.delete()
                    filePcm.delete()
                }
            })
        }
//        Log.i("AudioChannel", "AudioChannel run finish ")
    }

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
        audioChannel = null
    }

    fun initRecord(byteArray: ByteArray){
        if (!initialized) {
            init()
            writeRecord(byteArray)
        }
    }
}