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
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import okhttp3.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.litepal.LitePal.deleteAll
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
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
                    advertisingConfigDB.save()
                    updateConfig()
                }
                //讲解路线配置
                "replyRouteList" -> {
                    val gson = Gson()
                    val routeConfig = gson.fromJson(message, RouteConfig::class.java)
                    RobotStatus.routeConfig?.value = routeConfig
                    //所有图片存储的总路径
                    LogUtil.d("收到讲解路线配置")
                    val routeDB = RouteDB()
                    //存储图片的数据库
                    val routeList = routeConfig.routeList// 路线列表对象
                    for (route in 0 until routeList!!.size) {
//                        val isExist =
//                            LitePal.where("routename = ?", routeList[route].routeName)
//                                .count(RouteDB::class.java) > 0
                        if (QuerySql.queryTime(routeList[route].routeName) != routeList[route].timeStamp) {
                            //删除对应文件夹
                            if (routeList[route].timeStamp <= 0) {
                                deleteFolderFile(
                                    (Universal.robotFile + routeList[route].rootMapName + "/" + routeList[route].routeName),
                                    true
                                )
                            }
                            deleteAll("routedb")
                            deleteAll("pointconfigvodb")
                            deleteAll("touchscreenconfigdb")
                            deleteAll("bigscreenconfigdb")
                        }
                    }
                    val iterator = routeList.iterator()
                    while (iterator.hasNext()) {
                        val route = iterator.next()
                        if (route.timeStamp > 0) {
                            //创建对应文件夹。以路线名字命名(存放主屏幕)
                            openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/")
                            //创建对应文件夹。以路线名字命名(存放主屏幕)
                            openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
//                            openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + "group/")
                            openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/")
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
                            if (route.backgroundPic?.isNotEmpty() == true) {
                                selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                                val sdcardFile =
                                    selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                                val picfile =
                                    UpdateReturn().splitStr(route.backgroundPic)

                                val backPic = compareArrays(sdcardFile, picfile)
                                for (i in backPic.SameOne!!.indices) {
                                    deleteFolderFile(backPic.SameOne!![i], true)
                                }
                                routeDB.backgroundPic =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + route.backgroundPic.substring(
                                        route.backgroundPic.lastIndexOf("/") + 1
                                    ) //路线背景图
                                if (backPic.SameTwo?.isNotEmpty() == true) {
                                    Thread {
                                        for (i in backPic.SameTwo!!.indices) {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + backPic.SameTwo!![i],
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch",
                                                FileName(backPic.SameTwo!![i]!!),
                                                MyApplication.listener
                                            )
                                        }
                                    }.start()
                                }
                            } else {
                                deleteFolderFile(
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/",
                                    true
                                )
                            }
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
                                pointConfigVODB.explanationText = point.explanation
                                //排序
                                pointConfigVODB.scope = point.scope!!
                                //途径音频.mp3
                                if (point.walkVoice?.isNotEmpty() == true) {
                                    pointConfigVODB.walkVoice =
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/" + (point.walkVoice).substring(
                                            (point.walkVoice).lastIndexOf("/") + 1
                                        )
                                    val sdcardFile =
                                        selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
                                    val picfile =
                                        UpdateReturn().splitStr(point.walkVoice)

                                    val walkMp3 = compareArrays(sdcardFile, picfile)

                                    if (walkMp3.SameTwo?.isNotEmpty() == true) {
                                        Thread {
                                            for (i in walkMp3.SameTwo!!.indices) {
                                                DownloadBill.getInstance().addTask(
                                                    Universal.pathDownload + walkMp3.SameTwo!![i],
                                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                                    FileName(walkMp3.SameTwo!![i]!!),
                                                    MyApplication.listener
                                                )
                                            }
                                        }.start()
                                    }
                                }
                                //到达讲解.mp3
                                if (point.explanationVoice?.isNotEmpty() == true) {
                                    pointConfigVODB.explanationVoice =
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/" + (point.explanationVoice).substring(
                                            (point.explanationVoice).lastIndexOf("/") + 1
                                        )

                                    val sdcardFile =
                                        selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
                                    val picfile =
                                        UpdateReturn().splitStr(point.explanationVoice)

                                    val explanationMp3 = compareArrays(sdcardFile, picfile)
                                    if (explanationMp3.SameTwo?.isNotEmpty() == true) {
                                        Thread {
                                            for (i in explanationMp3.SameTwo!!.indices) {
                                                DownloadBill.getInstance().addTask(
                                                    Universal.pathDownload + explanationMp3.SameTwo!![i],
                                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                                    FileName(explanationMp3.SameTwo!![i]!!),
                                                    MyApplication.listener
                                                )
                                            }
                                        }.start()
                                    }
                                }
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
                                        openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                                        val sdcardFile =
                                            selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                                        val picfile =
                                            UpdateReturn().splitStr(point.bigScreenConfig.argPic.pics)
                                        val bigPic = compareArrays(sdcardFile, picfile)

                                        for (i in bigPic.SameOne!!.indices) {
                                            deleteFolderFile(bigPic.SameOne!![i], true)
                                        }
                                        //创建对应文件夹。以路线名字命名(存放大屏幕)
                                        bigScreenConfigDB.imageFile =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name
