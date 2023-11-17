package com.sendi.deliveredrobot.handler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelLazy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.entity.*
import com.sendi.deliveredrobot.entity.interaction.InteractionMqtt
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.helpers.ReplyIntentHelper
import com.sendi.deliveredrobot.helpers.ReplyQaConfigHelper
import com.sendi.deliveredrobot.helpers.RobotLogBagHelper
import com.sendi.deliveredrobot.model.*
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
import java.util.*
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
    private var fileNames: Array<String?>? = null//副屏内容
    private var houseFile: Array<String?>? = null//主屏内容
    private var advFile: Array<String?>? = null //广告内容

    /**
     * @describe 接收消息
     */
    @SuppressLint("SdCardPath", "SuspiciousIndentation")
    fun receive(mqttMessage: MqttMessage) {
        downLoadFinish()
        synchronized(MqttMessageHandler::class.java) {
            val message = String(mqttMessage.payload)
            val jsonObject = JsonParser.parseString(message) as JsonObject
//            if (!jsonObject.has("type") || RobotStatus.batteryStateNumber.value == false) return
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
                    LogUtil.d("obtain: 收到讲解配置信息")
                    val gson = Gson()
                    val explainConfig = gson.fromJson(message, ExplainConfig::class.java)
                    deleteAll(ExplainConfigDB::class.java)
                    RobotStatus.explainConfig?.value = explainConfig
                    val explainConfigDB = ExplainConfigDB()
                    explainConfigDB.slogan = explainConfig.slogan
                    explainConfigDB.stayTime = explainConfig.stayTime!!
                    explainConfigDB.routeListText = explainConfig.routeListText
                    explainConfigDB.routeListText = explainConfig.routeListText
                    explainConfigDB.pointListText = explainConfig.pointListText
                    explainConfigDB.startText = explainConfig.startText
                    explainConfigDB.endText = explainConfig.endText
                    explainConfigDB.interruptionText = explainConfig.interruptionText
                    explainConfigDB.timeStamp = explainConfig.timeStamp!!
                    if (explainConfigDB.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 讲解配置数据保存成功")
                        UpdateReturn().method()
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 讲解配置数据保存失败")
                    }
                }
                //广告配置
                "replyAdvertisementConfig" -> {
                    val gson = Gson()
                    val advertisingConfig = gson.fromJson(message, AdvertisingConfig::class.java)
                    deleteAll(AdvertisingConfigDB::class.java)
                    deleteFiles(File(Universal.advertisement))
                    advFile = null
                    Universal.advVideoFile = ""
                    Universal.advPics = ""
                    RobotStatus.advertisingConfig?.value = advertisingConfig
                    LogUtil.d("收到广告配置")
                    val advertisingConfigDB = AdvertisingConfigDB()
                    //创建文件的方法
                    createFolder()
                    advertisingConfigDB.timeStamp = advertisingConfig.timeStamp
                    advertisingConfigDB.type = advertisingConfig.argConfig!!.type!!
                    if (advertisingConfig.argConfig.argPic != null) {
                        Universal.advPics = advertisingConfig.argConfig.argPic.pics
                        advertisingConfigDB.picType = advertisingConfig.argConfig.argPic.picType
                        advertisingConfigDB.picPlayTime =
                            advertisingConfig.argConfig.argPic.picPlayTime
                    }
                    if (advertisingConfig.argConfig.argFont != null) {
                        advertisingConfigDB.fontContent =
                            advertisingConfig.argConfig.argFont.fontContent
                        advertisingConfigDB.fontColor =
                            advertisingConfig.argConfig.argFont.fontColor
                        advertisingConfigDB.fontSize =
                            advertisingConfig.argConfig.argFont.fontSize
                        advertisingConfigDB.fontLayout =
                            advertisingConfig.argConfig.argFont.fontLayout
                        advertisingConfigDB.fontBackGround =
                            advertisingConfig.argConfig.argFont.fontBackGround
                        advertisingConfigDB.textPosition =
                            advertisingConfig.argConfig.argFont.textPosition
                    }
                    if (advertisingConfig.argConfig.argVideo != null) {
                        Universal.advVideoFile = advertisingConfig.argConfig.argVideo.videos
                        advertisingConfigDB.videoAudio =
                            advertisingConfig.argConfig.argVideo.videoAudio!!
                        advertisingConfigDB.videolayout =
                            advertisingConfig.argConfig.argVideo.videoLayOut!!
                    }
                    if (advertisingConfig.argConfig.argRadio != null) {
                        LogUtil.d("广告配置收到了argRadio,提示暂无此配置")
                    }
                    if (advertisingConfig.argConfig.argPicGroup != null) {
                        LogUtil.d("广告配置收到了argPicGroup,提示暂无此配置")
                    }
                    if (advertisingConfigDB.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 广告配置数据保存成功")
                        updateConfig()
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 广告配置数据保存失败")
                    }
                }

                "replyShoppingGuideConfig" -> {
                    val gson = Gson()
                    val shoppingConfig = gson.fromJson(message, ShoppingGuideConfing::class.java)
                    RobotStatus.shoppingConfigList?.value = shoppingConfig
                    deleteAll(ShoppingConfigDB::class.java)
                    val shoppingConfigDB = ShoppingConfigDB()
                    shoppingConfigDB.name = shoppingConfig.name
                    shoppingConfigDB.firstPrompt = shoppingConfig.firstPrompt
                    shoppingConfigDB.completePrompt = shoppingConfig.completePrompt
                    shoppingConfigDB.interruptPrompt = shoppingConfig.interruptPrompt
                    shoppingConfigDB.baseTimeStamp = shoppingConfig.baseTimeStamp
                    if (shoppingConfigDB.save()) {
                        // 数据保存成功
                        Log.d("TAG", "云平台下发导购配置保存成功")
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "云平台下发导购配置数据保存失败")
                    }
                }
                //讲解路线配置
                "replyRouteList" -> {
                    InteractionMqtt().ExplainType(message)
                }

                //机器人门岗配置
                "replyGateConfig" -> {
                    val gson = Gson()
                    val gatekeeper = gson.fromJson(message, Gatekeeper::class.java)
                    RobotStatus.gatekeeper?.value = gatekeeper
                    deleteAll(ReplyGateConfig::class.java)
                    //提交到数据库
                    deleteFiles(File(Universal.Secondary))
                    //创建文件的方法
                    createFolder()
                    Log.d(ContentValues.TAG, "obtain: 收到新的门岗配置信息")
                    val replyGateConfig = ReplyGateConfig()
                    replyGateConfig.temperatureThreshold = gatekeeper.temperatureThreshold!!
                    replyGateConfig.tipsTemperatureInfo = gatekeeper.tipsTemperatureInfo
                    replyGateConfig.tipsTemperatureWarn = gatekeeper.tipsTemperatureWarn
                    replyGateConfig.tipsMaskWarn = gatekeeper.tipsMaskWarn
                    replyGateConfig.timeStamp = gatekeeper.timeStamp!!
                    replyGateConfig.bigScreenType = gatekeeper.argConfig!!.type!!
                    if (gatekeeper.argConfig.screen == 1) {
                        if (gatekeeper.argConfig.argPic != null) {
                            println("收到：argPic")
                            Universal.pics = gatekeeper.argConfig.argPic.pics
                            replyGateConfig.picType = gatekeeper.argConfig.argPic.picType
                            replyGateConfig.picPlayType =
                                gatekeeper.argConfig.argPic.picPlayType
                            replyGateConfig.picPlayTime =
                                gatekeeper.argConfig.argPic.picPlayTime
                        }
                        if (gatekeeper.argConfig.argFont != null) {
                            println("收到：argFont")
                            replyGateConfig.fontContent =
                                gatekeeper.argConfig.argFont.fontContent
                            replyGateConfig.fontColor = gatekeeper.argConfig.argFont.fontColor
                            replyGateConfig.fontSize = gatekeeper.argConfig.argFont.fontSize
                            replyGateConfig.fontLayout =
                                gatekeeper.argConfig.argFont.fontLayout
                            replyGateConfig.fontBackGround =
                                gatekeeper.argConfig.argFont.fontBackGround
                            replyGateConfig.textPosition =
                                gatekeeper.argConfig.argFont.textPosition
                        }
                        if (gatekeeper.argConfig.argVideo != null) {
                            println("收到：argVideo")
                            Universal.videoFile = gatekeeper.argConfig.argVideo.videos
                            replyGateConfig.videoAudio =
                                gatekeeper.argConfig.argVideo.videoAudio!!
                            replyGateConfig.videolayout =
                                gatekeeper.argConfig.argVideo.videoLayOut!!
                        }
                        if (gatekeeper.argConfig.argRadio != null) {
                            println("收到：argRadio 暂无")
                        }
                        if (gatekeeper.argConfig.argPicGroup != null) {
                            println("收到 argPicGroup")
                        }
                    }
                    if (replyGateConfig.save()) {
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
                    val gson = Gson()
                    val robotConfig = gson.fromJson(message, RobotConfig::class.java)
                    RobotStatus.robotConfig?.value = robotConfig
                    deleteAll(RobotConfigSql::class.java)
                    //提交数据到数据库
                    deleteFiles(File(Universal.Standby))
                    //创建文件的方法
                    createFolder()
                    Log.d(ContentValues.TAG, "obtain: 收到新的机器人配置信息")
                    val robotConfigSql = RobotConfigSql()
                    //更新数据——基础设置
                    val values = ContentValues()
                    values.put("robotmode", UpdateReturn().audioName(robotConfig.audioType!!))
                    val whereArgs = arrayOf(QuerySql.QueryBasicId().toString() + "")
                    UpDataSQL.update("basicsetting", values, "id = ?", whereArgs)
                    //单独处理女声
                    if (robotConfig.audioType == 0) {
                        UpdateReturn().randomVoice(1, QuerySql.QueryBasic().speechSpeed.toString())
                    } else {
                        UpdateReturn().randomVoice(
                            robotConfig.audioType,
                            QuerySql.QueryBasic().speechSpeed.toString()
                        )
                    }
                    //音色——放到基础设置统一管理
//                    robotConfigSql.audioType = robotConfig.audioType!!
                    robotConfigSql.wakeUpWord = robotConfig.wakeUpWord
                    robotConfigSql.sleep = robotConfig.sleep!!
                    robotConfigSql.sleepTime = robotConfig.sleepTime!!
                    robotConfigSql.wakeUpList = robotConfig.wakeUpList!!
                    robotConfigSql.sleepType = robotConfig.argConfig!!.type!!
                    robotConfigSql.mapName = robotConfig.mapName
                    robotConfigSql.timeStamp = robotConfig.timeStamp!!
                    robotConfigSql.password = robotConfig.password
                    robotConfigSql.waitingPointName = robotConfig.waitingPointName
                    robotConfigSql.chargePointName = robotConfig.chargePointName
                    if (robotConfig.argConfig.screen == 0) {
                        if (robotConfig.argConfig.argPic != null) {
                            println("收到：argPic")
                            Universal.sleepContentName = robotConfig.argConfig.argPic.pics
                            robotConfigSql.picType = robotConfig.argConfig.argPic.picType
                        }
                        if (robotConfig.argConfig.argFont != null) {
                            println("收到：小屏幕无argFont")
                        }
                        if (robotConfig.argConfig.argVideo != null) {
                            Universal.sleepContentName = robotConfig.argConfig.argVideo.videos
                            println("收到：argVideo")
                        }
                        if (robotConfig.argConfig.argRadio != null) {
                            println("收到：argRadio 暂无")
                        }
                        if (robotConfig.argConfig.argPicGroup != null) {
                            Universal.sleepContentName = robotConfig.argConfig.argPicGroup.sleepPic
                        }
                    }
                    if (robotConfigSql.save()) {
                        Log.d("TAG", "receive: 配置数据保存成功")
                        updateConfig(false)
                        Universal.mapType.postValue( false)
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 配置数据保存失败")
                    }
                }
                //云平台下发导购配置
                "replyShoppingGuideActionConfig" -> {
                    InteractionMqtt().ActionShoppingType(message)
                }
                //引领子功能配置
                "replyGuidePointConfig" -> {
                    InteractionMqtt().guidePointConfig(message)
                }

                "sendAppletTask" -> {
                    // 小程序下发任务
                    ToastUtil.show("收到远程任务..")
                    LogUtil.i("收到远程任务..")
                    RemoteOrderHelper.receiveRemoteOrder(jsonObject)
                }

                "replyGuideConfig" -> {
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
                    ReplyIntentHelper.replyIntent(message)
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
        //这里必须过几秒钟后才能赋值，否者可能会将原来数据库中的值赋给变量
        val assignmentThread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(2000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            UpdateReturn().assignment()
            if (!boolean) {
                UpdateReturn().settingMap()
            }
        }, "assignment")
        assignmentThread.start()
        //门岗配置图片&视频下载
        val thread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(2000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
//            nullData(boolean)
            if (Universal.pics != "" || Universal.videoFile != "") {
                if (Universal.pics != "") {
                    fileNames = null
                    fileNames = UpdateReturn().splitStr(Universal.pics)
                } else if (Universal.videoFile != "") {
                    fileNames = null
                    fileNames = UpdateReturn().splitStr(Universal.videoFile)
                }
//                try {
                //副屏
                if (fileNames!!.isNotEmpty()) {
                    for (i in 0 until fileNames!!.size) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + fileNames!![i],
                            Universal.Secondary,
                            FileName(fileNames!![i]!!),
                            MyApplication.listener
                        )
                    }
                }
            }
        }, "fileName")
        thread.start()

        //主屏幕休眠图片下载
        val thread1 = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(2000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
//            nullData(boolean)
            if (Universal.sleepContentName != "") {
                houseFile = UpdateReturn().splitStr(Universal.sleepContentName)
            }
            if (houseFile != null) {
                for (i in 0 until houseFile!!.size) {
                    DownloadBill.getInstance().addTask(
                        Universal.pathDownload + houseFile!![i],
                        Universal.Standby,
                        FileName(houseFile!![i]!!),
                        MyApplication.listener
                    )
                }
            }
        }, "sleepName")
        thread1.start()

        //广告图片&视频下载
        val thread2 = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(2000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
//            nullData(boolean)
            if (Universal.advVideoFile != "" || Universal.advPics != "") {
                if (Universal.advPics != "") {
                    advFile = null
                    advFile = UpdateReturn().splitStr(Universal.advPics)
                } else if (Universal.advVideoFile != "") {
                    advFile = null
                    advFile = UpdateReturn().splitStr(Universal.advVideoFile)
                }
//                try {
                //副屏
                if (advFile != null) {
                    for (i in 0 until advFile!!.size) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + advFile!![i],
                            Universal.advertisement,
                            FileName(advFile!![i]!!),
                            MyApplication.listener
                        )
                    }
                }
            }
        }, "advName")
        thread2.start()
        UpdateReturn().method(boolean)
    }

    fun FileName(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    private fun nullData() {
        if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "" && Universal.advVideoFile == "" && Universal.advPics == "") {
            Looper.prepare()
            Universal.pics = ""
            Universal.sleepContentName = ""
            Universal.videoFile = ""
            Universal.advVideoFile == ""
            Universal.advPics == ""
            RobotStatus.newUpdata.postValue(1)
            UpdateReturn().method()
            Looper.loop()
        }
    }

    fun downLoadFinish() {
        Universal.pics = ""
        Universal.sleepContentName = ""
        Universal.videoFile = ""
        Universal.advVideoFile == ""
        Universal.advPics == ""
//        LogUtil.d("1114")
//        UpdateReturn().method()
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

    fun compareArrays(array1: Array<String?>, array2: Array<String?>): SameName {
        val sameName = SameName()
        // 比较两个数组
        val commonFiles = array1.map { it?.substringAfterLast('/') }
            .intersect(array2.map { it?.substringAfterLast('/') })
        val uniqueFiles1 = array1.map { it?.substringAfterLast('/') }
            .subtract(array2.map { it?.substringAfterLast('/') })
        val uniqueFiles2 = array2.map { it?.substringAfterLast('/') }
            .subtract(array1.map { it?.substringAfterLast('/') })
        //共同包含的文件
        sameName.SameAll = commonFiles.map { name -> array1.find { it!!.endsWith("/$name") } }
        //第一个数组中独有的文件
        sameName.SameOne = uniqueFiles1.map { name -> array1.find { it!!.endsWith("/$name") } }
        //第二个数组中独有的文件
        sameName.SameTwo = uniqueFiles2.map { name -> array2.find { it!!.endsWith("/$name") } }
        return sameName
    }
}