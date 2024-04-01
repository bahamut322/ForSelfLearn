package com.sendi.deliveredrobot.handler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModelLazy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.ElevatorObject
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.entity.Table_Advertising
import com.sendi.deliveredrobot.entity.Table_Explain_Config
import com.sendi.deliveredrobot.entity.Table_Reply_Gate
import com.sendi.deliveredrobot.entity.Table_Robot_Config
import com.sendi.deliveredrobot.entity.Table_Shopping_Config
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.UpDataSQL
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.entity.entitySql.QuerySql.robotConfig
import com.sendi.deliveredrobot.entity.interaction.InteractionMqtt
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.helpers.ReplyAppletConfigHelper
import com.sendi.deliveredrobot.helpers.ReplyIntentHelper
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.RobotLogBagHelper
import com.sendi.deliveredrobot.model.AdvertisingConfig
import com.sendi.deliveredrobot.model.ExplainConfig
import com.sendi.deliveredrobot.model.Gatekeeper
import com.sendi.deliveredrobot.model.ReplyElevatorListModel
import com.sendi.deliveredrobot.model.ReplyFloorListModel
import com.sendi.deliveredrobot.model.ResetVerificationCodeAckModel
import com.sendi.deliveredrobot.model.RobotConfig
import com.sendi.deliveredrobot.model.SameName
import com.sendi.deliveredrobot.model.ShoppingGuideConfing
import com.sendi.deliveredrobot.model.log.RobotLog
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.litepal.LitePal.deleteAll
import java.io.File
import kotlin.concurrent.thread


/**
 *   @author: Swn
 *   @date: 2021/8/18 12:01
 *   @describe: MQTT消息处理(订阅X8)
 */
object MqttMessageHandler {
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
    private var mapNameChanged: Boolean = true
    private var waitingPointChanged: Boolean = true
    private var chargePointChanged: Boolean = true

