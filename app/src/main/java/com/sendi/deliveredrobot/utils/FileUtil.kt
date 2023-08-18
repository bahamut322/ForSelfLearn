package com.sendi.deliveredrobot.utils

import android.os.Environment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @description 文件处理类
 * @author heky
 * @date 2021-12-15
 */
object FileUtil {

    /**
     * @description 遍历Log文件夹，大于30天则删除
     */
    fun checkAndDeleteLogFilesCache(){
        val time = System.currentTimeMillis()
        val directory = File("${Environment.getExternalStorageDirectory()}/logger")
        if (!directory.exists()) return
        directory.listFiles()?.map {
            if((time - it.lastModified()) / 1000 / 60 / 60 / 24 > 30){
                //如果相差大于30天
                it.delete()
            }
        }
    }
    fun uploadFile(URL: String, fullPath: String, robotId: String, path: String) {

        // 创建 OkHttpClient 对象
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        // 构建上传文件的请求体
        val mediaType = "multipart/form-data".toMediaTypeOrNull()
        val file = File(fullPath)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("robotId", robotId)
            .addFormDataPart("path", path)
            .addFormDataPart("multipartFile", file.name, file.asRequestBody(mediaType))
            .build()

        // 构建上传文件的请求
        val request = Request.Builder()
            .url(URL)
            .post(requestBody)
            .build()

        // 发送上传文件的请求
        try {
            val response = client.newCall(request).execute()
            LogUtil.i(response.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}