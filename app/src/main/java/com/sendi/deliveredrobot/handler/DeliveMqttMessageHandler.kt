package com.sendi.deliveredrobot.handler

import android.content.ContentValues
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelLazy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.helpers.RobotLogBagHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.model.log.RobotLog
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.DeliverMqttService
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.litepal.LitePal
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

/**
 *   @author: heky
 *   @date: 2021/8/18 12:01
 *   @describe: MQTT消息处理(订阅送物)
 */
object DeliveMqttMessageHandler {
    private val dao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val basicSettingViewModel = ViewModelLazy(
        BasicSettingViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory }
    )
    private val mainScope = MainScope()
    private val gson = Gson()
    private val floorNameSet = HashSet<String>()
    private var fileNames: Array<String?>? = null//副屏内容


    /**
     * @describe 接收消息
     */
    fun receive(mqttMessage: MqttMessage) {
        synchronized(DeliveMqttMessageHandler::class.java) {
            val message = String(mqttMessage.payload)
            val jsonObject = JsonParser.parseString(message) as JsonObject
            if (!jsonObject.has("type")) return
            when (jsonObject.get("type").asString) {
                "callElevatorState" -> {
                    try {
                        when (jsonObject.get("state").asInt) {
                            0 -> {
                                RobotStatus.liftState = false
                            }

                            1 -> {
                                RobotStatus.liftState = true
                            }

                            else -> {

                            }
                        }
                    } catch (_: Exception) {

                    }
                }

                "sendVersionInfo" -> {
                    //版本更新信息
                    var flag = false
                    var size = 0
                    var path = ""
                    var version = ""
                    try {
                        flag = jsonObject.get("flag").asBoolean
                        size = jsonObject.get("size").asInt
                        path = jsonObject.get("path").asString
                        version = jsonObject.get("version").asString
                    } finally {
                        mainScope.launch {
                            withContext(Dispatchers.Main) {
                                RobotStatus.versionStatusModel.value = VersionStatusModel(
                                    flag = flag,
                                    size = size,
                                    path = path,
                                    version = version
                                )
                            }
                        }
                    }
                }

                "replyTimeStamp" -> {
                    //获取时间戳
                    var time: Long = 0
                    try {
                        time = jsonObject.get("sysTimeStamp").asLong
                    } finally {
                        mainScope.launch {
                            withContext(Dispatchers.Main) {
                                RobotStatus.sysTimeStamp.value = time
                            }
                        }
                    }
                }
//                "callElevatorCurrentFloor" -> {
//                    try {
//                        val currentFloorIndex = jsonObject.get("currentFloorIndex").asInt
//                        LiftHelper.liftReach(currentFloorIndex)
//                    }catch (_: java.lang.Exception){
//
//                    }
//                }
                //机器人门岗配置
//                "replyGateConfig" -> {
//                    val gson = Gson()
//                    val gatekeeper = gson.fromJson(message, Gatekeeper::class.java)
//                    RobotStatus.gatekeeper?.value = gatekeeper
//                    DialogHelper.loadingDialog.show()
//                    LitePal.deleteAll(ReplyGateConfig::class.java)
//                    //提交到数据库
//                    deleteFiles(File(Universal.Secondary))
//                    //创建文件的方法
//                    createFolder()
//                    Log.d(ContentValues.TAG, "obtain: 收到新的门岗配置信息")
//                    val replyGateConfig = ReplyGateConfig()
//                    replyGateConfig.temperatureThreshold = gatekeeper.temperatureThreshold!!
//                    replyGateConfig.picPlayType = gatekeeper.picPlayType!!
//                    replyGateConfig.picPlayTime = gatekeeper.picPlayTime!!
//                    replyGateConfig.videoAudio = gatekeeper.videoAudio!!
//                    replyGateConfig.fontContent = gatekeeper.fontContent
//                    replyGateConfig.fontColor = gatekeeper.fontColor
//                    replyGateConfig.fontSize = gatekeeper.fontSize!!
//                    replyGateConfig.fontBackGround = gatekeeper.fontBackGround
//                    replyGateConfig.tipsTemperatureInfo = gatekeeper.tipsTemperatureInfo
//                    replyGateConfig.tipsTemperatureWarn = gatekeeper.tipsTemperatureWarn
//                    replyGateConfig.tipsMaskWarn = gatekeeper.tipsMaskWarn
//                    replyGateConfig.timeStamp = gatekeeper.timeStamp!!
//                    replyGateConfig.picType = gatekeeper.picType!!
//                    replyGateConfig.fontLayout = gatekeeper.fontLayout!!
//                    replyGateConfig.bigScreenType = gatekeeper.bigScreenType!!
//                    replyGateConfig.textPosition = gatekeeper.textPosition!!
//                    Universal.pics = gatekeeper.pics
//                    Universal.videoFile = gatekeeper.videos
//                    replyGateConfig.save()
//                    updateConfig()
//
//                }

                //机器人配置
//                "replyRobotConfig" -> {
//                    val gson = Gson()
//                    val robotConfig = gson.fromJson(message, RobotConfig::class.java)
//                    RobotStatus.robotConfig?.value = robotConfig
//                    DialogHelper.loadingDialog.show()
//                    LitePal.deleteAll(RobotConfigSql::class.java)
//                    //提交数据到数据库
//                    deleteFiles(File(Universal.Standby))
//                    //创建文件的方法
//                    createFolder()
//                    Log.d(ContentValues.TAG, "obtain: 收到新的机器人配置信息")
//                    val robotConfigSql = RobotConfigSql()
//                    robotConfigSql.audioType = robotConfig.audioType!!
//                    robotConfigSql.wakeUpWord = robotConfig.wakeUpWord
//                    robotConfigSql.sleep = robotConfig.sleep!!
//                    robotConfigSql.sleepTime = robotConfig.sleepTime!!
//                    robotConfigSql.wakeUpList = robotConfig.wakeUpList!!
//                    robotConfigSql.sleepType = robotConfig.sleepType!!
//                    robotConfigSql.picType = robotConfig.picType!!
//                    robotConfigSql.mapName = robotConfig.mapName
//                    robotConfigSql.timeStamp = robotConfig.timeStamp!!
//                    Universal.sleepContentName = robotConfig.sleepContentName
//                    robotConfigSql.password = robotConfig.password
//                    robotConfigSql.save()
//                    updateConfig()
//                }
                "sendAppletTask" -> {
                    // 小程序下发任务
                    ToastUtil.show("收到远程任务..")
                    LogUtil.i("收到远程任务..")
                    RemoteOrderHelper.receiveRemoteOrder(jsonObject)
                }

                "resetVerificationCode" -> {
                    // 重置密码
                    ToastUtil.show("远程重置密码")
                    LogUtil.i("远程重置密码")
                    basicSettingViewModel.value.basicConfig.verifyPassword = "00000"
                    thread {
                        dao.updateBasicConfig(basicSettingViewModel.value.basicConfig)
                    }
                    DeliverMqttService.publish(ResetVerificationCodeAckModel().toString())
                }

                "replyFloorList" -> {
                    val replyFloorListModel =
                        gson.fromJson(message, ReplyFloorListModel::class.java)
                    // 楼层名字set，用于展示
                    floorNameSet.clear()
                    replyFloorListModel.floorList?.map { floorModel -> floorNameSet.add(floorModel.floor) }
                    ElevatorObject.originFloorModel = replyFloorListModel
                    ElevatorObject.floorNameArray = floorNameSet.toTypedArray()
                    ElevatorObject.elevatorNameArray = replyFloorListModel.elevatorList
                }

                "replyElevatorList" -> {
                    val replyElevatorListModel =
                        gson.fromJson(message, ReplyElevatorListModel::class.java)
                    handleReplyElevatorListModel(replyElevatorListModel)
                    ElevatorObject.originElevatorModel = replyElevatorListModel
                }

                "replyElevatorArrive" -> {
                    try {
                        val currentFloorName = jsonObject.get("currentFloorName").asString
                        val elevator = jsonObject.get("elevator").asString
                        LiftHelper.liftReach(currentFloorName, elevator)
                    } catch (_: java.lang.Exception) {

                    }
                }

                else -> {}
            }
        }
    }

    private fun handleReplyElevatorListModel(replyElevatorListModel: ReplyElevatorListModel) {
        for (elevatorModel in replyElevatorListModel.elevatorList) {
            val floorList =
                elevatorModel.floorList.filter { floorModel -> "开门" != floorModel.name && "关门" != floorModel.name }
            val floorNameSet = HashSet<String>()
            val transferSet = HashSet<String>()
            for (floorModel in floorList) {
                floorNameSet.add(floorModel.name)
                if (floorModel.transfer == 1) {
                    transferSet.add(floorModel.name)
                }
            }
            elevatorModel.apply {
                floorNameList = floorNameSet.toTypedArray()
                transferList = transferSet.toTypedArray()
            }
        }
    }

    /**
     *创建文件
     */