    /**
     * @describe 接收消息
     */
    @SuppressLint("SdCardPath", "SuspiciousIndentation")
    fun receive(mqttMessage: MqttMessage) {
        synchronized(MqttMessageHandler::class.java) {
            val message = String(mqttMessage.payload)
            val jsonObject = JsonParser.parseString(message) as JsonObject

            if (!jsonObject.has("type") )return
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

                "queryLogBag" -> {
                    mainScope.launch {
                        withContext(Dispatchers.Default) {
                            val robotLogBagHelper = RobotLogBagHelper()
                            robotLogBagHelper.queryLogBag()
                        }
                    }
                }

                "uploadLogBag" -> {
                    mainScope.launch {
                        withContext(Dispatchers.Default) {
                            val robotLogBagHelper = RobotLogBagHelper()
                            robotLogBagHelper.uploadLog(
                                gson.fromJson(
                                    jsonObject.get("robotBagLog"),
                                    RobotLog::class.java
                                )
                            )
                        }
                    }
                }
                //讲解配置
                "replyExplanationConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    ToastUtil.show("收到讲解配置信息")
                    LogUtil.d("obtain: 收到讲解配置信息")
                    val gson = Gson()
                    val explainConfig = gson.fromJson(message, ExplainConfig::class.java)
                    deleteAll(Table_Explain_Config::class.java)
                    RobotStatus.explainConfig = explainConfig
                    val tableExplainConfig =
                        Table_Explain_Config()
                    tableExplainConfig.slogan = explainConfig.slogan
                    tableExplainConfig.stayTime = explainConfig.stayTime!!
                    tableExplainConfig.routeListText = explainConfig.routeListText
                    tableExplainConfig.routeListText = explainConfig.routeListText
                    tableExplainConfig.pointListText = explainConfig.pointListText
                    tableExplainConfig.startText = explainConfig.startText
                    tableExplainConfig.endText = explainConfig.endText
                    tableExplainConfig.interruptionText = explainConfig.interruptionText
                    tableExplainConfig.timeStamp = explainConfig.timeStamp!!
                    if (tableExplainConfig.save()) {
                        // 数据保存成功
                        RobotStatus.newUpdate.postValue(2)
                        Log.d("TAG", "receive: 讲解配置数据保存成功")
                        UpdateReturn.method()
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 讲解配置数据保存失败")
                    }
                }
                //广告配置
                "replyAdvertisementConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    val gson = Gson()
                    val advertisingConfig = gson.fromJson(message, AdvertisingConfig::class.java)
                    deleteAll(Table_Advertising::class.java)
//                    deleteFiles(File(Universal.advertisement))
//                    advFile = null
                    ToastUtil.show("收到广告配置")
                    LogUtil.d("收到广告配置")
                    val tableAdvertising =
                        Table_Advertising()
                    //创建文件的方法
                    createFolder()
                    tableAdvertising.timeStamp = advertisingConfig.timeStamp
                    tableAdvertising.type = advertisingConfig.argConfig!!.type!!
                    if (advertisingConfig.argConfig.argPic != null) {
                        val advPics =
                            compareArrays(
                                Universal.advertisement,
                                advertisingConfig.argConfig.argPic.pics
                            )
                        for (i in advPics!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + advPics[i],
                                Universal.advertisement,
                                FileName(advPics[i]!!),
                                MyApplication.listener
                            )
                        }
                        tableAdvertising.picType = advertisingConfig.argConfig.argPic.picType
                        tableAdvertising.picPlayTime =
                            advertisingConfig.argConfig.argPic.picPlayTime
                    }
                    if (advertisingConfig.argConfig.argFont != null) {
                        tableAdvertising.fontContent =
                            advertisingConfig.argConfig.argFont.fontContent
                        tableAdvertising.fontColor =
                            advertisingConfig.argConfig.argFont.fontColor
                        tableAdvertising.fontSize =
                            advertisingConfig.argConfig.argFont.fontSize
                        tableAdvertising.fontLayout =
                            advertisingConfig.argConfig.argFont.fontLayout
                        tableAdvertising.fontBackGround =
                            advertisingConfig.argConfig.argFont.fontBackGround
                        tableAdvertising.textPosition =
                            advertisingConfig.argConfig.argFont.textPosition
                    }
                    if (advertisingConfig.argConfig.argVideo != null) {
                        val advVideoFile =
                            compareArrays(
                                Universal.advertisement,
                                advertisingConfig.argConfig.argVideo.videos
                            )
                        for (i in advVideoFile!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + advVideoFile[i],
                                Universal.advertisement,
                                FileName(advVideoFile[i]!!),
                                MyApplication.listener
                            )
                        }
                        tableAdvertising.videoAudio =
                            advertisingConfig.argConfig.argVideo.videoAudio!!
                        tableAdvertising.videolayout =
                            advertisingConfig.argConfig.argVideo.videoLayOut!!
                    }
                    if (advertisingConfig.argConfig.argRadio != null) {
                        LogUtil.d("广告配置收到了argRadio,提示暂无此配置")
                    }
                    if (advertisingConfig.argConfig.argPicGroup != null) {
                        LogUtil.d("广告配置收到了argPicGroup,提示暂无此配置")
                    }
                    if (tableAdvertising.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 广告配置数据保存成功")
                        RobotStatus.newUpdate.postValue(1)
                        updateConfig()
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 广告配置数据保存失败")
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
                "replyShoppingGuideConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    val gson = Gson()
                    ToastUtil.show("收到导购配置")
                    val shoppingConfig = gson.fromJson(message, ShoppingGuideConfing::class.java)
                    RobotStatus.shoppingConfigList?.value = shoppingConfig
                    deleteAll(Table_Shopping_Config::class.java)
                    val shoppingConfigDB = Table_Shopping_Config()
                    shoppingConfigDB.name = shoppingConfig.name
                    shoppingConfigDB.firstPrompt = shoppingConfig.firstPrompt
                    shoppingConfigDB.completePrompt = shoppingConfig.completePrompt
                    shoppingConfigDB.interruptPrompt = shoppingConfig.interruptPrompt
                    shoppingConfigDB.baseTimeStamp = shoppingConfig.baseTimeStamp
                    if (shoppingConfigDB.save()) {
                        // 数据保存成功
                        RobotStatus.newUpdate.postValue(2)
                        Log.d("TAG", "云平台下发导购配置保存成功")
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "云平台下发导购配置数据保存失败")
                    }
                }
                //讲解路线配置
                "replyRouteList" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    ToastUtil.show("收到讲解路线配置")
                    InteractionMqtt().explainType(message)
                }

                //机器人门岗配置
                "replyGateConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    val gson = Gson()
                    val gatekeeper = gson.fromJson(message, Gatekeeper::class.java)
                    deleteAll(Table_Reply_Gate::class.java)
                    //提交到数据库
