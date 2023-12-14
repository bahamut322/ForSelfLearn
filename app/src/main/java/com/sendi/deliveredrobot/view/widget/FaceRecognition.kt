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
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.LifecycleOwner
import com.alibaba.fastjson.JSONObject
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sendi.deliveredrobot.MyApplication.Companion.instance
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.interfaces.FaceDataListener
import com.sendi.deliveredrobot.model.FaceModel
import com.sendi.deliveredrobot.model.RectDeserializer
import com.sendi.deliveredrobot.navigationtask.RobotStatus.identifyFace
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.xutils.common.Callback
import org.xutils.common.Callback.CommonCallback
import org.xutils.http.RequestParams
import org.xutils.x
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type
import com.google.gson.Gson
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.FaceTips
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.model.Similarity
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
    private var manager = instance!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var speakNum = 0
    private var canSendData = true
    private lateinit var doubleString: List<FaceTips>
    private val isProcessing = AtomicBoolean(false)

    /**
     * @param extractFeature True代表获取人脸特征，默认为True
     * @param surfaceView 视频控件
     * @param width 图片宽
     * @param height 图片高
     * @param needSpeaking BaiduTTS播报
     * @param owner LifecycleOwner
     * @param needIdentify 开启人脸识别
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun suerFaceInit(
        extractFeature: Boolean = false,
        surfaceView: SurfaceView,
        width: Int = 800,
        height: Int = 600,
        needSpeaking: Boolean = false,
        owner: LifecycleOwner,
        needIdentify: Boolean = false
    ) {

        var cameraIds = arrayOfNulls<String>(0)
        doubleString = QuerySql.faceMessage()
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

        val surfaceHolder = surfaceView.holder
        //启动预览，到这里就能正常预览
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    c?.setPreviewDisplay(surfaceHolder)
                    val parameters = c?.parameters
                    try {
                        //图片分辨率
                        parameters?.setPictureSize(width, height)
                        //预览分辨率
                        parameters?.setPreviewSize(width, height)
                        c?.parameters = parameters
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                c?.startPreview() //开始预览
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.i(TAG, "surfaceChanged: $width $height")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
        //获取摄像实时数据
        c!!.setPreviewCallback { data: ByteArray?, _: Camera? ->
            if (data!!.isNotEmpty()) {
                val bm = decodeByteArrayToBitmap(data, width, height)
                if (bm != null) {
                    // 将耗时操作放在后台线程中
                    GlobalScope.launch(Dispatchers.IO) {
                        if (canSendData) {
                            canSendData = false
                            faceHttp(extractFeature, bm, needSpeaking, owner, needIdentify)
                            FaceDataListener.setFaceBit(bm)
                        }
                    }
                }
            }
        }
    }

    private fun decodeByteArrayToBitmap(data: ByteArray, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val image = YuvImage(data, ImageFormat.NV21, width, height, null)
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 75, stream)
            val jpegData = stream.toByteArray()
            bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * @param extractFeature True代表获取人脸特征，默认为True
     * @param bitmap 要识别的的bitmap
     * @param needSpeaking BaiduTTS播报
     * @param owner LifecycleOwner
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun faceHttp(
        extractFeature: Boolean = false,
        bitmap: Bitmap,
        needSpeaking: Boolean = false,
        owner: LifecycleOwner,
        needIdentify: Boolean = false
    ) {
        GlobalScope.launch(Dispatchers.IO) {

            val jsonParams = JSONObject()
            val base64 = bitmapToBase64(bitmap)
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
                        if (needSpeaking && !needIdentify) {
                            checkFace(owner)
                        }
                        if (needIdentify) {
                            val allFeatures = faceModelList.map { it.feat }
                            faceIdentify(allFeatures, owner)
                        }
                    }
                    //需要人脸识别，但是人脸数据返回为空的时候可以继续发送数据
                    if (needIdentify && faceModelList.isEmpty()) {
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
                    if (!needIdentify) {
                        canSendData = true
                    }
                    Log.d(TAG, "人脸检测请求完成: ")
                }
            })
        }
    }

    /**
     * 转base64
     * @param bitmap bitmap值
     */
    private fun bitmapToBase64(bitmap: Bitmap): String? {
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
        }
        return result
    }

    /**
     * 人脸播报
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun checkFace(
        owner: LifecycleOwner,
        speak: String = speakContent()
    ) {
        if (speakNum <= 0 && !isProcessing.get()) {
            speakNum = 1
            BaiduTTSHelper.getInstance().speak(speak)
            identifyFace!!.value = 0 //在百度TTS中设置为0不及时
        }
        identifyFace!!.observe(owner) { value ->
            if (value == 1 && isProcessing.compareAndSet(false, true)) {
                // 在协程内部调用挂起函数
                GlobalScope.launch(Dispatchers.Main) {
                    delay(5000)
                    speakNum = 0  // 将speakNum设置为0
                    isProcessing.set(false) // 处理完成，重置标志
                }
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun faceIdentify(faces: List<List<Double>?>, owner: LifecycleOwner) {
        GlobalScope.launch(Dispatchers.IO) {
            val jsonParams = JSONObject()
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
                        main(similarityResponse, owner)
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
    }


    fun main(similarityResponse: Similarity, owner: LifecycleOwner) {
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
                String.format(instance!!.getString(R.string.welcome_vip_understand), correspondingValues)
            )
        } else {
            println("人脸库：没有查到此人")
            if (QuerySql.QueryBasic().etiquette) {
                checkFace(owner)
            }
        }
    }


    /**
     * 将数据库中的人脸特征String转二位数组
     */
    private fun faceList(doubleString: List<FaceTips>): MutableList<List<Double>> {
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
            // 将一维数组转换为二维数组（这里假设每个子数组有10个元素）
            val chunkedArray = oneDimensionalArray.chunked(512)
            // 将二维数组添加到列表中
            twoDimensionalArrayList.addAll(chunkedArray)
        }
        return twoDimensionalArrayList
    }

    //检测人脸随机播放
    private fun speakContent(): String {
        val list = listOf(
            instance!!.getString(R.string.welcome_understand),
            instance!!.getString(R.string.can_i_help_you),
            instance!!.getString(R.string.i_hope_to_serve_you),
            instance!!.getString(R.string.welcome_i_am_xiao_di),

        )
        val randomIndex = Random().nextInt(list.size)
        return list[randomIndex]
    }

    fun onDestroy() {
        if (null != c) {
//            BaiduTTSHelper.getInstance().stop()
            c!!.setPreviewCallback(null)
            c!!.stopPreview()
            c!!.release()
            c = null
        }
    }
}