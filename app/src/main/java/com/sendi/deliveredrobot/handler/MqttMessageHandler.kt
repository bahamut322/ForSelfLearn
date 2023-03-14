package com.sendi.deliveredrobot.handler

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModelLazy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.entity.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import okhttp3.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.litepal.LitePal
import org.litepal.LitePal.deleteAll
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread


/**
 *   @author: heky
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
    private val gson = Gson()
    private val floorNameSet = HashSet<String>()
    private var fileNames: Array<String?>? = null//副屏内容
    private var houseFile: Array<String?>? = null//主屏内容
    private var advFile: Array<String?>? = null //广告内容


    /**
     * @describe 接收消息
     */
    @SuppressLint("SdCardPath")
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
                    explainConfigDB.pointListText = explainConfig.pointListText
                    explainConfigDB.startText = explainConfig.startText
                    explainConfigDB.endText = explainConfig.endText
                    explainConfigDB.interruptionText = explainConfig.interruptionText
                    explainConfigDB.timeStamp = explainConfig.timeStamp!!
                    explainConfigDB.save()
                }
                //广告配置
                "replyAdvertisementConfig" -> {
                    val gson = Gson()
                    val advertisingConfig = gson.fromJson(message, AdvertisingConfig::class.java)
                    deleteAll(AdvertisingConfigDB::class.java)
                    deleteFiles(File(Universal.advertisement))
                    RobotStatus.advertisingConfig?.value = advertisingConfig
                    DialogHelper.loadingDialog.show()
                    LogUtil.d("收到广告配置")
                    //创建文件的方法
                    createFolder()
                    val advertisingConfigDB = AdvertisingConfigDB()
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
                        advertisingConfigDB.fontSize = advertisingConfig.argConfig.argFont.fontSize
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
                    }
                    if (advertisingConfig.argConfig.argRadio != null) {
                        LogUtil.d("广告配置收到了argRadio,提示暂无此配置")
                    }
                    if (advertisingConfig.argConfig.argPicGroup != null) {
                        LogUtil.d("广告配置收到了argPicGroup,提示暂无此配置")
                    }
                    advertisingConfigDB.save()
                    updateConfig()
                }
                //讲解路线配置
                "replyRouteList" -> {
                    val gson = Gson()
                    val routeConfig = gson.fromJson(message, RouteConfig::class.java)
                    RobotStatus.routeConfig?.value = routeConfig
                    LogUtil.d("收到讲解路线配置")
                    val routeDB = RouteDB()
                    val routeList = routeConfig.routeList// 路线列表对象
                    for (route in 0 until routeList!!.size) {
                        val isExist =
                            LitePal.where("routename = ?", routeList[route].routeName)
                                .count(RouteDB::class.java) > 0
                        if (isExist && QuerySql.queryTime(routeList[route].routeName) != routeList[route].timeStamp) {
                            //删除对应文件夹
                            deleteFolderFile((Universal.robotFile + routeList[route].rootMapName +"/" + routeList[route].routeName),true)
                            deleteAll(
                                BigScreenConfigDB::class.java,
                                "pointconfigvodb_id = ?",
                                QuerySql.pointConfigVoDB_id(routeList[route].routeName).toString()
                            )
                            deleteAll(
                                TouchScreenConfigDB::class.java,
                                "pointconfigvodb_id = ?",
                                QuerySql.pointConfigVoDB_id(routeList[route].routeName).toString()
                            )
                            deleteAll(
                                PointConfigVODB::class.java,
                                "routedb_id = ?",
                                QuerySql.routeDB_id(routeList[route].routeName).toString()
                            )
                            deleteAll(
                                RouteDB::class.java,
                                "routename = ? ",
                                routeList[route].routeName
                            )
                        }
                    }
                    val iterator = routeList.iterator()
                    while (iterator.hasNext()) {
                        val route = iterator.next()
                        if (route.timeStamp > 0) {
                            //创建对应文件夹。以路线名字命名(存放大屏幕)
                            openFile(Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/big/")
                            //创建对应文件夹。以路线名字命名(存放主屏幕)
                            openFile(Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/touch/")
                            //创建对应文件夹。以路线名字命名(存放主屏幕)
                            openFile(Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/mp3/")
                            val pointIterator = route.pointConfigVOList.iterator()
                            //路线名字
                            routeDB.routeName = route.routeName
                            //总图名字
                            routeDB.rootMapName = route.rootMapName
                            //简介
                            routeDB.introduction = route.introduction
                            //配置时间戳
                            routeDB.timeStamp = route.timeStamp
                            //路线背景图
                            routeDB.backgroundPic =
                                Universal.robotFile + route.rootMapName +"/"+  route.routeName + "/touch/" + (route.backgroundPic!!).substring((route.backgroundPic).lastIndexOf("/") + 1) //路线背景图
                            Thread {
                                if (route.backgroundPic != null) {
                                    downloadFile(
                                        Universal.pathDownload + route.backgroundPic,
                                        Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/touch"
                                    )
                                }
                            }.start()

                            var pointItem: List<PointConfigVODB>
                            while (pointIterator.hasNext()) {
                                val point = pointIterator.next()
                                pointItem = ArrayList()
                                val pointConfigVODB = PointConfigVODB()
                                //点名
                                pointConfigVODB.name = point.name
                                //途径播报内容-播报语(200)
                                pointConfigVODB.walkText = point.walkText
                                //讲解播报内容-播报语(200)
                                pointConfigVODB.explanationText = point.explanationText
                                //排序
                                pointConfigVODB.scope = point.scope!!
                                //途径音频.mp3
                                pointConfigVODB.walkVoice =
                                    Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/mp3/" +  (point.walkVoice!!).substring((point.walkVoice).lastIndexOf("/") + 1)
                                val walkVoiceFile =
                                    UpdateReturn().splitStr(point.walkVoice!!)
                                Thread {
                                    for (i in walkVoiceFile!!.indices) {
                                        downloadFile(
                                            Universal.pathDownload + walkVoiceFile[i],
                                            Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/mp3"
                                        )
                                    }
                                }.start()
                                //到达讲解.mp3
                                pointConfigVODB.explanationVoice =
                                    Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/mp3/" +  (point.explanationVoice!!).substring((point.explanationVoice).lastIndexOf("/") + 1)
                                val explanationVoiceFile =
                                    UpdateReturn().splitStr(point.explanationVoice!!)
                                Thread {
                                    for (i in explanationVoiceFile!!.indices) {
                                        downloadFile(
                                            Universal.pathDownload + explanationVoiceFile[i],
                                            Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/mp3"
                                        )
                                    }
                                }.start()
                                //大屏配置
                                if (point.bigScreenConfig!!.screen == 1) {
                                    val bigScreenConfigDB = BigScreenConfigDB()
                                    //配置类型
                                    bigScreenConfigDB.type =
                                        point.bigScreenConfig.type!!
                                    if (point.bigScreenConfig.argPic != null) {
                                        //图片布局
                                        bigScreenConfigDB.picType =
                                            point.bigScreenConfig.argPic.picType
                                        //轮播时间
                                        bigScreenConfigDB.picPlayTime =
                                            point.bigScreenConfig.argPic.picPlayTime
                                        //图片
                                        bigScreenConfigDB.imageFile =
                                            Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/big/" + (point.bigScreenConfig.argPic.pics!!).substring((point.bigScreenConfig.argPic.pics).lastIndexOf("/") + 1)
                                        val bigPicFile =
                                            UpdateReturn().splitStr(point.bigScreenConfig.argPic.pics)
                                        Thread {
                                            for (i in bigPicFile!!.indices) {
                                                downloadFile(
                                                    Universal.pathDownload + bigPicFile[i],
                                                    Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/big"
                                                )
                                            }
                                        }.start()
                                    }
                                    if (point.bigScreenConfig.argFont != null) {
                                        //文字
                                        bigScreenConfigDB.fontContent =
                                            point.bigScreenConfig.argFont.fontContent
                                        //文字颜色
                                        bigScreenConfigDB.fontColor =
                                            point.bigScreenConfig.argFont.fontColor
                                        //文字大小 1-大，2-中，3-小,
                                        bigScreenConfigDB.fontSize =
                                            point.bigScreenConfig.argFont.fontSize
                                        //文字方向 1-横向，2-纵向
                                        bigScreenConfigDB.fontLayout =
                                            point.bigScreenConfig.argFont.fontLayout
                                        //背景颜色
                                        bigScreenConfigDB.fontBackGround =
                                            point.bigScreenConfig.argFont.fontBackGround
                                        //文字显示位置  0-居中 1-居上 2-居下
                                        bigScreenConfigDB.textPosition =
                                            point.bigScreenConfig.argFont.textPosition
                                    }
                                    if (point.bigScreenConfig.argVideo != null) {
                                        //视频是否播放声音
                                        bigScreenConfigDB.videoAudio =
                                            point.bigScreenConfig.argVideo.videoAudio!!
                                        //视频储存位置
                                        bigScreenConfigDB.videoFile =
                                            Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/big/" +
                                        (point.bigScreenConfig.argVideo.videos!!).substring((point.bigScreenConfig.argVideo.videos).lastIndexOf("/") + 1)
                                        val bigVideoFile =
                                            UpdateReturn().splitStr(point.bigScreenConfig.argVideo.videos!!)
                                        Thread {
                                            for (i in bigVideoFile!!.indices) {
                                                downloadFile(
                                                    Universal.pathDownload + bigVideoFile[i],
                                                    Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/big"
                                                )
                                            }
                                        }.start()
                                    }
                                    bigScreenConfigDB.save()
                                    pointConfigVODB.bigScreenConfigDB = bigScreenConfigDB
                                }
                                //小屏
                                if (point.touchScreenConfig!!.screen == 0) {
                                    val touchScreenConfigDB = TouchScreenConfigDB()
                                    //配置类型
                                    touchScreenConfigDB.touch_type =
                                        point.touchScreenConfig.type!!
                                    if (point.touchScreenConfig.argPic != null) {
                                        //图片布局
                                        touchScreenConfigDB.touch_picType =
                                            point.touchScreenConfig.argPic!!.picType
                                        //轮播时间
                                        touchScreenConfigDB.touch_picPlayTime =
                                            point.touchScreenConfig.argPic.picPlayTime
                                        //图片路径
                                        touchScreenConfigDB.touch_imageFile =
                                            Universal.robotFile + route.rootMapName +"/"+ route.routeName + "/touch/" + (point.touchScreenConfig.argPic.pics).substring((point.touchScreenConfig.argPic.pics).lastIndexOf("/") + 1)
                                        val touchPicFile =
                                            UpdateReturn().splitStr(point.touchScreenConfig.argPic.pics)
                                        Thread {
                                            for (i in touchPicFile!!.indices) {
                                                downloadFile(
                                                    Universal.pathDownload + touchPicFile[i],
                                                    Universal.robotFile  + route.rootMapName +"/"+ route.routeName + "/touch"
                                                )
                                            }
                                        }.start()
                                    }
                                    if (point.touchScreenConfig.argFont != null) {
                                        //文字
                                        touchScreenConfigDB.touch_fontContent =
                                            point.touchScreenConfig.argFont.fontContent
                                        //文字颜色
                                        touchScreenConfigDB.touch_fontColor =
                                            point.touchScreenConfig.argFont.fontColor
                                        //文字大小 1-大，2-中，3-小,
                                        touchScreenConfigDB.touch_fontSize =
                                            point.touchScreenConfig.argFont.fontSize
                                        //文字方向 1-横向，2-纵向
                                        touchScreenConfigDB.touch_fontLayout =
                                            point.touchScreenConfig.argFont.fontLayout
                                        //背景颜色
                                        touchScreenConfigDB.touch_fontBackGround =
                                            point.touchScreenConfig.argFont.fontBackGround
                                        //文字显示位置  0-居中 1-居上 2-居下
                                        touchScreenConfigDB.touch_textPosition =
                                            point.touchScreenConfig.argFont.textPosition
                                    }
                                    touchScreenConfigDB.save()
                                    pointConfigVODB.touchScreenConfigDB = touchScreenConfigDB
                                }
                                pointConfigVODB.save()
                                pointItem.add(pointConfigVODB)
                                routeDB.mapPointName = pointItem
                                routeDB.save()

                            }
                        }
                    }

                }

                //机器人门岗配置
                "replyGateConfig" -> {
                    val gson = Gson()
                    val gatekeeper = gson.fromJson(message, Gatekeeper::class.java)
                    RobotStatus.gatekeeper?.value = gatekeeper
                    DialogHelper.loadingDialog.show()
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
                        }
                        if (gatekeeper.argConfig.argRadio != null) {
                            println("收到：argRadio 暂无")
                        }
                        if (gatekeeper.argConfig.argPicGroup != null) {
                            println("收到 argPicGroup")
                        }
                    }
                    replyGateConfig.save()
                    updateConfig()

                }

                //机器人配置
                "replyRobotConfig" -> {
                    val gson = Gson()
                    val robotConfig = gson.fromJson(message, RobotConfig::class.java)
                    RobotStatus.robotConfig?.value = robotConfig
                    DialogHelper.loadingDialog.show()
                    deleteAll(RobotConfigSql::class.java)
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
                    robotConfigSql.sleepType = robotConfig.argConfig!!.type!!
                    robotConfigSql.mapName = robotConfig.mapName
                    robotConfigSql.timeStamp = robotConfig.timeStamp!!
                    robotConfigSql.password = robotConfig.password
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
                            println("收到 未编写argPicGroup")
                        }
                    }
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

    private fun openFile(file: String) {
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
    private fun deleteFiles(file: File): Boolean {
        return try {
            if (file.isDirectory) { //判断是否是文件夹
                val files = file.listFiles() //遍历文件夹里面的所有的
                for (i in files!!.indices) {
                    LogUtil.e("更新原有文件>>>>>> " + files[i].toString())
                    deleteFiles(files[i]) //删除
                }
            } else {
                file.delete()
                LogUtil.e("删除文件夹>>>>>> " + file.toString())
            }
            System.gc() //系统回收垃圾
            true
        } catch (e: Exception) {
            LogUtil.e("更新报错！！！: $e")
            false
        }
    }


    private fun updateConfig() {
        //这里必须过几秒钟后才能赋值，否者可能会将原来数据库中的值赋给变量
        val assignmentThread = Thread({
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
        //门岗配置图片&视频下载
        val thread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            UpdateReturn().mapSetting()
            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "") {
                DialogHelper.loadingDialog.dismiss()
                Universal.pics = ""
                Universal.sleepContentName = ""
                Universal.videoFile = ""
                RobotStatus.newUpdata.postValue(1)
                UpdateReturn().method()
            }
            if (Universal.pics != "" || Universal.videoFile != "" || Universal.sleepContentName != "" || Universal.advVideoFile != "" || Universal.advPics != "") {
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
                        downloadFile(
                            "http://172.168.201.34:9055/management_res/" + fileNames!![i],
                            Universal.Secondary
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
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "" && Universal.advVideoFile == "" && Universal.advPics == "") {
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
                houseFile = UpdateReturn().splitStr(Universal.sleepContentName)
            }
            if (houseFile != null) {
                for (i in 0 until houseFile!!.size) {
                    downloadFile(
                        "http://172.168.201.34:9055/management_res/" + houseFile!![i],
                        Universal.Standby
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
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (Universal.pics == "" && Universal.videoFile == "" && Universal.sleepContentName == "" && Universal.advVideoFile == "" && Universal.advPics == "") {
                DialogHelper.loadingDialog.dismiss()
                Universal.pics = ""
                Universal.sleepContentName = ""
                Universal.videoFile = ""
                RobotStatus.newUpdata.postValue(1)
                UpdateReturn().method()
            }
            if (Universal.pics != "" || Universal.videoFile != "" || Universal.sleepContentName != "" || Universal.advVideoFile != "" || Universal.advPics != "") {
                if (Universal.advPics != "") {
                    advFile = null
                    advFile = UpdateReturn().splitStr(Universal.advPics)
                } else if (Universal.advVideoFile != "") {
                    advFile = null
                    advFile = UpdateReturn().splitStr(Universal.advVideoFile)
                }
//                try {
                //副屏
                if (advFile!!.isNotEmpty()) {
                    for (i in 0 until advFile!!.size) {
                        downloadFile(
                            "http://172.168.201.34:9055/management_res/" + advFile!![i],
                            Universal.advertisement
                        )
                    }
                }
            }
        }, "advName")
        thread2.start()
    }

    /**
     * 下载方法
     * @param url 下载地址
     * @param savePath 储存本地地址
     */
    private fun downloadFile(url: String, savePath: String) {
        val startTime = System.currentTimeMillis()
        LogUtil.e("开始下载：$url")
        DialogHelper.loadingDialog.show()
        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 下载失败
                e.printStackTrace()
                LogUtil.e("下载失败：$url ")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                try {
                    `is` = response.body!!.byteStream()
                    val total = response.body!!.contentLength()
                    val file = File(savePath, url.substring(url.lastIndexOf("/") + 1))
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (`is`.read(buf).also { len = it } != -1) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        // 下载中
                        LogUtil.e("下载中：$file: $progress %")
                    }
                    fos.flush()
                    // 下载完成
                    if (UpdateReturn().splitStr(Universal.sleepContentName)!!.isEmpty()) {
                        if (fileNames!!.size == UpdateReturn().fileSize(Universal.Secondary)) {
                            DialogHelper.loadingDialog.dismiss()
                            Universal.pics = ""
                            Universal.sleepContentName = ""
                            Universal.videoFile = ""
                            RobotStatus.newUpdata.postValue(1)
                            UpdateReturn().method()
                        }
                    } else {
                        if (fileNames!!.size + UpdateReturn().splitStr(Universal.sleepContentName)!!.size == UpdateReturn().fileSize(
                                Universal.Secondary
                            ) + UpdateReturn().fileSize(Universal.Standby)
                        ) {
                            DialogHelper.loadingDialog.dismiss()
                            Universal.pics = ""
                            Universal.sleepContentName = ""
                            Universal.videoFile = ""
                            RobotStatus.newUpdata.postValue(1)
                            UpdateReturn().method()
                        }
                    }
                    LogUtil.e("下载成功：$file,下载耗时= ${(System.currentTimeMillis() - startTime)} ms")
                    DialogHelper.loadingDialog.dismiss()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    LogUtil.e("下载失败!!")
                    DialogHelper.loadingDialog.dismiss()
                } finally {
                    try {
                        `is`?.close()
                    } catch (_: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (_: IOException) {
                    }
                }
            }
        })
    }

    /**
     * 删除目录下所有文件
     * @param filePath 目录地址
     * @param deleteThisPath 是否删除目录
     */
    fun deleteFolderFile(filePath: String?, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) { //目录
                    val files = file.listFiles()
                    for (i in files.indices) {
                        deleteFolderFile(files[i].absolutePath, true)
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) { //如果是文件，删除
                        file.delete()
                    } else { //目录
                        if (file.listFiles().size == 0) { //目录下没有文件或者目录，删除
                            file.delete()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }
}