//                    deleteFiles(File(Universal.Secondary))
                    //创建文件的方法
                    createFolder()
                    ToastUtil.show("收到新的门岗配置信息")
                    Log.d(ContentValues.TAG, "obtain: 收到新的门岗配置信息")
                    val tableReplyGate =
                        Table_Reply_Gate()
                    tableReplyGate.temperatureThreshold = gatekeeper.temperatureThreshold!!
                    tableReplyGate.tipsTemperatureInfo = gatekeeper.tipsTemperatureInfo
                    tableReplyGate.tipsTemperatureWarn = gatekeeper.tipsTemperatureWarn
                    tableReplyGate.tipsMaskWarn = gatekeeper.tipsMaskWarn
                    tableReplyGate.timeStamp = gatekeeper.timeStamp!!
                    tableReplyGate.bigScreenType = gatekeeper.argConfig!!.type!!
                    if (gatekeeper.argConfig.screen == 1) {
                        if (gatekeeper.argConfig.argPic != null) {
                            println("收到：argPic")
                            val pics = compareArrays(
                                Universal.Secondary,
                                gatekeeper.argConfig.argPic.pics
                            )!!
                            for (i in pics.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + pics[i],
                                    Universal.Secondary,
                                    FileName(pics[i]!!),
                                    MyApplication.listener
                                )
                            }
                            tableReplyGate.picType = gatekeeper.argConfig.argPic.picType
                            tableReplyGate.picPlayType =
                                gatekeeper.argConfig.argPic.picPlayType
                            tableReplyGate.picPlayTime =
                                gatekeeper.argConfig.argPic.picPlayTime
                        }
                        if (gatekeeper.argConfig.argFont != null) {
                            println("收到：argFont")
                            tableReplyGate.fontContent =
                                gatekeeper.argConfig.argFont.fontContent
                            tableReplyGate.fontColor = gatekeeper.argConfig.argFont.fontColor
                            tableReplyGate.fontSize = gatekeeper.argConfig.argFont.fontSize
                            tableReplyGate.fontLayout =
                                gatekeeper.argConfig.argFont.fontLayout
                            tableReplyGate.fontBackGround =
                                gatekeeper.argConfig.argFont.fontBackGround
                            tableReplyGate.textPosition =
                                gatekeeper.argConfig.argFont.textPosition
                        }
                        if (gatekeeper.argConfig.argVideo != null) {
                            println("收到：argVideo")
                            val videoFile = compareArrays(
                                Universal.Secondary,
                                gatekeeper.argConfig.argVideo.videos
                            )!!
                            for (i in videoFile.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + videoFile[i],
                                    Universal.Secondary,
                                    FileName(videoFile[i]!!),
                                    MyApplication.listener
                                )
                            }
                            tableReplyGate.videoAudio =
                                gatekeeper.argConfig.argVideo.videoAudio!!
                            tableReplyGate.videolayout =
                                gatekeeper.argConfig.argVideo.videoLayOut!!
                        }
                        if (gatekeeper.argConfig.argRadio != null) {
                            println("收到：argRadio 暂无")
                        }
                        if (gatekeeper.argConfig.argPicGroup != null) {
                            println("收到 argPicGroup")
                        }
                    }
                    if (tableReplyGate.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 机器人门岗配置数据保存成功")
                        updateConfig()
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 机器人门岗配置数据保存失败")
                    }
                }

                //机器人配置
                "replyRobotConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    val gson = Gson()
                    ToastUtil.show("收到机器人配置")
                    Log.d(ContentValues.TAG, "obtain: 收到新的机器人配置信息")
                    val robotConfig = gson.fromJson(message, RobotConfig::class.java)
                    RobotStatus.robotConfig?.value = robotConfig
                    val currentConfig: Table_Robot_Config? = robotConfig()
                    if (currentConfig != null) {
                        mapNameChanged = currentConfig.mapName != robotConfig.mapName
                        waitingPointChanged =
                            currentConfig.waitingPointName != robotConfig.waitingPointName
                        chargePointChanged =
                            currentConfig.chargePointName != robotConfig.chargePointName
                    }
                    deleteAll(Table_Robot_Config::class.java)
                    //提交数据到数据库
//                    deleteFiles(File(Universal.Standby))
                    //创建文件的方法
                    createFolder()

                    val tableRobotConfig =
                        Table_Robot_Config()
                    //更新数据——基础设置
                    val values = ContentValues()
                    values.put("robotmode", UpdateReturn.audioName(robotConfig.audioType!!))
                    val whereArgs = arrayOf(QuerySql.QueryBasicId().toString() + "")
                    UpDataSQL.update("table_basic", values, "id = ?", whereArgs)
                    //单独处理女声
                    if (robotConfig.audioType == 0) {
                        UpdateReturn.randomVoice(
                            1,
                            QuerySql.QueryBasic().speechSpeed.toString()
                        )
                    } else {
                        UpdateReturn.randomVoice(
                            robotConfig.audioType,
                            QuerySql.QueryBasic().speechSpeed.toString()
                        )
                    }
                    //音色——放到基础设置统一管理
