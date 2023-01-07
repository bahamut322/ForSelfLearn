package com.sendi.deliveredrobot.handler

import androidx.lifecycle.ViewModelLazy
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.ElevatorObject
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
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
    private val mainScope = MainScope()
    private val basicSettingViewModel = ViewModelLazy(
        BasicSettingViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory }
    )
    private val gson = Gson()
    private val floorNameSet = HashSet<String>()

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
////                  val robotId: String
//                    val temperatureThreshold: Float?
//                    val bigScreenType: Int?
//                    val pics: String?
//                    val picPlayType: Int?
//                    val picPlayTime: Int?
//                    val videos: String?
//                    val videoFrame: Int?
//                    val videoAudio: Int?
//                    val fontContent: String?
//                    val fontColor: String?
//                    val fontSize: Int?
//                    val fontLayout: Int?
//                    val fontBackGround: String?
//                    val tipsTemperatureInfo: String?
//                    val tipsTemperatureWarn: String?
//                    val tipsMaskWarn: String?
//                    val timeStamp: Long?
//                    val picType: Int?
//                    val textPosition: Int?
//                    try {
////                      robotId = jsonObject.get("robotId").asString
//                        temperatureThreshold = jsonObject.get("temperatureThreshold").asFloat
//                        bigScreenType = jsonObject.get("bigScreenType").asInt
//                        picPlayType = jsonObject.get("picPlayType").asInt
//                        picPlayTime = jsonObject.get("picPlayTime").asInt
//                        videoFrame = jsonObject.get("videoFrame").asInt
//                        videoAudio = jsonObject.get("videoAudio").asInt
//                        fontColor = jsonObject.get("fontColor").asString
//                        fontSize = jsonObject.get("fontSize").asInt
//                        fontLayout = jsonObject.get("fontLayout").asInt
//                        fontBackGround = jsonObject.get("fontBackGround").asString
//                        tipsTemperatureInfo = jsonObject.get("tipsTemperatureInfo").asString
//                        tipsTemperatureWarn = jsonObject.get("tipsTemperatureWarn").asString
//                        tipsMaskWarn = jsonObject.get("tipsMaskWarn").asString
//                        timeStamp = jsonObject.get("timeStamp").asLong
//                        picType = jsonObject.get("picType").asInt
//                        textPosition = jsonObject.get("textPosition").asInt
//                        fontContent = jsonObject.get("fontContent").asString
//                        videos= jsonObject.get("videos").asString
//                        pics = jsonObject.get("pics").asString
//                    } finally {
//                        mainScope.launch {
//                            withContext(Dispatchers.Main) {
//                                RobotStatus.gatekeeper.value = Gatekeeper(
////                                    robotId = robotId,
//                                    temperatureThreshold = temperatureThreshold,
//                                    bigScreenType = bigScreenType,
//                                    pics = pics,
//                                    picPlayType = picPlayType,
//                                    picPlayTime = picPlayTime,
//                                    videos = videos,
//                                    videoFrame = videoFrame,
//                                    videoAudio = videoAudio,
//                                    fontContent = fontContent,
//                                    fontColor = fontColor,
//                                    fontSize = fontSize,
//                                    fontLayout = fontLayout,
//                                    fontBackGround = fontBackGround,
//                                    tipsTemperatureInfo = tipsTemperatureInfo,
//                                    tipsTemperatureWarn = tipsTemperatureWarn,
//                                    tipsMaskWarn = tipsMaskWarn,
//                                    timeStamp = timeStamp,
//                                    picType = picType,
//                                    textPosition = textPosition
//                                )
//                            }
//                        }
//                    }
                    val gson = Gson()
                    val gatekeeper  = gson.fromJson(message, Gatekeeper::class.java)
                    RobotStatus.gatekeeper.value = gatekeeper
                }

                //机器人配置
                "replyRobotConfig" -> {
                    val gson = Gson()
                    val robotConfig  = gson.fromJson(message, RobotConfig::class.java)
                    RobotStatus.robotConfig.value = robotConfig

//                    val robotId: String
//                    val audioType: Int?
//                    val wakeUpWord: String?
//                    val sleep: Int?
//                    val sleepTime: Int?
//                    val wakeUpType: Int?
//                    val sleepType: Int?
//                    val sleepContentName: String ?
//                    val picType: Int ?
//                    val timeStamp: Long ?
//                    try {
////                        robotId = jsonObject.get("robotId").asString
//                        audioType = jsonObject.get("audioType").asInt
//                        wakeUpWord = jsonObject.get("wakeUpWord").asString
//                        sleep = jsonObject.get("sleep").asInt
//                        sleepTime = jsonObject.get("sleepTime").asInt
//                        wakeUpType = jsonObject.get("wakeUpType").asInt
//                        sleepType = jsonObject.get("sleepType").asInt
//                        sleepContentName = jsonObject.get("sleepContentName").asString
//                        picType = jsonObject.get("picType").asInt
//                        timeStamp = jsonObject.get("timeStamp").asLong
//                    } finally {
//                        mainScope.launch {
//                            withContext(Dispatchers.Main) {
//                                RobotStatus.robotConfig.value = RobotConfig(
////                                    robotId = robotId,
//                                    audioType = audioType,
//                                    wakeUpWord = wakeUpWord,
//                                    sleep = sleep,
//                                    sleepTime = sleepTime,
//                                    wakeUpType = wakeUpType,
//                                    sleepType = sleepType,
//                                    sleepContentName = sleepContentName,
//                                    picType = picType,
//                                    timeStamp = timeStamp
//                                )
//                            }
//                        }
//                    }
                }
//                "sendVersionInfo" -> {
//                    //版本更新信息
//                    val flag: Boolean
//                    val size: Int
//                    val path: String
//                    val version: String
//                    try {
//                        flag = jsonObject.get("flag").asBoolean
//                        size = jsonObject.get("size").asInt
//                        path = jsonObject.get("path").asString
//                        version = jsonObject.get("version").asString
//                    } finally {
//                        mainScope.launch {
//                            withContext(Dispatchers.Main) {
//                                RobotStatus.versionStatusModel.value = VersionStatusModel(
//                                    flag = flag,
//                                    size = size,
//                                    path = path,
//                                    version = version
//                                )
//                            }
//                        }
//                    }
//                }
//                "sendRobotTenancy" -> {
//                    val useType: Int
//                    val days: Int
//                    val deadline: String
//                    val robotName: String
//                    try {
//                        // 下发机器人租期信息
//                        useType = jsonObject.get("useType").asInt
//                        days = jsonObject.get("days").asInt
//                        deadline = when (useType) {
//                            0 -> MyApplication.instance!!.resources.getString(R.string.forever)
//                            else -> jsonObject.get("deadline").asString
//                        }
//                        robotName = jsonObject.get("robotName").asString
//                    } finally {
//                        mainScope.launch {
//                            withContext(Dispatchers.Main) {
//                                RobotStatus.tenancy.value =
//                                    ResponseTenancyModel(useType, days, deadline, robotName)
//                            }
//                        }
//                    }
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
}