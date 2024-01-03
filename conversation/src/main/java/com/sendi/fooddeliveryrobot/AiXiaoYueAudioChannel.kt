package com.sendi.fooddeliveryrobot

import android.media.AudioRecord
import android.os.Environment
import android.util.Log
import com.sendi.fooddeliveryrobot.util.HanziToPinyin
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
import java.io.IOException

class AiXiaoYueAudioChannel(audioRecord: AudioRecord): BaseAudioChannel(audioRecord) {
    override fun initRetrofit() {
        url = "http://app.yuexiu.gov.cn/"
        retrofit = Retrofit.Builder() //设置网络请求BaseUrl地址
            .baseUrl(url) //设置数据解析器
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override fun call() {
        val filePcm = File(Environment.getExternalStorageDirectory().toString() + "/" + time + ".pcm")
        val fileWav = File(Environment.getExternalStorageDirectory().toString() + "/" + time + ".wav")
        val fileBody = RequestBody.create(MediaType.parse("audio/*"), fileWav)
//        val part = MultipartBody.Part.createFormData("file", fileWav.name, fileBody)
        val part = MultipartBody.Part.createFormData("File", fileWav.name, fileBody)
//        val call = retrofit?.create(ApiService::class.java)?.uploadFile(body, part)
        val call = retrofit?.create(ApiService::class.java)?.uploadFile2(part)
        val startTime = System.currentTimeMillis()
        call?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val takeTime = (System.currentTimeMillis() - startTime) / 1000f
                    val s = response.body()?.string()
                    if (!s.isNullOrEmpty()) {
                        Log.i("AudioChannel", s)
//                        val audioTransTextModel = gson.fromJson(s, AudioTransTextModel::class.java)
                        val audioTransTextModel = gson.fromJson(s, GetVFFileToTextModel::class.java)
                        val textProcessed = audioTransTextModel.data?:""
                        if (textProcessed.isEmpty()) {
                            return
                        }
                        val resultList = HanziToPinyin.instance?.get(textProcessed)
                        val stringBuilder = StringBuilder()
                        resultList?.map {
//                            Log.i("AudioChannel", it.toString())
                            stringBuilder.append(it.target)
                        }
                        callback?.invoke(textProcessed, stringBuilder.toString(),takeTime)
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
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
}