//                    robotConfigSql.audioType = robotConfig.audioType!!
                    tableRobotConfig.wakeUpWord = robotConfig.wakeUpWord
                    tableRobotConfig.sleep = robotConfig.sleep!!
                    tableRobotConfig.sleepTime = robotConfig.sleepTime!!
                    tableRobotConfig.wakeUpList = robotConfig.wakeUpList!!
                    tableRobotConfig.sleepType = robotConfig.argConfig!!.type!!
                    tableRobotConfig.mapName = robotConfig.mapName
                    tableRobotConfig.timeStamp = robotConfig.timeStamp!!
                    tableRobotConfig.password = robotConfig.password
                    tableRobotConfig.waitingPointName = robotConfig.waitingPointName
                    tableRobotConfig.chargePointName = robotConfig.chargePointName
                    tableRobotConfig.slogan = robotConfig.slogan
                    if (robotConfig.argConfig.screen == 0) {
                        if (robotConfig.argConfig.argPic != null) {
                            println("收到：argPic")
//                            val videos =
//                                UpdateReturn().splitStr(robotConfig.argConfig.argPic.pics)
                            val videos = compareArrays(
                                Universal.Standby,
                                robotConfig.argConfig.argPic.pics
                            )!!
                            for (i in videos.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + videos[i],
                                    Universal.Standby,
                                    FileName(videos[i]!!),
                                    MyApplication.listener
                                )
                            }

                            tableRobotConfig.picType = robotConfig.argConfig.argPic.picType
                        }
                        if (robotConfig.argConfig.argFont != null) {
                            println("收到：小屏幕无argFont")
                        }
                        if (robotConfig.argConfig.argVideo != null) {
//                            val videos =
//                                UpdateReturn().splitStr(robotConfig.argConfig.argVideo.videos)
                            val videos = compareArrays(
                                Universal.Standby,
                                robotConfig.argConfig.argVideo.videos
                            )!!
                            for (i in videos.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + videos[i],
                                    Universal.Standby,
                                    FileName(videos[i]!!),
                                    MyApplication.listener
                                )
                            }
                            println("收到：argVideo")
                        }
                        if (robotConfig.argConfig.argRadio != null) {
                            println("收到：argRadio 暂无")
                        }
                        if (robotConfig.argConfig.argPicGroup != null) {
//                            val videos =
//                                UpdateReturn().splitStr(robotConfig.argConfig.argPicGroup.sleepPic!!)
                            val videos = compareArrays(
                                Universal.Standby,
                                robotConfig.argConfig.argPicGroup.sleepPic!!
                            )!!
                            for (i in videos.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + videos[i],
                                    Universal.Standby,
                                    FileName(videos[i]!!),
                                    MyApplication.listener
                                )
                            }
                        }

                    }
                    if (tableRobotConfig.save()) {
                        Log.d("TAG", "receive: 配置数据保存成功")
                        if (mapNameChanged || waitingPointChanged || chargePointChanged) {
                            updateConfig(false)
                            DialogHelper.selfCheckDialog(
                                "检查到机器人地图改变",
                                "请把机器人推到充电桩：\n${robotConfig.chargePointName!!}，\n自动进行位置切换",
                                "地图改变",
                                true,
                                false,
                                null
                            ).show()
                        } else {
                            updateConfig(true)
                        }
                        Universal.mapType.postValue(false)
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 配置数据保存失败")
                    }
                }
                //云平台下发导购配置
                "replyShoppingGuideActionConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    ToastUtil.show("收到发导购配置")
                    InteractionMqtt().ActionShoppingType(message)
                }
                //引领子功能配置
                "replyGuidePointConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    ToastUtil.show("收到引领子功能配置")
                    InteractionMqtt().guidePointConfig(message)
                }

                "sendAppletTask" -> {
                    // 小程序下发任务
                    ToastUtil.show("收到远程任务..")
                    LogUtil.i("收到远程任务..")
                    RemoteOrderHelper.receiveRemoteOrder(jsonObject)
                }

                "replyGuideConfig" -> {
                    if (RobotStatus.batteryStateNumber.value == false) return
                    ToastUtil.show("收到引领配置")
                    InteractionMqtt().guideFoundation(message)

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

                "replyQaConfig" -> {
                    //云平台下发问答配置
                    ReplyQaConfigHelper.replyQaConfig(message)
                }

                "replyIntent" -> {
                    //平台回复机器人问题结果
                    mainScope.launch(Dispatchers.Main) {
                        ReplyIntentHelper.replyIntent(message)
                    }
                }
                "replyGreetConfig" ->{
                    if (RobotStatus.batteryStateNumber.value == false) return
                    LogUtil.d("收到迎宾配置")
                    InteractionMqtt().replyGreet(message)
                }
                "replyAppletConfig" -> {
                    LogUtil.i("收到小程序配置")
                    if (RobotStatus.batteryStateNumber.value == false) return
                    LogUtil.i("处理小程序配置")
                    ReplyAppletConfigHelper.replyAppletConfig(message)
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
        //文件夹目录(存放广告图片/视频)
        val fileADV = File(Universal.advertisement)
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!fileStandby.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            fileStandby.mkdirs()
        } else if (!fileSecondary.exists()) {
            fileSecondary.mkdirs()
        } else if (!fileADV.exists()) {
            fileADV.mkdirs()
        }
    }

    fun openFile(file: String) {
        //文件夹目录(存放待机图片/视频...)
        val fileStandby = File(file)
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!fileStandby.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            fileStandby.mkdirs()
        }
    }

    /**
     * 删除文件
     */
    fun deleteFiles(file: File): Boolean {
        return try {
            if (file.isDirectory) { //判断是否是文件夹
                val files = file.listFiles() //遍历文件夹里面的所有的
                for (i in files!!.indices) {
                    LogUtil.e("更新原有文件>>>>>> " + files[i].toString())
                    deleteFiles(files[i]) //删除
                }
            } else {
                file.delete()
                LogUtil.e("删除文件夹>>>>>> $file")
            }
            System.gc() //系统回收垃圾
            true
        } catch (e: Exception) {
            LogUtil.e("更新报错！！！: $e")
            false
        }
    }


    private fun updateConfig(boolean: Boolean = true) {
        if (!boolean) {
            UpdateReturn.settingMap()
        }
        UpdateReturn.method(boolean)
    }

    fun FileName(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }


    fun selectImagePath(path: String?): Array<String?> {
        //传入指定文件夹的路径　　　　
        val file = File(path)
        val files = file.listFiles()
        val imagePaths = ArrayList<String>()
        for (i in files.indices) {
            imagePaths.add(files[i].path)
        }
        return imagePaths.toTypedArray()
    }

    /**
     * @param sdcardFile sdcard的路径
     * @param needDownload Mqtt传入下载的路径
     */
    fun compareArrays(sdcardFile: String?, needDownload: String?): List<String?>? {
        val sameName = SameName()
        val sdcardPic = selectImagePath(sdcardFile)
        val download = UpdateReturn.splitStr(needDownload!!)
        // 比较两个数组
        val commonFiles = sdcardPic.mapNotNull { it?.substringAfterLast('/') }
            .intersect(download.mapNotNull { it?.substringAfterLast('/') })
        val uniqueFiles1 = sdcardPic.mapNotNull { it?.substringAfterLast('/') }
            .subtract(download.mapNotNull { it?.substringAfterLast('/') })
        val uniqueFiles2 = download.mapNotNull { it?.substringAfterLast('/') }
            .subtract(sdcardPic.mapNotNull { it?.substringAfterLast('/') })
        //共同包含的文件
        sameName.SameAll =
            commonFiles.mapNotNull { name -> sdcardPic.find { it?.endsWith("/$name") == true } }
        //第一个数组中独有的文件
        sameName.SameOne =
            uniqueFiles1.mapNotNull { name -> sdcardPic.find { it?.endsWith("/$name") == true } }
        //第二个数组中独有的文件
        sameName.SameTwo =
            uniqueFiles2.mapNotNull { name -> download.find { it?.endsWith("/$name") == true } }
        //删除Sdcard中多余的文件
        Log.d("compareArrays", "共同包含的文件: ${sameName.SameAll}")
        Log.d("compareArrays", "sdcard独有的文件: ${sameName.SameOne}")
        Log.d("compareArrays", "需要下载的的文件: ${sameName.SameTwo}")

        for (i in sameName.SameOne!!.indices) {
            UpdateReturn.deleteFolderFile(sameName.SameOne!![i], true)
        }
        return sameName.SameTwo
    }
}