//    private fun createFolder() {
//        //文件夹目录(存放待机图片/视频...)
//        val fileStandby = File(Universal.Standby)
//        //文件目录(存放正常的副屏轮播)
//        val fileSecondary = File(Universal.Secondary)
//        //判断文件夹是否存在，如果不存在就创建，否则不创建
//        if (!fileStandby.exists()) {
//            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
//            fileStandby.mkdirs()
//        } else if (!fileSecondary.exists()) {
//            fileSecondary.mkdirs()
//        }
//    }

    /**
     * 删除文件
     */
//    private fun deleteFiles(file: File): Boolean {
//        return try {
//            if (file.isDirectory) { //判断是否是文件夹
//                val files = file.listFiles() //遍历文件夹里面的所有的
//                for (i in files!!.indices) {
//                    Log.e("SelfCheckFragment", "更新原有文件>>>>>> " + files[i].toString())
//                    deleteFiles(files[i]) //删除
//                }
//            } else {
//                file.delete()
//            }
//            System.gc() //系统回收垃圾
//            true
//        } catch (e: Exception) {
//            Log.e("SelfCheckFragment", "更新报错！！！: $e")
//            false
//        }
//    }


//    private fun updateConfig() {
//        //这里必须过几秒钟后才能赋值，否者可能会将原来数据库中的值赋给变量
//        val assignmentThread = Thread({
//            val threadName = Thread.currentThread().name
//            println(threadName + "线程开始执行")
//            try {
//                Thread.sleep(5000)
//                //15000
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//            println(threadName + "任务执行完毕")
//            UpdateReturn().assignment()
//        }, "assignment")
//        assignmentThread.start()
//
//        val thread = Thread({
//            val threadName = Thread.currentThread().name
//            println(threadName + "线程开始执行")
//            try {
//                Thread.sleep(10000)
//                //15000
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "") {
//                DialogHelper.loadingDialog.dismiss()
//                Universal.pics = ""
//                Universal.sleepContentName = ""
//                Universal.videoFile = ""
//                RobotStatus.newUpdata.postValue(1)
//                UpdateReturn().method()
//            }
//            if (Universal.pics != "" || Universal.videoFile != "" || Universal.sleepContentName != "") {
//                if (Universal.pics != "") {
//                    fileNames = null
//                    fileNames = UpdateReturn().splitStr(Universal.pics)
//                } else if (Universal.videoFile != "") {
//                    fileNames = null
//                    fileNames = UpdateReturn().splitStr(Universal.videoFile)
//                }
////                try {
//                //副屏
//                Log.d("TAG", "updateConfig: " + fileNames!!.size)
//                if (fileNames!!.isNotEmpty()) {
//                    for (i in 0 until fileNames!!.size) {
//                        downloadFile(
//                            "http://172.168.201.34:9055/management_res/" + fileNames!![i],
//                            Universal.Secondary
//                        )
//                    }
//                }
//            }
//        }, "fileName")
//        thread.start()
//        val thread1 = Thread({
//            val threadName = Thread.currentThread().name
//            println(threadName + "线程开始执行")
//            try {
//                Thread.sleep(10000)
//                //15000
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "") {
//                Looper.prepare()
//                DialogHelper.loadingDialog.dismiss()
//                Universal.pics = ""
//                Universal.sleepContentName = ""
//                Universal.videoFile = ""
//                RobotStatus.newUpdata.postValue(1)
//                UpdateReturn().method()
//                Looper.loop()
//            }
//            if (Universal.sleepContentName != "") {
//                UpdateReturn().splitStr(Universal.sleepContentName)
//            }
//            if (UpdateReturn().splitStr(Universal.sleepContentName) != null) {
//                for (i in 0 until UpdateReturn().splitStr(Universal.sleepContentName)!!.size) {
//                    downloadFile(
//                        "http://172.168.201.34:9055/management_res/" + fileNames!![i],
//                        Universal.Standby
//                    )
//                }
//            }
//        }, "sleepName")
//        thread1.start()
//    }

    //下载
//    private fun downloadFile(url: String, savePath: String) {
////        final String url = "http://172.168.201.34:9055/management_res/";
//        val startTime = System.currentTimeMillis()
//        Log.i("DOWNLOAD", "开始下载：$url")
//        val okHttpClient = OkHttpClient()
//        val request: Request = Request.Builder().url(url).build()
//        okHttpClient.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // 下载失败
//                e.printStackTrace()
//                Log.e("DOWNLOAD", "下载失败：$url ")
//            }
//
//            @Throws(IOException::class)
//            override fun onResponse(call: Call, response: Response) {
//                var `is`: InputStream? = null
//                val buf = ByteArray(2048)
//                var len = 0
//                var fos: FileOutputStream? = null
//                // 储存下载文件的目录
////                val savePath = Universal.Secondary
//                try {
//                    `is` = response.body!!.byteStream()
//                    val total = response.body!!.contentLength()
//                    val file = File(savePath, url.substring(url.lastIndexOf("/") + 1))
//                    fos = FileOutputStream(file)
//                    var sum: Long = 0
//                    while (`is`.read(buf).also { len = it } != -1) {
//                        fos.write(buf, 0, len)
//                        sum += len.toLong()
//                        val progress = (sum * 1.0f / total * 100).toInt()
//                        // 下载中
//                        Log.i("DOWNLOAD", "下载中：$file: $progress %")
//                    }
//                    fos.flush()
//                    // 下载完成
////                    listener.onDownloadSuccess();
//                    if (UpdateReturn().splitStr(Universal.sleepContentName)!!.isEmpty()) {
//                        if (fileNames!!.size  == UpdateReturn().fileSize(Universal.Secondary)) {
//                            DialogHelper.loadingDialog.dismiss()
//                            Universal.pics = ""
//                            Universal.sleepContentName = ""
//                            Universal.videoFile = ""
//                            RobotStatus.newUpdata.postValue(1)
//                            UpdateReturn().method()
//                        }
//                    }else{
//                        if (fileNames!!.size + UpdateReturn().splitStr(Universal.sleepContentName)!!.size == UpdateReturn().fileSize(Universal.Secondary)+UpdateReturn().fileSize(Universal.Standby)) {
//                            DialogHelper.loadingDialog.dismiss()
//                            Universal.pics = ""
//                            Universal.sleepContentName = ""
//                            Universal.videoFile = ""
//                            RobotStatus.newUpdata.postValue(1)
//                            UpdateReturn().method()
//                        }
//                    }
//                    Log.e("DOWNLOAD", "下载成功：$file,下载耗时= ${(System.currentTimeMillis() - startTime)} ms")
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                    Log.e("DOWNLOAD", "下载失败!!")
//                } finally {
//                    try {
//                        `is`?.close()
//                    } catch (_: IOException) {
//                    }
//                    try {
//                        fos?.close()
//                    } catch (_: IOException) {
//                    }
//                }
//            }
//        })
//    }

}