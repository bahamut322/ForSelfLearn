package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Table_Face
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.interfaces.FaceDataListener
import com.sendi.deliveredrobot.model.FaceModel
import com.sendi.deliveredrobot.model.RectDeserializer
import com.sendi.deliveredrobot.model.Similarity
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.identifyFace
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.service.UpdateReturn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.xutils.common.Callback
import org.xutils.common.Callback.CommonCallback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @Author Swn
 * @Data 2023/12/7
 * @describe
 */
class FaceRecognition {
    private val TAG = "人脸TAG"
    var c: Camera? = null
    private var speakNum = 0
    private var canSendData = true
    private var doubleString: ArrayList<Table_Face> = ArrayList()
    private val isProcessing = AtomicBoolean(false)
    private val jsonParams = JSONObject()
    private val faceScope = CoroutineScope(Dispatchers.Default + Job())
    private var manager =
        MyApplication.instance!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val channel = Channel<ByteArray>(capacity = Channel.CONFLATED) // 限制 Channel 大小

    /**
     * @param extractFeature True代表获取人脸特征，默认为True
     * @param width 图片宽
     * @param height 图片高
     * @param owner LifecycleOwner
     * @param needEtiquette 开启人脸检测
     */
    fun suerFaceInit(
        extractFeature: Boolean = false,
        width: Int = 800,
        height: Int = 600,
        owner: LifecycleOwner,
        needEtiquette: Boolean = false
    ) {
        RobotStatus.newUpdata.observe(owner){
            Log.d(TAG, "suerFaceInit: 获取数据")
            doubleString = QuerySql.faceMessage()
        }
        var cameraIds = arrayOfNulls<String>(0)
        try {
            cameraIds = manager.cameraIdList
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        if (cameraIds.isNotEmpty()) {
            //后置摄像头存在
            c = if (cameraIds[0] != null) {
                Camera.open(0) //1，0代表前后摄像头
            } else {
                Camera.open(1) //1，0代表前后摄像头
            }
        }
        c?.setDisplayOrientation(0) //预览图与手机方向一致
        val parameters: Camera.Parameters? = c?.parameters?.apply {
            setPictureSize(width, height)
            setPreviewSize(width, height)
        }
        try {
            c?.parameters = parameters
        } catch (_: Exception) {
        }
        c?.startPreview()
        //对于 YUV_420_SP（NV21）格式，公式为：width * height * 3 / 2
        val buffer = ByteArray((width * height * 3 / 2))
        c?.addCallbackBuffer(buffer)
        //获取摄像实时数据
        c?.setPreviewCallbackWithBuffer { data: ByteArray, _: Camera? ->
            if (data.isNotEmpty() && canSendData) {
                canSendData = false
                channel.trySend(data).isSuccess // 将数据发送到Channel
            }
            c?.addCallbackBuffer(buffer)
        }
        // 启动一个单独的协程来处理数据
        faceScope.launch {
            for (data in channel) { // 从Channel中接收数据
                try {
                    Log.d(TAG, "suerFaceInit人脸识别协程名: ${Thread.currentThread().name}")
                    val bm = decodeByteArrayToBitmap(data, width, height)
                    if (bm != null) {
                        faceHttp(extractFeature, bm, owner, needEtiquette)
                        FaceDataListener.setFaceBit(bm)
                        bm.recycle()
                    }
                }catch (_:Exception){}
            }
        }
    }

    private fun decodeByteArrayToBitmap(data: ByteArray, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val image = YuvImage(data, ImageFormat.NV21, width, height, null)
        val stream = ByteArrayOutputStream()
        try {
            if (image.compressToJpeg(Rect(0, 0, width, height), 75, stream)) {
                val jpegData = stream.toByteArray()
                bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }


    /**
     * @param extractFeature True代表获取人脸特征，默认为True
     * @param bitmap 要识别的的bitmap
     * @param owner LifecycleOwner
     */
    fun faceHttp(
        extractFeature: Boolean = false,
        bitmap: Bitmap,
        owner: LifecycleOwner,
        needEtiquette: Boolean = false
    ) {
        val base64 = bitmapToBase64(bitmap)
        bitmap?.recycle()
        System.gc()
        // 添加参数到JSON对象
        jsonParams["img"] = base64 // 后续需要修改base64
        jsonParams["extract"] = extractFeature
        // 将JSON对象转换为字符串
        val jsonString = jsonParams.toString()
        // 打印请求的JSON数据
        Log.d(TAG, "发送人脸检测请求数据: $jsonString")
        // 创建RequestParams对象
        val params = RequestParams(Universal.POST_FAST) // 替换为你的API端点URL
        params.isAsJsonContent = true // 设置请求内容为JSON
        params.bodyContent = jsonString // 设置请求体为JSON字符串
        // 发送POST请求
        x.http().post(params, object : CommonCallback<String> {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "收到人脸检测数据：$result")
                val gson = GsonBuilder()
                    .registerTypeAdapter(Rect::class.java, RectDeserializer())
                    .create()
                val listType: Type = object : TypeToken<List<FaceModel>>() {}.type
                val faceModelList: List<FaceModel> = gson.fromJson(result, listType)
                FaceDataListener.setFaceModels(faceModelList)
                // Update data
                if (faceModelList.isNotEmpty()) {
                    Log.d(TAG, "人脸检测解析数据：${faceModelList}")
                    if (needEtiquette && !extractFeature) {
                        checkFace(owner)
                    }
                    if (extractFeature) {
                        val allFeatures = faceModelList.map { it.feat }
                        faceIdentify(allFeatures, owner, needEtiquette)
                    }
                }
                //需要人脸识别，但是人脸数据返回为空的时候可以继续发送数据
                if (extractFeature && faceModelList.isEmpty()) {
                    canSendData = true
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {
                // 请求出错，处理错误信息
                canSendData = true
                Log.i(TAG, "人脸检测请求出错: $ex")
            }

            override fun onCancelled(cex: Callback.CancelledException) {
                // 请求被取消，处理取消请求'
                Log.d(TAG, "人脸检测请求被取消: ")
            }

            override fun onFinished() {
                // 请求完成，无论成功或失败都会调用
                if (!extractFeature) {
                    canSendData = true
                }
                Log.d(TAG, "人脸检测请求完成: ")
            }
        })
    }

    /**
     * 转base64
     * @param bitmap bitmap值
     */
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                baos.flush()
                baos.close()
                val bitmapBytes = baos.toByteArray()
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            bitmap?.recycle()
        }
        return result
    }

    /**
     * 人脸播报
     */
    private fun checkFace(
        owner: LifecycleOwner?,
        speak: String = Placeholder.replaceText(QuerySql.selectGreetConfig().strangerPrompt)
    ) {
        if (speakNum <= 0 && !isProcessing.get()) {
            speakNum = 1
            BaiduTTSHelper.getInstance().speak(speak)
            identifyFace!!.value = 0 //在百度TTS中设置为0不及时
        }
        identifyFace!!.observe(owner!!) { value ->
            if (value == 1 && isProcessing.compareAndSet(false, true)) {
                // 在协程内部调用挂起函数
                CoroutineScope(Dispatchers.Default).launch {
                    delay(5000)
                    speakNum = 0  // 将speakNum设置为0
                    isProcessing.set(false) // 处理完成，重置标志
                    this@launch.cancel()
                }
            }
        }
    }


    fun faceIdentify(
        faces: List<List<Double>?>,
        owner: LifecycleOwner,
        needEtiquette: Boolean = false
    ) {
        // 添加参数到JSON对象
        jsonParams["feat"] = faces
        jsonParams["feats"] = faceList(doubleString)
        // 将JSON对象转换为字符串
        val jsonString = jsonParams.toString()
        // 打印请求的JSON数据
        val params = RequestParams(Universal.POST_IDENTIFY) // 替换为你的API端点URL
        params.isAsJsonContent = true // 设置请求内容为JSON
        params.bodyContent = jsonString // 设置请求体为JSON字符串
        Log.d(TAG, "人脸识别请求发送数据: $jsonString")
        // 发送POST请求
        x.http().post(params, object : CommonCallback<String> {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "收到人脸识别数据：$result")
                if (result != null) {
                    val gson = Gson()
                    // 确保这里使用的是正确的数据类
                    val similarityResponse = gson.fromJson(result, Similarity::class.java)
                    main(similarityResponse, owner, needEtiquette)
                }
            }

            override fun onError(ex: Throwable, isOnCallback: Boolean) {
                // 请求出错，处理错误信息
                canSendData = true
                Log.i(TAG, "人脸识别请求出错: $ex")
            }

            override fun onCancelled(cex: Callback.CancelledException) {
                // 请求被取消，处理取消请求'
                Log.d(TAG, "人脸识别请求被取消: ")
            }

            override fun onFinished() {
                // 请求完成，无论成功或失败都会调用
                canSendData = true
                Log.d(TAG, "人脸识别请求完成: ")
            }
        })
    }


    fun main(
        similarityResponse: Similarity,
        owner: LifecycleOwner,
        needEtiquette: Boolean = false
    ) {
        if (similarityResponse.similarity.isNotEmpty()) {
            val chunkSize = doubleString.size // 指定子列表的大小

            val resultArrays = similarityResponse.similarity.chunked(chunkSize)

            // 调试输出
            println("Result Arrays: $resultArrays")

            val maxValuesWithIndex = resultArrays.flatMapIndexed { _, array ->
                val max = array.maxOrNull() ?: Double.MIN_VALUE
                val maxIndex = array.indexOf(max)
                if (max >= 0.7) listOf(Pair(maxIndex, max)) else emptyList()
            }
            // 调试输出
            println("Max Values With Index: $maxValuesWithIndex")
            val correspondingValues = maxValuesWithIndex.mapNotNull { (maxIndex, _) ->
                doubleString.getOrNull(maxIndex)?.name
            }.distinct().joinToString(", ")
            // 调试输出
            println("Concatenated Corresponding Values: $correspondingValues")

            if (correspondingValues.isNotEmpty()) {
                checkFace(
                    owner,
                    Placeholder.replaceText(QuerySql.selectGreetConfig().vipPrompt, name = correspondingValues)
                )
            } else {
                println("人脸库：没有查到此人")
                if (needEtiquette) {
                    checkFace(owner)
                }
            }
        } else {
            if (needEtiquette) {
                checkFace(owner)
            }
        }
    }


    /**
     * 将数据库中的人脸特征String转二位数组
     */
    private fun faceList(doubleString: List<Table_Face>): MutableList<List<Double>> {
        // 假设这是你的 JSON 字符串列表
        val jsonStringList = doubleString.map { it.sexual }
        // 创建 Gson 实例
        val gson = Gson()
        // 创建一个新的列表来存放二维数组
        val twoDimensionalArrayList = mutableListOf<List<Double>>()
        // 遍历 JSON 字符串列表
        jsonStringList.forEach { jsonString ->
            // 使用 Gson 将 JSON 字符串转换为一维数组
            val typeToken = object : TypeToken<List<Double>>() {}.type
            val oneDimensionalArray: List<Double> = gson.fromJson(jsonString, typeToken)
            // 将一维数组转换为二维数组（这里假设每个子数组有512个元素）
            val chunkedArray = oneDimensionalArray.chunked(512)
            // 将二维数组添加到列表中
            twoDimensionalArrayList.addAll(chunkedArray)
        }
        return twoDimensionalArrayList
    }

    //检测人脸随机播放
    private fun speakContent(): String {
        val list = listOf(
            "您好，我是这里的多功能党建机器人，${QuerySql.robotConfig().wakeUpWord}，有什么可以帮到您吗？",
            "喊我“${QuerySql.robotConfig().wakeUpWord}”，问我问题"
        )
        val randomIndex = Random().nextInt(list.size)
        return list[randomIndex]
    }

    fun onDestroy() {
        if (null != c) {
            // 取消所有协程任务
            faceScope.cancel()
            channel.close()
            // 停止预览
            c?.stopPreview()
            // 移除预览回调
            c?.setPreviewCallbackWithBuffer(null)
            // 释放相机资源
            c?.release()
            c = null
        }
    }
}