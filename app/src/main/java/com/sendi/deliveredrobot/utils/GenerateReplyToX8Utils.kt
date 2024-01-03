package com.sendi.deliveredrobot.utils

import android.os.SystemClock
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sendi.fooddeliveryrobot.GetVFFileToTextModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object GenerateReplyToX8Utils {
    private val gson = Gson()
    fun generateReplyToX8(
        consultationContent: String,
        consultationType: Int = 0,
        customerWechat: String = "",
        wxid: String = "X8",
        consultationTime: String = "${System.currentTimeMillis() / 1000}"
    ): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
        val mediaType = "application/json".toMediaTypeOrNull()
        val getVFFileToTextModel = JsonObject().apply {
            addProperty("consultationContent", consultationContent)
            addProperty("consultationType", consultationType)
            addProperty("customerWechat", customerWechat)
            addProperty("wxid", wxid)
            addProperty("consultationTime", consultationTime)
        }
        val requestBody = getVFFileToTextModel.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://app.yuexiu.gov.cn/wechatWork/automaticResponse/generateReplyToX8")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()
        try {
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            Log.i("AudioChannel", "generateReplyToX8耗时${(System.currentTimeMillis() - startTime) / 1000f}s")
            LogUtil.i("generateReplyToX8耗时${(System.currentTimeMillis() - startTime) / 1000f}s")
            val data = response.body?.string()?:""
//            LogUtil.i(data)
            val getVFFileToTextModel = gson.fromJson(data, GetVFFileToTextModel::class.java)
            return getVFFileToTextModel?.data ?: "网络超时，请稍后重试..."
        } catch (e: IOException) {
            LogUtil.e(e.toString())
        }
        return "网络错误,请稍后重试..."
    }
}