package com.sendi.deliveredrobot.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.sendi.deliveredrobot.model.ReplyIntentModel
import com.sendi.fooddeliveryrobot.GetVFFileToTextModel
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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
        consultationTime: String = "${System.currentTimeMillis() / 1000}",
        questionID: String = "0"
    ): GetVFFileToTextModel? {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
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
        val url = HttpUrl.Builder()
            .scheme("http")
            .host("app.yuexiu.gov.cn")
            .addPathSegments("wechatWork/automaticResponse/generateReplyToX8")
            .addQueryParameter("questionID", questionID)
            .build()
        val request = Request.Builder()
//            .url("http://app.yuexiu.gov.cn/wechatWork/automaticResponse/generateReplyToX8")
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()
        return try {
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            LogUtil.i("generateReplyToX8耗时${(System.currentTimeMillis() - startTime) / 1000f}s")
            val data = response.body?.string() ?: ""
            LogUtil.i(data)
            gson.fromJson(data, GetVFFileToTextModel::class.java)
        } catch (e: IOException) {
            LogUtil.e(e.toString())
            null
        } catch (e: JsonSyntaxException) {
            LogUtil.e(e.toString())
            null
        }
    }

    fun getReplyInfoModel(getVFFileToTextModel: GetVFFileToTextModel?): ReplyIntentModel{
        try {
            return ReplyIntentModel(
                images = null,
                questionAnswer = getVFFileToTextModel?.data?.reply,
                questionNumber = getVFFileToTextModel?.data?.id?.toLong(),
                code = getVFFileToTextModel?.code,
                type = null,
                videos = null,
                frames = null
            )
        }catch (e:Exception){
            return ReplyIntentModel(
                images = null,
                questionAnswer = null,
                questionNumber = null,
                code = null,
                type = null,
                videos = null,
                frames = null
            )
        }
    }
}