//                                            val bigPicFile =
//                                                UpdateReturn().splitStr(point.bigScreenConfig.argPic.pics)
                                        if (bigPic.SameTwo?.isNotEmpty() == true) {
                                            Thread {
                                                for (i in bigPic.SameTwo!!.indices) {
                                                    DownloadBill.getInstance().addTask(
                                                        Universal.pathDownload + bigPic.SameTwo!![i],
                                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                                        FileName(bigPic.SameTwo!![i]!!),
                                                        MyApplication.listener
                                                    )
                                                }
                                            }.start()
                                        }
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
                                        bigScreenConfigDB.videolayout =
                                            point.bigScreenConfig.argVideo.videoLayOut!!
                                        //视频储存位置
                                        //创建对应文件夹。以路线名字命名(存放大屏幕)
                                        openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)

                                        val sdcardFile =
                                            selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                                        val picfile =
                                            UpdateReturn().splitStr(point.bigScreenConfig.argVideo.videos)
                                        val argVideoName = compareArrays(sdcardFile, picfile)
                                        for (i in argVideoName.SameOne!!.indices) {
                                            deleteFolderFile(argVideoName.SameOne!![i], true)
                                        }
                                        bigScreenConfigDB.videoFile =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name
                                        if (argVideoName.SameTwo?.isNotEmpty() == true) {
                                            Thread {
                                                for (i in argVideoName.SameTwo!!.indices) {
                                                    DownloadBill.getInstance().addTask(
                                                        Universal.pathDownload + argVideoName.SameTwo!![i],
                                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                                        FileName(argVideoName.SameTwo!![i]!!),
                                                        MyApplication.listener
                                                    )
                                                }
                                            }.start()
                                        }
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
                                            point.touchScreenConfig.argPic.picType
                                        //轮播时间
                                        touchScreenConfigDB.touch_picPlayTime =
                                            point.touchScreenConfig.argPic.picPlayTime
                                        //图片路径
                                        if (point.touchScreenConfig.argPic.pics.isNotEmpty()) {
                                            val sdcardFile =
                                                selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                                            val picfile =
                                                UpdateReturn().splitStr(point.touchScreenConfig.argPic.pics)
                                            val touchFileName = compareArrays(sdcardFile, picfile)

                                            for (i in touchFileName.SameOne!!.indices) {
                                                deleteFolderFile(touchFileName.SameOne!![i], true)
                                            }
                                            touchScreenConfigDB.touch_imageFile =
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/"

                                            if (touchFileName.SameTwo!!.isNotEmpty()) {
                                                Thread {
                                                    for (i in touchFileName.SameTwo!!.indices) {
                                                        DownloadBill.getInstance().addTask(
                                                            Universal.pathDownload + touchFileName.SameTwo!![i],
                                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch",
                                                            FileName(touchFileName.SameTwo!![i]!!),
                                                            MyApplication.listener
                                                        )
                                                    }
                                                }.start()
                                            }
                                        }
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
                                    if (point.touchScreenConfig.argPicGroup != null) {
                                        //行走中
                                        touchScreenConfigDB.touch_walkPic =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + point.touchScreenConfig.argPicGroup.walkPic
                                        Thread {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + point.touchScreenConfig.argPicGroup.walkPic,
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                                FileName(point.touchScreenConfig.argPicGroup.walkPic!!),
                                                MyApplication.listener
                                            )
                                        }.start()
                                        //被阻挡
                                        touchScreenConfigDB.touch_blockPic =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + point.touchScreenConfig.argPicGroup.blockPic
                                        Thread {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + point.touchScreenConfig.argPicGroup.blockPic,
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                                FileName(point.touchScreenConfig.argPicGroup.blockPic!!),
                                                MyApplication.listener
                                            )
                                        }.start()
                                        //到点
                                        touchScreenConfigDB.touch_arrivePic =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + point.touchScreenConfig.argPicGroup.arrivePic
                                        Thread {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + point.touchScreenConfig.argPicGroup.arrivePic,
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                                FileName(point.touchScreenConfig.argPicGroup.arrivePic!!),
                                                MyApplication.listener
                                            )
                                        }.start()
                                        //返回
                                        touchScreenConfigDB.touch_overTaskPic =
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + point.touchScreenConfig.argPicGroup.overTaskPic
                                        Thread {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + point.touchScreenConfig.argPicGroup.overTaskPic,
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                                FileName(point.touchScreenConfig.argPicGroup.overTaskPic!!),
                                                MyApplication.listener
                                            )
                                        }.start()
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
                    replyGateConfig.save()
                    updateConfig()

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
            nullData()
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
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            nullData()
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
                Thread.sleep(10000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            nullData()
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
        UpdateReturn().method()
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