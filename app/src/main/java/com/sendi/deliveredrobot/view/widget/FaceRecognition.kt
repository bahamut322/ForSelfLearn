package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
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
import androidx.lifecycle.MediatorLiveData
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Table_Face
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.interfaces.FaceDataListener
import com.sendi.deliveredrobot.model.FaceModel
import com.sendi.deliveredrobot.model.RectDeserializer
import com.sendi.deliveredrobot.model.Similarity
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
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
import kotlin.concurrent.thread


/**
 * @Author Swn
 * @Data 2023/12/7
 * @describe 人脸识别
 */
object FaceRecognition {
    private const val TAG = "人脸TAG"
    var c: Camera? = null
//    private var speakNum = 0
    private var canSendData = true
    private var doubleString: ArrayList<Table_Face?> = ArrayList()
    private val faceHttpJsonParams = JSONObject()
    private val faceIdentifyJsonParams = JSONObject()
    private var faceScope: CoroutineScope? = null
    private var manager = MyApplication.instance!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var channel: Channel<ByteArray>? = null
    private var shouldExecute:Boolean = true // 控制方法是否执行的标志
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Rect::class.java, RectDeserializer())
        .create()
    val listType: Type = object : TypeToken<List<FaceModel>>() {}.type
    @SuppressLint("StaticFieldLeak")
    private val faceIdentifyParams = RequestParams(Universal.POST_IDENTIFY) // 替换为你的API端点URL
    private val typeToken: Type = object : TypeToken<List<Double>>() {}.type
    @SuppressLint("StaticFieldLeak")
    private val faceHttpParams = RequestParams(Universal.POST_FAST) // 替换为你的API端点URL
    private val identifyMediatorLiveData: MediatorLiveData<Int> = MediatorLiveData()
    private val newUpdateMediatorLiveData: MediatorLiveData<Int> = MediatorLiveData()
    private const val DELAY_TIME = 1000 * 10L * 1000
    private var lastSpeakTime: Long? = null

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
        needEtiquette: Boolean = false
    ) {
        lastSpeakTime = 0L
        channel = Channel(capacity = Channel.CONFLATED) // 限制 Channel 大小
        shouldExecute = true
        faceScope = CoroutineScope(Dispatchers.Default + Job())
        RobotStatus.identifyFaceSpeak.postValue(1)
        thread {
            LogUtil.i("人脸识别初始化")
            canSendData = true
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
                    channel?.trySend(data) // 将数据发送到Channel
                }
                c?.addCallbackBuffer(buffer)
            }
            newUpdateMediatorLiveData.addSource(RobotStatus.newUpdate){
                Log.d(TAG, "suerFaceInit: 获取数据")
                doubleString = QuerySql.faceMessage()
            }
            identifyMediatorLiveData.addSource(RobotStatus.identifyFaceSpeak) { value: Int ->
                if (value == 1) {
                    lastSpeakTime = System.currentTimeMillis()
                }
            }
            // 启动一个单独的协程来处理数据
            faceScope?.launch {
                if (channel != null) {
                    for (data in channel!!) { // 从Channel中接收数据
                        try {
//                    Log.d(TAG, "suerFaceInit人脸识别协程名: ${Thread.currentThread().name}")
                            val bm = decodeByteArrayToBitmap(data, width, height)
                            if (bm != null) {
                                faceHttp(extractFeature, bm, needEtiquette)
                                FaceDataListener.setFaceBit(bm)
                                bm.recycle()
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    /**
     * 将NV21格式的字节数组转换为Bitmap
     * @param data NV21格式的字节数组
     * @param width 图片宽
     * @param height 图片高
     */
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
        needEtiquette: Boolean = false
    ) {
        val base64 = bitmapToBase64(bitmap)
        System.gc()
        // 添加参数到JSON对象
        faceHttpJsonParams["img"] = base64 // 后续需要修改base64
        faceHttpJsonParams["extract"] = extractFeature
        // 将JSON对象转换为字符串
        val jsonString = faceHttpJsonParams.toString()
        // 打印请求的JSON数据
//        Log.d(TAG, "发送人脸检测请求数据: $jsonString")
        // 创建RequestParams对象
        faceHttpParams.isAsJsonContent = true // 设置请求内容为JSON
        faceHttpParams.bodyContent = jsonString // 设置请求体为JSON字符串
        // 发送POST请求
        x.http().post(faceHttpParams, object : CommonCallback<String> {
            override fun onSuccess(result: String?) {
//                Log.d(TAG, "收到人脸检测数据：$result")
                val faceModelList: List<FaceModel> = gson.fromJson(result, listType)
                FaceDataListener.setFaceModels(faceModelList)
                // Update data
                if (faceModelList.isNotEmpty()) {
//                    Log.d(TAG, "人脸检测解析数据：${faceModelList}")
                    if (needEtiquette && !extractFeature) {
                        checkFaceSpeak()
                    }
                    if (extractFeature) {
                        val allFeatures = faceModelList.map { it.feat }
                        faceIdentify(allFeatures, needEtiquette)
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
    private fun checkFaceSpeak(
        speak: String = Placeholder.replaceText(QuerySql.selectGreetConfig().strangerPrompt)
    ) {
        synchronized(this){
            if (shouldExecute && RobotStatus.identifyFaceSpeak.value == 1) {
//            speakNum = 1
                RobotStatus.identifyFaceSpeak.postValue(0) //在百度TTS中设置为0不及时
                val currentTime = System.currentTimeMillis()
                if (lastSpeakTime != null && currentTime - lastSpeakTime!! > DELAY_TIME) {
                    BaiduTTSHelper.getInstance().speak(speak, "")
                }
//            BaiduTTSHelper.getInstance().speak(speak, "")
            }
        }
    }


    /**
     * 人脸识别
     * @param faces 人脸特征
     * @param needEtiquette 是否需要播报
     */
    fun faceIdentify(
        faces: List<List<Double>?>,
        needEtiquette: Boolean = false
    ) {
        // 添加参数到JSON对象
        faceIdentifyJsonParams["feat"] = faces
        faceIdentifyJsonParams["feats"] = faceList(doubleString)
        // 将JSON对象转换为字符串
        val jsonString = faceIdentifyJsonParams.toString()
        // 打印请求的JSON数据
        faceIdentifyParams.isAsJsonContent = true // 设置请求内容为JSON
        faceIdentifyParams.bodyContent = jsonString // 设置请求体为JSON字符串
//        Log.d(TAG, "人脸识别请求发送数据")
        // 发送POST请求
        x.http().post(faceIdentifyParams, object : CommonCallback<String> {
            override fun onSuccess(result: String?) {
//                Log.d(TAG, "收到人脸识别数据：$result")
                if (result != null) {
                    // 确保这里使用的是正确的数据类
                    main(gson.fromJson(result, Similarity::class.java), needEtiquette)
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


    /**
     * 人脸识别主要逻辑
     * @param similarityResponse 人脸识别数据
     * @param needEtiquette 是否需要播报
     */
    fun main(
        similarityResponse: Similarity,
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
                checkFaceSpeak(
                    Placeholder.replaceText(
                        QuerySql.selectGreetConfig().vipPrompt,
                        name = correspondingValues
                    )
                )
            } else {
                println("人脸库：没有查到此人")
                if (needEtiquette) {
                    checkFaceSpeak()
                }
            }
        } else {
            if (needEtiquette) {
                checkFaceSpeak()
            }
        }
    }


    /**
     * 将人脸特征字符串列表转换为二维数组
     * @param doubleString 人脸特征字符串列表
     */
    private fun faceList(doubleString: List<Table_Face?>): MutableList<List<Double>> {
        // 创建 Gson 实例
        // 创建一个新的列表来存放二维数组
        val twoDimensionalArrayList = mutableListOf<List<Double>>()
        // 遍历 JSON 字符串列表
        doubleString.forEach { tableFace ->
            try {
                // 假设这是你的 JSON 字符串
                val jsonString = tableFace?.sexual
                // 使用 Gson 将 JSON 字符串转换为一维数组
                val oneDimensionalArray: List<Double> = gson.fromJson(jsonString, typeToken)
                // 将一维数组转换为二维数组（这里假设每个子数组有512个元素）
                val chunkedArray = oneDimensionalArray.chunked(512)
                // 将二维数组添加到列表中
                twoDimensionalArrayList.addAll(chunkedArray)
            } catch (e: JsonSyntaxException) {
                // 解析失败时，捕获异常并添加一个空的字符串列表
                twoDimensionalArrayList.add(emptyList())
            }
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

    /**
     * 销毁
     */
    fun onDestroy() {
        SpeakHelper.stop()
        MainScope().launch(Dispatchers.Main) {
            newUpdateMediatorLiveData.removeSource(RobotStatus.newUpdate)
            identifyMediatorLiveData.removeSource(RobotStatus.identifyFaceSpeak)
        }
        if (null != c) {
            thread {
                LogUtil.i("人脸识别销毁")
                // 取消所有协程任务
                shouldExecute = false
                faceScope?.cancel()
                channel?.close()
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
}