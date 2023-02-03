package com.sendi.deliveredrobot.handler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelLazy
import com.alibaba.fastjson.JSONObject
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
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MapConfig
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.inputfilter.DownloadUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.litepal.LitePal
import java.io.File
import java.util.*
import kotlin.concurrent.thread

/**
 *   @author: heky
 *   @date: 2021/8/18 12:01
 *   @describe: MQTT消息处理
 */
object MqttMessageHandler {
    private val dao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val basicSettingViewModel = ViewModelLazy(
        BasicSettingViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory }
    )
    private val gson = Gson()
    private val floorNameSet = HashSet<String>()
    var fileName: Array<String>? = null//副屏内容
    var sleepName: Array<String>? = null//熄屏内容
    var fileNamepassc: Int = 0
    var sleepNamepassc: Int = 0
    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null

    /**
     * @describe 接收消息
     */
    fun receive(mqttMessage: MqttMessage) {
        synchronized(MqttMessageHandler::class.java) {
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
//                "callElevatorCurrentFloor" -> {
//                    try {
//                        val currentFloorIndex = jsonObject.get("currentFloorIndex").asInt
//                        LiftHelper.liftReach(currentFloorIndex)
//                    }catch (_: java.lang.Exception){
//
//                    }
//                }
                //机器人门岗配置
                "replyGateConfig" -> {
                    val gson = Gson()
                    val gatekeeper = gson.fromJson(message, Gatekeeper::class.java)
                    RobotStatus.gatekeeper?.value = gatekeeper
                    DialogHelper.loadingDialog.show();
                    LitePal.deleteAll(ReplyGateConfig::class.java)
                    //提交到数据库
                    deleteFiles(File(Universal.Secondary))
                    //创建文件的方法
                    createFolder()
                    Log.d(ContentValues.TAG, "obtain: 收到新的门岗配置信息")
                    val replyGateConfig = ReplyGateConfig()
                    replyGateConfig.temperatureThreshold = gatekeeper.temperatureThreshold!!
                    replyGateConfig.picPlayType = gatekeeper.picPlayType!!
                    replyGateConfig.picPlayTime = gatekeeper.picPlayTime!!
                    replyGateConfig.videoAudio = gatekeeper.videoAudio!!
                    replyGateConfig.fontContent = gatekeeper.fontContent
                    replyGateConfig.fontColor = gatekeeper.fontColor
                    replyGateConfig.fontSize = gatekeeper.fontSize!!
                    replyGateConfig.fontBackGround = gatekeeper.fontBackGround
                    replyGateConfig.tipsTemperatureInfo = gatekeeper.tipsTemperatureInfo
                    replyGateConfig.tipsTemperatureWarn = gatekeeper.tipsTemperatureWarn
                    replyGateConfig.tipsMaskWarn = gatekeeper.tipsMaskWarn
                    replyGateConfig.timeStamp = gatekeeper.timeStamp!!
                    replyGateConfig.picType = gatekeeper.picType!!
                    replyGateConfig.fontLayout = gatekeeper.fontLayout!!
                    replyGateConfig.bigScreenType = gatekeeper.bigScreenType!!
                    replyGateConfig.textPosition = gatekeeper.textPosition!!
                    Universal.pics = gatekeeper.pics
                    Universal.videoFile = gatekeeper.videos
                    replyGateConfig.save()
                    updateConfig()

                }

                //机器人配置
                "replyRobotConfig" -> {
                    val gson = Gson()
                    val robotConfig = gson.fromJson(message, RobotConfig::class.java)
                    RobotStatus.robotConfig?.value = robotConfig
                    DialogHelper.loadingDialog.show();
                    LitePal.deleteAll(RobotConfigSql::class.java)
                    //提交数据到数据库
                    deleteFiles(File(Universal.Standby))
                    //创建文件的方法
                    createFolder()
                    Log.d(ContentValues.TAG, "obtain: 收到新的机器人配置信息")
                    val robotConfigSql = RobotConfigSql()
                    robotConfigSql.audioType = robotConfig.audioType!!
                    robotConfigSql.wakeUpWord = robotConfig.wakeUpWord
                    robotConfigSql.sleep = robotConfig.sleep!!
                    robotConfigSql.sleepTime = robotConfig.sleepTime!!
                    robotConfigSql.wakeUpList = robotConfig.wakeUpList!!
                    robotConfigSql.sleepType = robotConfig.sleepType!!
                    robotConfigSql.picType = robotConfig.picType!!
                    robotConfigSql.mapName = robotConfig.mapName
                    robotConfigSql.timeStamp = robotConfig.timeStamp!!
                    Universal.sleepContentName = robotConfig.sleepContentName
                    robotConfigSql.password = robotConfig.password
                    robotConfigSql.save()
                    updateConfig()
                }
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
                    CloudMqttService.publish(ResetVerificationCodeAckModel().toString())
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
    private fun createFolder() {
        //文件夹目录(存放待机图片/视频...)
        val fileStandby = File(Universal.Standby)
        //文件目录(存放正常的副屏轮播)
        val fileSecondary = File(Universal.Secondary)
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!fileStandby.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            fileStandby.mkdirs()
        } else if (!fileSecondary.exists()) {
            fileSecondary.mkdirs()
        }
    }

    /**
     * 删除文件
     */
    private fun deleteFiles(file: File): Boolean {
        return try {
            if (file.isDirectory) { //判断是否是文件夹
                val files = file.listFiles() //遍历文件夹里面的所有的
                for (i in files!!.indices) {
                    Log.e("SelfCheckFragment", "更新原有文件>>>>>> " + files[i].toString())
                    deleteFiles(files[i]) //删除
                }
            } else {
                file.delete()
            }
            System.gc() //系统回收垃圾
            true
        } catch (e: Exception) {
            Log.e("SelfCheckFragment", "更新报错！！！: $e")
            false
        }
    }

    private fun updateConfig() {
        //这里必须过几秒钟后才能赋值，否者可能会将原来数据库中的值赋给变量
        val assignmentThread = Thread({
            fileNamepassc = 0
            sleepNamepassc = 0
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(5000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            UpdateReturn().assignment()
        }, "assignment")
        assignmentThread.start()

        val thread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "") {
                DialogHelper.loadingDialog.dismiss()
                Universal.pics = ""
                Universal.sleepContentName = ""
                Universal.videoFile = ""
                RobotStatus.newUpdata.postValue(1)
                UpdateReturn().method()
            }
            if (Universal.pics != "" || Universal.videoFile != "" || Universal.sleepContentName != "") {
                if (Universal.pics != "") {
                    fileName = Universal.pics?.split(",")?.toTypedArray()
                } else if (Universal.videoFile != "") {
                    fileName = Universal.videoFile?.split(",")?.toTypedArray()
                }
//                try {
                //副屏
                for (i in fileName!!) {
                    Looper.prepare()
                    DownloadUtil.getInstance().download(
                        "http://172.168.201.34:9055/management_res/$i",
                        Universal.Secondary,
                        object : DownloadUtil.OnDownloadListener {
                            override fun onDownloadSuccess(path: String) {
                                Log.e("TAG", "已保存：$path ")
                                if (sleepName == null) {
                                    if (fileNamepassc == UpdateReturn().fileSize(Universal.Secondary)) {
                                        DialogHelper.loadingDialog.dismiss()
                                        Universal.pics = ""
                                        Universal.sleepContentName = ""
                                        Universal.videoFile = ""
                                        RobotStatus.newUpdata.postValue(1)
                                        UpdateReturn().method()
                                    }
                                } else {
                                    if (fileNamepassc + sleepNamepassc == UpdateReturn().fileSize(Universal.Standby) + UpdateReturn().fileSize(
                                            Universal.Secondary
                                        )
                                    ) {
                                        DialogHelper.loadingDialog.dismiss()
                                        Universal.pics = ""
                                        Universal.sleepContentName = ""
                                        Universal.videoFile = ""
                                        RobotStatus.newUpdata.postValue(1)
                                        UpdateReturn().method()
                                    }
                                }
                            }

                            @SuppressLint("LongLogTag")
                            override fun onDownloading(progress: Int) {
                                if (progress == 100) {
                                    fileNamepassc++
                                }
                            }

                            override fun onDownloadFailed() {
                                Log.d(BaseFragment.TAG, "下载失败: ")
                            }
                        })
                    Looper.loop()
                }
            }
        }, "fileName")
        thread.start()
        val thread1 = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "") {
                Looper.prepare()
                DialogHelper.loadingDialog.dismiss()
                Universal.pics = ""
                Universal.sleepContentName = ""
                Universal.videoFile = ""
                RobotStatus.newUpdata.postValue(1)
                UpdateReturn().method()
                Looper.loop()
            }
            if (Universal.sleepContentName != "") {
                sleepName = Universal.sleepContentName?.split(",")?.toTypedArray()
            }
            if (sleepName != null) {
                for (i in sleepName!!) {
                    Looper.prepare()
                    DownloadUtil.getInstance().download(
                        "http://172.168.201.34:9055/management_res$i",
                        Universal.Standby,
                        object : DownloadUtil.OnDownloadListener {
                            override fun onDownloadSuccess(path: String) {
                                Log.e("TAG", "已保存：$path ")
                                if (fileName == null) {
                                    if (sleepNamepassc == UpdateReturn().fileSize(Universal.Standby)) {
                                        DialogHelper.loadingDialog.dismiss()
                                        Universal.pics = ""
                                        Universal.sleepContentName = ""
                                        Universal.videoFile = ""
                                        RobotStatus.newUpdata.postValue(1)
                                        UpdateReturn().method()
                                    }
                                } else {
                                    if (fileNamepassc + sleepNamepassc ==  UpdateReturn().fileSize(Universal.Standby) + UpdateReturn().fileSize(
                                            Universal.Secondary
                                        )
                                    ) {
                                        DialogHelper.loadingDialog.dismiss()
                                        Universal.pics = ""
                                        Universal.sleepContentName = ""
                                        Universal.videoFile = ""
                                        RobotStatus.newUpdata.postValue(1)
                                        UpdateReturn().method()
                                    }
                                }
                            }

                            @SuppressLint("LongLogTag")
                            override fun onDownloading(progress: Int) {
                                if (progress == 100) {
                                    sleepNamepassc++

                                }
                            }

                            override fun onDownloadFailed() {
                                Log.d(ContentValues.TAG, "下载失败: ")
                            }
                        })
                    Looper.loop()
                }
            }
        }, "sleepName")
        thread1.start()
    }
}