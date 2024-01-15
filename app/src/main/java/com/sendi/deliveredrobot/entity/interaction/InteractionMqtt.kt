package com.sendi.deliveredrobot.entity.interaction

import android.content.ContentValues
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.entity.GuideConfig
import com.sendi.deliveredrobot.entity.Table_Big_Screen
import com.sendi.deliveredrobot.entity.Table_Face
import com.sendi.deliveredrobot.entity.Table_Greet_Config
import com.sendi.deliveredrobot.entity.Table_Guide_Foundation
import com.sendi.deliveredrobot.entity.Table_Guide_Point_Pic
import com.sendi.deliveredrobot.entity.Table_Point_Config
import com.sendi.deliveredrobot.entity.Table_Route
import com.sendi.deliveredrobot.entity.Table_Shopping_Action
import com.sendi.deliveredrobot.entity.Table_Touch_Screen
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.DeleteSql
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.handler.MqttMessageHandler
import com.sendi.deliveredrobot.model.ActionsList
import com.sendi.deliveredrobot.model.GuidePointList
import com.sendi.deliveredrobot.model.ReplyGreetConfigModel
import com.sendi.deliveredrobot.model.RouteConfig
import com.sendi.deliveredrobot.model.guideFoundationModel
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import org.litepal.LitePal
import org.litepal.LitePal.updateAll
import java.io.File
import kotlin.concurrent.thread


/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe
 */
class InteractionMqtt {


    fun ActionShoppingType(message: String) {
        val gson = Gson()
        var shoppingActionDB: Table_Shopping_Action
        var map: LinkedHashMap<String, Long>
        val shoppingConfig = gson.fromJson(message, ActionsList::class.java)
        RobotStatus.shoppingActionList?.value = shoppingConfig
        Log.d("TAG", "收到导购配置")
        val actionList = shoppingConfig.actions// 路线列表对象
        val iterator = actionList?.iterator()
        //开始通过迭代器遍历导购点
        while (iterator!!.hasNext()) {
            shoppingActionDB = Table_Shopping_Action()
            val action = iterator.next()
            //删除某个点
            if (action.timeStamp!! <= 0) {
                //删除文件夹
                MqttMessageHandler.deleteFiles(File(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name))
                //删除数据库中的大屏
                DeleteSql.deleteBigPic((Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"))
                //删除数据库中的小屏
                DeleteSql.deleteTouchPic((Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/"))
                //删除点
                if (DeleteSql.deleteShoppingAction(action.name, action.rootMapName)) {
                    LogUtil.d("导购：地图${action.rootMapName}中${action.name}删除成功")
                } else {
                    LogUtil.d("导购：地图${action.rootMapName}中${action.name}删除失败")
                }
                RobotStatus.newUpdata.postValue(2)
                continue
            }
            map = LinkedHashMap()
            if (QuerySql.SelectShoppingAction(action.rootMapName) != null) {
                //将数据添加到LinkHashMap
                for (actionHash in QuerySql.SelectShoppingAction(action.rootMapName)) {
                    Log.d("", "ActionShoppingType: HashMap添加数据")
                    map[actionHash.name!!] = actionHash.timestamp!!
                }

                //HashMap查询数据
                val timestamp = map[action.name]
                if (timestamp != null && action.timeStamp != timestamp) {
                    Log.e("TAG", "ActionShoppingType: 查询到时间戳更改")
                    // 删除map中的数据
                    map.remove(action.name)
                    //删除对应的图片文件夹
                    MqttMessageHandler.deleteFiles(File(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name))
                    //删除数据库中的大屏
                    DeleteSql.deleteBigPic((Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"))
                    //删除数据库中的小屏
                    DeleteSql.deleteTouchPic((Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/"))
                    //删除数据库中的数据
                    DeleteSql.deleteShoppingAction(action.name, action.rootMapName)
                    println("Name: $action.name, Timestamp: $timestamp")
                } else if (action.timeStamp == timestamp) {
                    RobotStatus.newUpdata.postValue(2)
                    continue
                }
            }

            shoppingActionDB.name = action.name
            shoppingActionDB.actionType = action.actionType
            shoppingActionDB.pointName = action.pointName
            shoppingActionDB.waitingTime = action.waitingTime
            shoppingActionDB.standText = action.standText
            shoppingActionDB.arriveText = action.arriveText
            shoppingActionDB.moveText = action.moveText
            shoppingActionDB.timestamp = action.timeStamp
            shoppingActionDB.rootMapName = action.rootMapName
            //大屏幕配置
            if (action.bigScreenConfig!!.screen == 1) {
                val tableBigScreen =
                    Table_Big_Screen()
                //配置类型
                tableBigScreen.type =
                    action.bigScreenConfig!!.type!!
                if (action.bigScreenConfig!!.argPic != null) {
                    //图片布局
                    tableBigScreen.picType =
                        action.bigScreenConfig!!.argPic?.picType!!
                    //轮播时间
                    tableBigScreen.picPlayTime =
                        action.bigScreenConfig!!.argPic!!.picPlayTime
                    //图片
                    MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/")
                    val picfile = MqttMessageHandler.compareArrays(
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/",
                        action.bigScreenConfig!!.argPic!!.pics
                    )
                    //创建对应文件夹。以路线名字命名(存放大屏幕)
                    tableBigScreen.imageFile =
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"
                    Thread {
                        for (i in picfile!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + picfile[i],
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/",
                                MqttMessageHandler.FileName(picfile[i]!!),
                                MyApplication.listener
                            )
                        }
                    }.start()
                }
                if (action.bigScreenConfig!!.argFont != null) {
                    //文字
                    tableBigScreen.fontContent =
                        action.bigScreenConfig!!.argFont?.fontContent
                    //文字颜色
                    tableBigScreen.fontColor =
                        action.bigScreenConfig!!.argFont?.fontColor
                    //文字大小 1-大，2-中，3-小,
                    tableBigScreen.fontSize =
                        action.bigScreenConfig!!.argFont?.fontSize!!
                    //文字方向 1-横向，2-纵向
                    tableBigScreen.fontLayout =
                        action.bigScreenConfig!!.argFont?.fontLayout!!
                    //背景颜色
                    tableBigScreen.fontBackGround =
                        action.bigScreenConfig!!.argFont?.fontBackGround
                    //文字显示位置  0-居中 1-居上 2-居下
                    tableBigScreen.textPosition =
                        action.bigScreenConfig!!.argFont?.textPosition!!
                }
                if (action.bigScreenConfig!!.argVideo != null) {
                    //视频是否播放声音
                    tableBigScreen.videoAudio =
                        action.bigScreenConfig!!.argVideo?.videoAudio!!
                    tableBigScreen.videolayout =
                        action.bigScreenConfig!!.argVideo?.videoLayOut!!
                    //视频储存位置
                    //创建对应文件夹。以路线名字命名(存放大屏幕)
                    MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/")
                    val picfile = MqttMessageHandler.compareArrays(
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/",
                        action.bigScreenConfig!!.argVideo?.videos!!
                    )
                    tableBigScreen.videoFile =
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"
                    Thread {
                        for (i in picfile!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + picfile[i],
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/",
                                MqttMessageHandler.FileName(picfile[i]!!),
                                MyApplication.listener
                            )
                        }
                    }.start()
                }
                tableBigScreen.save()
                shoppingActionDB.bigScreenConfig = tableBigScreen
            }

            if (action.touchScreenConfig!!.screen == 0) {
                //创建对应文件夹。以路线名字命名(存放主屏幕)
                MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/")
                val tableTouchScreen =
                    Table_Touch_Screen()
                //配置类型
                tableTouchScreen.touch_type =
                    action.touchScreenConfig!!.type!!
                if (action.touchScreenConfig!!.argPic != null) {
                    //图片布局
                    tableTouchScreen.touch_picType =
                        action.touchScreenConfig!!.argPic?.picType!!
                    //轮播时间
                    tableTouchScreen.touch_picPlayTime =
                        action.touchScreenConfig!!.argPic?.picPlayTime!!
                    //图片路径
                    if (action.touchScreenConfig!!.argPic?.pics!!.isNotEmpty()) {
                        val picfile = MqttMessageHandler.compareArrays(
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/",
                            action.touchScreenConfig!!.argPic?.pics!!
                        )
                        tableTouchScreen.touch_imageFile =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/"
                        Thread {
                            for (i in picfile!!.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + picfile[i],
                                    Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/",
                                    MqttMessageHandler.FileName(picfile[i]!!),
                                    MyApplication.listener
                                )
                            }
                        }.start()
                    }
                }
                if (action.touchScreenConfig!!.argFont != null) {
                    //文字
                    tableTouchScreen.touch_fontContent =
                        action.touchScreenConfig!!.argFont!!.fontContent
                    //文字颜色
                    tableTouchScreen.touch_fontColor =
                        action.touchScreenConfig!!.argFont!!.fontColor
                    //文字大小 1-大，2-中，3-小,
                    tableTouchScreen.touch_fontSize =
                        action.touchScreenConfig!!.argFont!!.fontSize
                    //文字方向 1-横向，2-纵向
                    tableTouchScreen.touch_fontLayout =
                        action.touchScreenConfig!!.argFont!!.fontLayout
                    //背景颜色
                    tableTouchScreen.touch_fontBackGround =
                        action.touchScreenConfig!!.argFont!!.fontBackGround
                    //文字显示位置  0-居中 1-居上 2-居下
                    tableTouchScreen.touch_textPosition =
                        action.touchScreenConfig!!.argFont!!.textPosition
                }
                if (action.touchScreenConfig!!.argPicGroup != null) {
                    //行走中
                    if (action.touchScreenConfig!!.argPicGroup!!.walkPic != "") {
                        tableTouchScreen.touch_walkPic =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/" + MqttMessageHandler.FileName(
                                action.touchScreenConfig!!.argPicGroup!!.walkPic!!
                            )
                        Thread {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + action.touchScreenConfig!!.argPicGroup!!.walkPic,
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/",
                                MqttMessageHandler.FileName(action.touchScreenConfig!!.argPicGroup!!.walkPic!!),
                                MyApplication.listener
                            )
                        }.start()
                    } else {
                        tableTouchScreen.touch_walkPic = Universal.gifDefault
                    }
                    //被阻挡
                    if (action.touchScreenConfig!!.argPicGroup!!.blockPic != "") {
                        tableTouchScreen.touch_blockPic =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/" + MqttMessageHandler.FileName(
                                action.touchScreenConfig!!.argPicGroup!!.blockPic!!
                            )
                        Thread {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + action.touchScreenConfig!!.argPicGroup!!.blockPic!!,
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/",
                                MqttMessageHandler.FileName(action.touchScreenConfig!!.argPicGroup!!.blockPic!!),
                                MyApplication.listener
                            )
                        }.start()
                    } else {
                        tableTouchScreen.touch_blockPic =
                            Universal.gifDefault
                    }
                    //到点
                    if (action.touchScreenConfig!!.argPicGroup!!.arrivePic != "") {
                        tableTouchScreen.touch_arrivePic =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/" + MqttMessageHandler.FileName(
                                action.touchScreenConfig!!.argPicGroup!!.arrivePic!!
                            )
                        Thread {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + action.touchScreenConfig!!.argPicGroup!!.arrivePic!!,
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/",
                                MqttMessageHandler.FileName(action.touchScreenConfig!!.argPicGroup!!.arrivePic!!),
                                MyApplication.listener
                            )
                        }.start()
                    } else {
                        tableTouchScreen.touch_arrivePic =
                            Universal.gifDefault
                    }
                    //返回
                    if (action.touchScreenConfig!!.argPicGroup!!.overTaskPic != "") {
                        tableTouchScreen.touch_overTaskPic =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/" + MqttMessageHandler.FileName(
                                action.touchScreenConfig!!.argPicGroup!!.overTaskPic!!
                            )
                        Thread {
                            DownloadBill.getInstance().addTask(
                                action.touchScreenConfig!!.argPicGroup!!.overTaskPic!!,
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/group/",
                                MqttMessageHandler.FileName(action.touchScreenConfig!!.argPicGroup!!.overTaskPic!!),
                                MyApplication.listener
                            )
                        }.start()
                    } else {
                        tableTouchScreen.touch_overTaskPic =
                            Universal.gifDefault
                    }
                }
                tableTouchScreen.save()
                shoppingActionDB.touchScreenConfig = tableTouchScreen
            }
            if (shoppingActionDB.save()) {
                RobotStatus.newUpdata.postValue(2)
            }
        }

    }


    fun ExplainType(message: String) {
        val gson = Gson()
        val routeConfig = gson.fromJson(message, RouteConfig::class.java)
        RobotStatus.routeConfig?.value = routeConfig
        //所有图片存储的总路径
        LogUtil.d("收到讲解路线配置")
        val tableRoute = Table_Route()
        //存储图片的数据库
        val routeList = routeConfig.routeList// 路线列表对象
        val isExist =
            LitePal.where("routename = ?", routeList!![0].routeName)
                .count(Table_Route::class.java) > 0
//                    if (isExist) {
//                        for (route in 0 until routeList.size) {
        if (QuerySql.queryTime(routeList[0].routeName) != routeList[0].timeStamp && isExist) {
            //删除对应文件夹
            if (routeList[0].timeStamp <= 0) {
                UpdateReturn().deleteDirectory((File(Universal.robotFile + routeList[0].rootMapName + "/" + routeList[0].routeName)))
//                            deleteFolderFile(
//                                (),
//                                true
//                            )
            }
            val routeDBID = QuerySql.routeDB_id(routeList[0].routeName)
            val pointConfigVoDB_id = QuerySql.pointConfigVoDB_id(routeList[0].routeName)

            val deleteTouchScreen = LitePal.deleteAll(
                Table_Touch_Screen::class.java,
                "table_point_config_id = ?",
                pointConfigVoDB_id.toString()
            )
            val deleteBigScreen = LitePal.deleteAll(
                Table_Big_Screen::class.java,
                "table_point_config_id = ?",
                pointConfigVoDB_id.toString()
            )
            val deletePointConfig = LitePal.deleteAll(
                Table_Point_Config::class.java,
                "table_route_id = ?",
                routeDBID.toString()
            )
            val deleteTableRoute =
                LitePal.deleteAll(Table_Route::class.java, "routename = ?", routeList[0].routeName)
            if (deleteTouchScreen > 0) {
                Log.e("TAG", "TouchScreenConfigDB数据删除成功")
            }
            if (deleteBigScreen > 0) {
                Log.e("TAG", "BigScreenConfigDB数据删除成功")
            }
            if (deletePointConfig > 0) {
                Log.e("TAG", "PointConfigVODB数据删除成功")
            }
            if (deleteTableRoute > 0) {
                Log.e("TAG", "RouteDB数据删除成功")
            }
        }
//                        }
//                    }
        val iterator = routeList.iterator()
        while (iterator.hasNext()) {
            val route = iterator.next()
            if (route.timeStamp > 0) {
                //创建对应文件夹。以路线名字命名(存放主屏幕)
                MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
//                            openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + "group/")
                MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/")
                val pointIterator = route.pointConfigVOList.iterator()
                //路线名字
                tableRoute.routeName = route.routeName
                //总图名字
                tableRoute.rootMapName = route.rootMapName
                //简介
                tableRoute.introduction = route.introduction
                //配置时间戳
                tableRoute.timeStamp = route.timeStamp
                //路线背景图
                if (route.backgroundPic?.isNotEmpty() == true) {
                    MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/")
                    MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                    val backPic = MqttMessageHandler.compareArrays(
                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch",
                        route.backgroundPic
                    )
                    tableRoute.backgroundPic =
                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + route.backgroundPic.substring(
                            route.backgroundPic.lastIndexOf("/") + 1
                        ) //路线背景图
                    if (backPic?.isNotEmpty() == true) {
                        Thread {
                            for (i in backPic.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + backPic[i],
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch",
                                    MqttMessageHandler.FileName(backPic[i]!!),
                                    MyApplication.listener
                                )
                            }
                        }.start()
                    }

                }
                var pointItem: List<Table_Point_Config>
                while (pointIterator.hasNext()) {
                    val point = pointIterator.next()
                    pointItem = ArrayList()
                    val tablePointConfig =
                        Table_Point_Config()
                    //点名
                    tablePointConfig.name = point.name
                    //途径播报内容-播报语(200)
                    tablePointConfig.walkText = point.walkText
                    //讲解播报内容-播报语(200)
                    tablePointConfig.explanationText = point.explanation
                    //排序
                    tablePointConfig.scope = point.scope!!
                    //途径音频.mp3
                    if (point.walkVoice?.isNotEmpty() == true) {
                        tablePointConfig.walkVoice =
                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/" + (point.walkVoice).substring(
                                (point.walkVoice).lastIndexOf("/") + 1
                            )
                        val walkMp3 = MqttMessageHandler.compareArrays(
                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/",
                            point.walkVoice
                        )

                        if (walkMp3?.isNotEmpty() == true) {
                            Thread {
                                for (i in walkMp3.indices) {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + walkMp3[i],
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                        MqttMessageHandler.FileName(walkMp3[i]!!),
                                        MyApplication.listener
                                    )
                                }
                            }.start()
                        }
                    }
                    //到达讲解.mp3
                    if (point.explanationVoice?.isNotEmpty() == true) {
                        tablePointConfig.explanationVoice =
                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/" + (point.explanationVoice).substring(
                                (point.explanationVoice).lastIndexOf("/") + 1
                            )


                        val explanationMp3 =
                            MqttMessageHandler.compareArrays(
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/",
                                point.explanationVoice
                            )
                        if (explanationMp3?.isNotEmpty() == true) {
                            Thread {
                                for (i in explanationMp3.indices) {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + explanationMp3[i],
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                        MqttMessageHandler.FileName(explanationMp3[i]!!),
                                        MyApplication.listener
                                    )
                                }
                            }.start()
                        }
                    }
                    //大屏配置
                    if (point.bigScreenConfig!!.screen == 1) {
                        val tableBigScreen =
                            Table_Big_Screen()
                        //配置类型
                        tableBigScreen.type =
                            point.bigScreenConfig.type!!
                        if (point.bigScreenConfig.argPic != null) {
                            //图片布局
                            tableBigScreen.picType =
                                point.bigScreenConfig.argPic.picType
                            //轮播时间
                            tableBigScreen.picPlayTime =
                                point.bigScreenConfig.argPic.picPlayTime
                            //图片
                            MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                            val bigPic = MqttMessageHandler.compareArrays(
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                point.bigScreenConfig.argPic.pics
                            )

                            //创建对应文件夹。以路线名字命名(存放大屏幕)
                            tableBigScreen.imageFile =
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name
//                                            val bigPicFile =
//                                                UpdateReturn().splitStr(point.bigScreenConfig.argPic.pics)
                            if (bigPic?.isNotEmpty() == true) {
                                Thread {
                                    for (i in bigPic.indices) {
                                        DownloadBill.getInstance().addTask(
                                            Universal.pathDownload + bigPic[i],
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                            MqttMessageHandler.FileName(bigPic[i]!!),
                                            MyApplication.listener
                                        )
                                    }
                                }.start()
                            }
                        }
                        if (point.bigScreenConfig.argFont != null) {
                            //文字
                            tableBigScreen.fontContent =
                                point.bigScreenConfig.argFont.fontContent
                            //文字颜色
                            tableBigScreen.fontColor =
                                point.bigScreenConfig.argFont.fontColor
                            //文字大小 1-大，2-中，3-小,
                            tableBigScreen.fontSize =
                                point.bigScreenConfig.argFont.fontSize
                            //文字方向 1-横向，2-纵向
                            tableBigScreen.fontLayout =
                                point.bigScreenConfig.argFont.fontLayout
                            //背景颜色
                            tableBigScreen.fontBackGround =
                                point.bigScreenConfig.argFont.fontBackGround
                            //文字显示位置  0-居中 1-居上 2-居下
                            tableBigScreen.textPosition =
                                point.bigScreenConfig.argFont.textPosition
                        }
                        if (point.bigScreenConfig.argVideo != null) {
                            //视频是否播放声音
                            tableBigScreen.videoAudio =
                                point.bigScreenConfig.argVideo.videoAudio!!
                            tableBigScreen.videolayout =
                                point.bigScreenConfig.argVideo.videoLayOut!!
                            //视频储存位置
                            //创建对应文件夹。以路线名字命名(存放大屏幕)
                            MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                            val argVideoName =
                                MqttMessageHandler.compareArrays(
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                    point.bigScreenConfig.argVideo.videos
                                )
                            tableBigScreen.videoFile =
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name
                            if (argVideoName?.isNotEmpty() == true) {
                                Thread {
                                    for (i in argVideoName.indices) {
                                        DownloadBill.getInstance().addTask(
                                            Universal.pathDownload + argVideoName[i],
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                            MqttMessageHandler.FileName(argVideoName[i]!!),
                                            MyApplication.listener
                                        )
                                    }
                                }.start()
                            }
                        }
                        tableBigScreen.save()
                        tablePointConfig.bigScreenConfigDB = tableBigScreen
                    }
                    //小屏
                    if (point.touchScreenConfig!!.screen == 0) {
                        //创建对应文件夹。以路线名字命名(存放主屏幕)
                        MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name + "/")
                        val tableTouchScreen =
                            Table_Touch_Screen()
                        //配置类型
                        tableTouchScreen.touch_type =
                            point.touchScreenConfig.type!!
                        if (point.touchScreenConfig.argPic != null) {
                            //图片布局
                            tableTouchScreen.touch_picType =
                                point.touchScreenConfig.argPic.picType
                            //轮播时间
                            tableTouchScreen.touch_picPlayTime =
                                point.touchScreenConfig.argPic.picPlayTime
                            //图片路径
                            if (point.touchScreenConfig.argPic.pics.isNotEmpty()) {
                                val touchFileName =
                                    MqttMessageHandler.compareArrays(
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name,
                                        point.touchScreenConfig.argPic.pics
                                    )

                                tableTouchScreen.touch_imageFile =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name + "/"

                                if (touchFileName?.isNotEmpty() == true) {
                                    Thread {
                                        for (i in touchFileName.indices) {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + touchFileName[i],
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name,
                                                MqttMessageHandler.FileName(touchFileName[i]!!),
                                                MyApplication.listener
                                            )
                                        }
                                    }.start()
                                }
                            }
                        }
                        if (point.touchScreenConfig.argFont != null) {
                            //文字
                            tableTouchScreen.touch_fontContent =
                                point.touchScreenConfig.argFont.fontContent
                            //文字颜色
                            tableTouchScreen.touch_fontColor =
                                point.touchScreenConfig.argFont.fontColor
                            //文字大小 1-大，2-中，3-小,
                            tableTouchScreen.touch_fontSize =
                                point.touchScreenConfig.argFont.fontSize
                            //文字方向 1-横向，2-纵向
                            tableTouchScreen.touch_fontLayout =
                                point.touchScreenConfig.argFont.fontLayout
                            //背景颜色
                            tableTouchScreen.touch_fontBackGround =
                                point.touchScreenConfig.argFont.fontBackGround
                            //文字显示位置  0-居中 1-居上 2-居下
                            tableTouchScreen.touch_textPosition =
                                point.touchScreenConfig.argFont.textPosition
                        }
                        if (point.touchScreenConfig.argPicGroup != null) {
                            //行走中
                            if (point.touchScreenConfig.argPicGroup.walkPic != "") {
                                tableTouchScreen.touch_walkPic =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + MqttMessageHandler.FileName(
                                        point.touchScreenConfig.argPicGroup.walkPic!!
                                    )
                                Thread {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + point.touchScreenConfig.argPicGroup.walkPic,
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                        MqttMessageHandler.FileName(point.touchScreenConfig.argPicGroup.walkPic),
                                        MyApplication.listener
                                    )
                                }.start()
                            } else {
                                tableTouchScreen.touch_walkPic = Universal.gifDefault
                            }
                            //被阻挡
                            if (point.touchScreenConfig.argPicGroup.blockPic != "") {
                                tableTouchScreen.touch_blockPic =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + MqttMessageHandler.FileName(
                                        point.touchScreenConfig.argPicGroup.blockPic!!
                                    )
                                Thread {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + point.touchScreenConfig.argPicGroup.blockPic,
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                        MqttMessageHandler.FileName(point.touchScreenConfig.argPicGroup.blockPic),
                                        MyApplication.listener
                                    )
                                }.start()
                            } else {
                                tableTouchScreen.touch_blockPic =
                                    Universal.gifDefault
                            }
                            //到点
                            if (point.touchScreenConfig.argPicGroup.arrivePic != "") {
                                tableTouchScreen.touch_arrivePic =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + MqttMessageHandler.FileName(
                                        point.touchScreenConfig.argPicGroup.arrivePic!!
                                    )
                                Thread {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + point.touchScreenConfig.argPicGroup.arrivePic,
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                        MqttMessageHandler.FileName(point.touchScreenConfig.argPicGroup.arrivePic),
                                        MyApplication.listener
                                    )
                                }.start()
                            } else {
                                tableTouchScreen.touch_arrivePic =
                                    Universal.gifDefault
                            }
                            //返回
                            if (point.touchScreenConfig.argPicGroup.overTaskPic != "") {
                                tableTouchScreen.touch_overTaskPic =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/" + MqttMessageHandler.FileName(
                                        point.touchScreenConfig.argPicGroup.overTaskPic!!
                                    )
                                Thread {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + point.touchScreenConfig.argPicGroup.overTaskPic,
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/group/",
                                        MqttMessageHandler.FileName(point.touchScreenConfig.argPicGroup.overTaskPic),
                                        MyApplication.listener
                                    )
                                }.start()
                            } else {
                                tableTouchScreen.touch_overTaskPic =
                                    Universal.gifDefault
                            }
                        }
                        tableTouchScreen.save()
                        tablePointConfig.touchScreenConfigDB = tableTouchScreen
                    }
                    tablePointConfig.save()
                    pointItem.add(tablePointConfig)
                    tableRoute.mapPointName = pointItem
                    if (tableRoute.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 讲解点数据保存成功")
                        RobotStatus.newUpdata.postValue(2)
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 讲解点数据保存失败")
                    }
                }
            }
        }
    }

    fun guidePointConfig(message: String) {
        val gson = Gson()
        var guidPointTime: LinkedHashMap<String, Long>
        var guideConfigDB: GuideConfig
        val guideConfig = gson.fromJson(message, GuidePointList::class.java)
        RobotStatus.guidePointList?.value = guideConfig
        Log.d("TAG", "收到引领配置")
        val mapsList = guideConfig.maps
        val mapIterator = mapsList?.iterator()

        while (mapIterator!!.hasNext()) {
            guideConfigDB = GuideConfig()
            val maps = mapIterator.next()


            var pointList: List<Table_Guide_Point_Pic>
            val pointIterator = maps?.pointList!!.iterator()
            while (pointIterator.hasNext()) {
                val points = pointIterator.next()
                pointList = ArrayList()
                val guidePointPicDB = Table_Guide_Point_Pic()

                //查询时间戳是否相同
                guidPointTime = LinkedHashMap()
                val guideList = QuerySql.selectGuideList(maps.mapName)
                if (guideList.isEmpty()) {
                    // 处理空结果集的情况，例如记录日志、显示错误消息或者返回
                    Log.e("TAG", "查询结果为空")

                } else {
                    for (timeHash in guideList) {
                        Log.d("", "ActionShoppingType: HashMap添加数据")
                        guidPointTime[timeHash.pointName!!] = timeHash.pointTimeStamp!!
                    }
                    val timestamp = guidPointTime[points!!.pointName]
                    if (timestamp != null && points.pointTimeStamp != timestamp) {
                        Log.e("TAG", "ActionShoppingType: 查询到时间戳更改")
                        // 删除map中的数据
                        guidPointTime.remove(points.pointName)
                        //删除对应的图片文件夹
                        MqttMessageHandler.deleteFiles(File(Universal.robotFile + "GuidePic/" + points.pointName + "/"))
                        //删除数据库中的数据
                        DeleteSql.deleteGuidePointConfig(
                            points.pointName,
                            maps.mapName
                        )
                    } else if (points.pointTimeStamp == timestamp) {
                        val values = ContentValues()
                        values.put("maptimestamp", maps.mapTimeStamp)
                        updateAll(
                            Table_Guide_Point_Pic::class.java,
                            values,
                            "pointname = ? and mapname = ?",
                            points.pointName,
                            maps.mapName
                        )
                        RobotStatus.newUpdata.postValue(2)
                        continue
                    } else if (points.pointTimeStamp!! <= 0) {
                        Log.e("TAG", "ActionShoppingType: 引领数据删除")
                        // 删除map中的数据
                        guidPointTime.remove(points.pointName)
                        //删除对应的图片文件夹
                        MqttMessageHandler.deleteFiles(File(Universal.robotFile + "GuidePic/" + points.pointName + "/"))
                        //删除数据库中的数据
                        DeleteSql.deleteGuidePointConfig(
                            points.pointName,
                            maps.mapName
                        )
                        continue
                    }
                }

                //创建文件夹
                MqttMessageHandler.openFile(Universal.robotFile + "GuidePic/" + points!!.pointName + "/")
                guidePointPicDB.pointName = points.pointName
                guidePointPicDB.guidePicUrl =
                    Universal.robotFile + "GuidePic/" + points.pointName + "/" + "test.${
                        fileName(points.guidePicUrl!!)
                    }"
                guidePointPicDB.pointTimeStamp = points.pointTimeStamp
                guidePointPicDB.mapTimeStamp = maps.mapTimeStamp
                guidePointPicDB.mapName = maps.mapName
                pointList.add(guidePointPicDB)
                thread {
                    DownloadBill.getInstance().addTask(
                        Universal.pathDownload + points.guidePicUrl,
                        Universal.robotFile + "GuidePic/" + points.pointName + "/",
                        "test.${fileName(points.guidePicUrl!!)}",
                        MyApplication.listener
                    )
                }
                guideConfigDB.pointList = pointList

                if (guidePointPicDB.save()) {
                    RobotStatus.newUpdata.postValue(2)
                }
            }
        }
    }

    fun guideFoundation(message: String) {
        val gson = Gson()
        val guideFoundation = gson.fromJson(message, guideFoundationModel::class.java)
        RobotStatus.guideFoundationConfig?.value = guideFoundation

        LitePal.deleteAll(Table_Guide_Foundation::class.java)
        MqttMessageHandler.deleteFiles(File(Universal.robotFile + "GuidePic/foundation"))

        val guideFoundationConfigDB = Table_Guide_Foundation()
        guideFoundationConfigDB.arrivePrompt = guideFoundation.arrivePrompt
        guideFoundationConfigDB.movePrompt = guideFoundation.movePrompt
        guideFoundationConfigDB.firstPrompt = guideFoundation.firstPrompt
        guideFoundationConfigDB.interruptPrompt = guideFoundation.interruptPrompt
        guideFoundationConfigDB.timeStamp = guideFoundation.timeStamp

        //大屏幕
        if (guideFoundation.bigScreenConfig!!.screen == 1) {
            val tableBigScreen =
                Table_Big_Screen()
            //配置类型
            tableBigScreen.type =
                guideFoundation.bigScreenConfig!!.type!!
            if (guideFoundation.bigScreenConfig!!.argPic != null) {
                //图片布局
                tableBigScreen.picType =
                    guideFoundation.bigScreenConfig!!.argPic?.picType!!
                //轮播时间
                tableBigScreen.picPlayTime =
                    guideFoundation.bigScreenConfig!!.argPic!!.picPlayTime
                //图片
                MqttMessageHandler.openFile(Universal.robotFile + "GuidePic/foundation/big/")

                val picfile = MqttMessageHandler.compareArrays(
                    Universal.robotFile + "GuidePic/foundation/big/",
                    guideFoundation.bigScreenConfig!!.argPic!!.pics
                )
                //创建对应文件夹。以路线名字命名(存放大屏幕)
                tableBigScreen.imageFile =
                    Universal.robotFile + "GuidePic/foundation/big/"
                Thread {
                    for (i in picfile!!.indices) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + picfile[i],
                            Universal.robotFile + "GuidePic/foundation/big/",
                            MqttMessageHandler.FileName(picfile[i]!!),
                            MyApplication.listener
                        )
                    }
                }.start()
            }
            if (guideFoundation.bigScreenConfig!!.argFont != null) {
                //文字
                tableBigScreen.fontContent =
                    guideFoundation.bigScreenConfig!!.argFont?.fontContent
                //文字颜色
                tableBigScreen.fontColor =
                    guideFoundation.bigScreenConfig!!.argFont?.fontColor
                //文字大小 1-大，2-中，3-小,
                tableBigScreen.fontSize =
                    guideFoundation.bigScreenConfig!!.argFont?.fontSize!!
                //文字方向 1-横向，2-纵向
                tableBigScreen.fontLayout =
                    guideFoundation.bigScreenConfig!!.argFont?.fontLayout!!
                //背景颜色
                tableBigScreen.fontBackGround =
                    guideFoundation.bigScreenConfig!!.argFont?.fontBackGround
                //文字显示位置  0-居中 1-居上 2-居下
                tableBigScreen.textPosition =
                    guideFoundation.bigScreenConfig!!.argFont?.textPosition!!
            }
            if (guideFoundation.bigScreenConfig!!.argVideo != null) {
                //视频是否播放声音
                tableBigScreen.videoAudio =
                    guideFoundation.bigScreenConfig!!.argVideo?.videoAudio!!
                tableBigScreen.videolayout =
                    guideFoundation.bigScreenConfig!!.argVideo?.videoLayOut!!
                //视频储存位置
                //创建对应文件夹。以路线名字命名(存放大屏幕)
                MqttMessageHandler.openFile(Universal.robotFile + "GuidePic/foundation/big/")

                val picfile = MqttMessageHandler.compareArrays(
                    Universal.robotFile + "GuidePic/foundation/big/",
                    guideFoundation.bigScreenConfig!!.argVideo?.videos!!
                )
                tableBigScreen.videoFile =
                    Universal.robotFile + "GuidePic/foundation/big/"
                Thread {
                    for (i in picfile!!.indices) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + picfile[i],
                            Universal.robotFile + "GuidePic/foundation/big/",
                            MqttMessageHandler.FileName(picfile[i]!!),
                            MyApplication.listener
                        )
                    }
                }.start()
            }
            tableBigScreen.save()
            guideFoundationConfigDB.bigScreenConfig = tableBigScreen
        }
        if (guideFoundation.touchScreenConfig!!.screen == 0) {
            //创建对应文件夹。以路线名字命名(存放主屏幕)
            MqttMessageHandler.openFile(Universal.robotFile + "GuidePic/foundation/touch/")
            val tableTouchScreen =
                Table_Touch_Screen()
            //配置类型
            tableTouchScreen.touch_type =
                guideFoundation.touchScreenConfig!!.type!!
            if (guideFoundation.touchScreenConfig!!.argPic != null) {
                //图片布局
                tableTouchScreen.touch_picType =
                    guideFoundation.touchScreenConfig!!.argPic?.picType!!
                //轮播时间
                tableTouchScreen.touch_picPlayTime =
                    guideFoundation.touchScreenConfig!!.argPic?.picPlayTime!!
                //图片路径
                if (guideFoundation.touchScreenConfig!!.argPic?.pics!!.isNotEmpty()) {
                    val picfile = MqttMessageHandler.compareArrays(
                        Universal.robotFile + "GuidePic/foundation/touch/",
                        guideFoundation.touchScreenConfig!!.argPic?.pics!!
                    )
                    tableTouchScreen.touch_imageFile =
                        Universal.robotFile + "GuidePic/foundation/touch/"
                    Thread {
                        for (i in picfile!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + picfile[i],
                                Universal.robotFile + "GuidePic/foundation/touch/",
                                MqttMessageHandler.FileName(picfile[i]!!),
                                MyApplication.listener
                            )
                        }
                    }.start()
                }
            }
            if (guideFoundation.touchScreenConfig!!.argFont != null) {
                //文字
                tableTouchScreen.touch_fontContent =
                    guideFoundation.touchScreenConfig!!.argFont!!.fontContent
                //文字颜色
                tableTouchScreen.touch_fontColor =
                    guideFoundation.touchScreenConfig!!.argFont!!.fontColor
                //文字大小 1-大，2-中，3-小,
                tableTouchScreen.touch_fontSize =
                    guideFoundation.touchScreenConfig!!.argFont!!.fontSize
                //文字方向 1-横向，2-纵向
                tableTouchScreen.touch_fontLayout =
                    guideFoundation.touchScreenConfig!!.argFont!!.fontLayout
                //背景颜色
                tableTouchScreen.touch_fontBackGround =
                    guideFoundation.touchScreenConfig!!.argFont!!.fontBackGround
                //文字显示位置  0-居中 1-居上 2-居下
                tableTouchScreen.touch_textPosition =
                    guideFoundation.touchScreenConfig!!.argFont!!.textPosition
            }
            if (guideFoundation.touchScreenConfig!!.argPicGroup != null) {
                //行走中
                if (guideFoundation.touchScreenConfig!!.argPicGroup!!.walkPic != "") {
                    tableTouchScreen.touch_walkPic =
                        Universal.robotFile + "GuidePic/foundation/group/" + MqttMessageHandler.FileName(
                            guideFoundation.touchScreenConfig!!.argPicGroup!!.walkPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + guideFoundation.touchScreenConfig!!.argPicGroup!!.walkPic,
                            Universal.robotFile + "GuidePic/foundation/group/",
                            MqttMessageHandler.FileName(guideFoundation.touchScreenConfig!!.argPicGroup!!.walkPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_walkPic = Universal.gifDefault
                }
                //被阻挡
                if (guideFoundation.touchScreenConfig!!.argPicGroup!!.blockPic != "") {
                    tableTouchScreen.touch_blockPic =
                        Universal.robotFile + "GuidePic/foundation/group/" + MqttMessageHandler.FileName(
                            guideFoundation.touchScreenConfig!!.argPicGroup!!.blockPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + guideFoundation.touchScreenConfig!!.argPicGroup!!.blockPic!!,
                            Universal.robotFile + "GuidePic/foundation/group/",
                            MqttMessageHandler.FileName(guideFoundation.touchScreenConfig!!.argPicGroup!!.blockPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_blockPic =
                        Universal.gifDefault
                }
                //到点
                if (guideFoundation.touchScreenConfig!!.argPicGroup!!.arrivePic != "") {
                    tableTouchScreen.touch_arrivePic =
                        Universal.robotFile + "GuidePic/foundation/group/" + MqttMessageHandler.FileName(
                            guideFoundation.touchScreenConfig!!.argPicGroup!!.arrivePic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + guideFoundation.touchScreenConfig!!.argPicGroup!!.arrivePic!!,
                            Universal.robotFile + "GuidePic/foundation/group/",
                            MqttMessageHandler.FileName(guideFoundation.touchScreenConfig!!.argPicGroup!!.arrivePic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_arrivePic =
                        Universal.gifDefault
                }
                //返回
                if (guideFoundation.touchScreenConfig!!.argPicGroup!!.overTaskPic != "") {
                    tableTouchScreen.touch_overTaskPic =
                        Universal.robotFile + "GuidePic/foundation/group/" + MqttMessageHandler.FileName(
                            guideFoundation.touchScreenConfig!!.argPicGroup!!.overTaskPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            guideFoundation.touchScreenConfig!!.argPicGroup!!.overTaskPic!!,
                            Universal.robotFile + "GuidePic/foundation/group/",
                            MqttMessageHandler.FileName(guideFoundation.touchScreenConfig!!.argPicGroup!!.overTaskPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_overTaskPic =
                        Universal.gifDefault
                }
            }
            tableTouchScreen.save()
            guideFoundationConfigDB.touchScreenConfig = tableTouchScreen
        }
        if (guideFoundationConfigDB.save()) {
            RobotStatus.newUpdata.postValue(2)
        }
    }

    fun replyGreet(message: String) {
        val gson = Gson()
        val replyGreetGson = gson.fromJson(message, ReplyGreetConfigModel::class.java)
        RobotStatus.replyGreet?.value = replyGreetGson

        LitePal.deleteAll(Table_Greet_Config::class.java)

        val replyTableGreetConfig =
            Table_Greet_Config()
        replyTableGreetConfig.greetPoint = replyGreetGson.greetPoint
        replyTableGreetConfig.firstPrompt = replyGreetGson.firstPrompt
        replyTableGreetConfig.strangerPrompt = replyGreetGson.strangerPrompt
        replyTableGreetConfig.vipPrompt = replyGreetGson.vipPrompt
        replyTableGreetConfig.exitPrompt = replyGreetGson.exitPrompt
        replyTableGreetConfig.timeStamp = replyGreetGson.timeStamp
        //大屏幕配置
        if (replyGreetGson.bigScreenConfig!!.screen == 1) {
            val tableBigScreen =
                Table_Big_Screen()
            //配置类型
            tableBigScreen.type =
                replyGreetGson.bigScreenConfig!!.type!!
            if (replyGreetGson.bigScreenConfig!!.argPic != null) {
                //图片布局
                tableBigScreen.picType =
                    replyGreetGson.bigScreenConfig!!.argPic?.picType!!
                //轮播时间
                tableBigScreen.picPlayTime =
                    replyGreetGson.bigScreenConfig!!.argPic!!.picPlayTime
                //图片
                MqttMessageHandler.openFile(Universal.robotFile + "replyGreet/big/")

                val picfile = MqttMessageHandler.compareArrays(
                    Universal.robotFile + "replyGreet/big/",
                    replyGreetGson.bigScreenConfig!!.argPic!!.pics
                )
                //创建对应文件夹。以路线名字命名(存放大屏幕)
                tableBigScreen.imageFile =
                    Universal.robotFile + "replyGreet/big/"
                Thread {
                    for (i in picfile!!.indices) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + picfile[i],
                            Universal.robotFile + "replyGreet/big/",
                            MqttMessageHandler.FileName(picfile[i]!!),
                            MyApplication.listener
                        )
                    }
                }.start()
            }
            if (replyGreetGson.bigScreenConfig!!.argFont != null) {
                //文字
                tableBigScreen.fontContent =
                    replyGreetGson.bigScreenConfig!!.argFont?.fontContent
                //文字颜色
                tableBigScreen.fontColor =
                    replyGreetGson.bigScreenConfig!!.argFont?.fontColor
                //文字大小 1-大，2-中，3-小,
                tableBigScreen.fontSize =
                    replyGreetGson.bigScreenConfig!!.argFont?.fontSize!!
                //文字方向 1-横向，2-纵向
                tableBigScreen.fontLayout =
                    replyGreetGson.bigScreenConfig!!.argFont?.fontLayout!!
                //背景颜色
                tableBigScreen.fontBackGround =
                    replyGreetGson.bigScreenConfig!!.argFont?.fontBackGround
                //文字显示位置  0-居中 1-居上 2-居下
                tableBigScreen.textPosition =
                    replyGreetGson.bigScreenConfig!!.argFont?.textPosition!!
            }
            if (replyGreetGson.bigScreenConfig!!.argVideo != null) {
                //视频是否播放声音
                tableBigScreen.videoAudio =
                    replyGreetGson.bigScreenConfig!!.argVideo?.videoAudio!!
                tableBigScreen.videolayout =
                    replyGreetGson.bigScreenConfig!!.argVideo?.videoLayOut!!
                //视频储存位置
                //创建对应文件夹。以路线名字命名(存放大屏幕)
                MqttMessageHandler.openFile(Universal.robotFile + "replyGreet/big/")

                val picfile = MqttMessageHandler.compareArrays(
                    Universal.robotFile + "replyGreet/big/",
                    replyGreetGson.bigScreenConfig!!.argVideo?.videos!!
                )
                tableBigScreen.videoFile =
                    Universal.robotFile + "replyGreet/big/"
                Thread {
                    for (i in picfile!!.indices) {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + picfile[i],
                            Universal.robotFile + "replyGreet/big/",
                            MqttMessageHandler.FileName(picfile[i]!!),
                            MyApplication.listener
                        )
                    }
                }.start()
            }
            tableBigScreen.save()
            replyTableGreetConfig.bigScreenConfig = tableBigScreen
        }
        //小屏幕配置
        if (replyGreetGson.touchScreenConfig!!.screen == 0) {
            //创建对应文件夹。以路线名字命名(存放主屏幕)
            MqttMessageHandler.openFile(Universal.robotFile + "replyGreet/touch/")
            val tableTouchScreen =
                Table_Touch_Screen()
            //配置类型
            tableTouchScreen.touch_type =
                replyGreetGson.touchScreenConfig!!.type!!
            if (replyGreetGson.touchScreenConfig!!.argPic != null) {
                //图片布局
                tableTouchScreen.touch_picType =
                    replyGreetGson.touchScreenConfig!!.argPic?.picType!!
                //轮播时间
                tableTouchScreen.touch_picPlayTime =
                    replyGreetGson.touchScreenConfig!!.argPic?.picPlayTime!!
                //图片路径
                if (replyGreetGson.touchScreenConfig!!.argPic?.pics!!.isNotEmpty()) {
                    val picfile = MqttMessageHandler.compareArrays(
                        Universal.robotFile + "replyGreet/touch/",
                        replyGreetGson.touchScreenConfig!!.argPic?.pics!!
                    )
                    tableTouchScreen.touch_imageFile =
                        Universal.robotFile + "replyGreet/touch/"
                    Thread {
                        for (i in picfile!!.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + picfile[i],
                                Universal.robotFile + "replyGreet/touch/",
                                MqttMessageHandler.FileName(picfile[i]!!),
                                MyApplication.listener
                            )
                        }
                    }.start()
                }
            }
            if (replyGreetGson.touchScreenConfig!!.argFont != null) {
                //文字
                tableTouchScreen.touch_fontContent =
                    replyGreetGson.touchScreenConfig!!.argFont!!.fontContent
                //文字颜色
                tableTouchScreen.touch_fontColor =
                    replyGreetGson.touchScreenConfig!!.argFont!!.fontColor
                //文字大小 1-大，2-中，3-小,
                tableTouchScreen.touch_fontSize =
                    replyGreetGson.touchScreenConfig!!.argFont!!.fontSize
                //文字方向 1-横向，2-纵向
                tableTouchScreen.touch_fontLayout =
                    replyGreetGson.touchScreenConfig!!.argFont!!.fontLayout
                //背景颜色
                tableTouchScreen.touch_fontBackGround =
                    replyGreetGson.touchScreenConfig!!.argFont!!.fontBackGround
                //文字显示位置  0-居中 1-居上 2-居下
                tableTouchScreen.touch_textPosition =
                    replyGreetGson.touchScreenConfig!!.argFont!!.textPosition
            }
            if (replyGreetGson.touchScreenConfig!!.argPicGroup != null) {
                //行走中
                if (replyGreetGson.touchScreenConfig!!.argPicGroup!!.walkPic != "") {
                    tableTouchScreen.touch_walkPic =
                        Universal.robotFile + "replyGreet/group/" + MqttMessageHandler.FileName(
                            replyGreetGson.touchScreenConfig!!.argPicGroup!!.walkPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + replyGreetGson.touchScreenConfig!!.argPicGroup!!.walkPic,
                            Universal.robotFile + "replyGreet/group/",
                            MqttMessageHandler.FileName(replyGreetGson.touchScreenConfig!!.argPicGroup!!.walkPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_walkPic = Universal.gifDefault
                }
                //被阻挡
                if (replyGreetGson.touchScreenConfig!!.argPicGroup!!.blockPic != "") {
                    tableTouchScreen.touch_blockPic =
                        Universal.robotFile + "replyGreet/group/" + MqttMessageHandler.FileName(
                            replyGreetGson.touchScreenConfig!!.argPicGroup!!.blockPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + replyGreetGson.touchScreenConfig!!.argPicGroup!!.blockPic!!,
                            Universal.robotFile + "replyGreet/group/",
                            MqttMessageHandler.FileName(replyGreetGson.touchScreenConfig!!.argPicGroup!!.blockPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_blockPic =
                        Universal.gifDefault
                }
                //到点
                if (replyGreetGson.touchScreenConfig!!.argPicGroup!!.arrivePic != "") {
                    tableTouchScreen.touch_arrivePic =
                        Universal.robotFile + "replyGreet/group/" + MqttMessageHandler.FileName(
                            replyGreetGson.touchScreenConfig!!.argPicGroup!!.arrivePic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            Universal.pathDownload + replyGreetGson.touchScreenConfig!!.argPicGroup!!.arrivePic!!,
                            Universal.robotFile + "replyGreet/group/",
                            MqttMessageHandler.FileName(replyGreetGson.touchScreenConfig!!.argPicGroup!!.arrivePic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_arrivePic =
                        Universal.gifDefault
                }
                //返回
                if (replyGreetGson.touchScreenConfig!!.argPicGroup!!.overTaskPic != "") {
                    tableTouchScreen.touch_overTaskPic =
                        Universal.robotFile + "replyGreet/group/" + MqttMessageHandler.FileName(
                            replyGreetGson.touchScreenConfig!!.argPicGroup!!.overTaskPic!!
                        )
                    Thread {
                        DownloadBill.getInstance().addTask(
                            replyGreetGson.touchScreenConfig!!.argPicGroup!!.overTaskPic!!,
                            Universal.robotFile + "replyGreet/group/",
                            MqttMessageHandler.FileName(replyGreetGson.touchScreenConfig!!.argPicGroup!!.overTaskPic!!),
                            MyApplication.listener
                        )
                    }.start()
                } else {
                    tableTouchScreen.touch_overTaskPic =
                        Universal.gifDefault
                }
            }
            tableTouchScreen.save()
            replyTableGreetConfig.touchScreenConfig = tableTouchScreen
        }
        if (replyGreetGson.faceFeats!!.isNotEmpty()) {
            val faceFeatsList = replyGreetGson.faceFeats // 人脸信息
            val faceIterator = faceFeatsList?.iterator()
            val faceFeats = ArrayList<Table_Face>() // 在循环外部初始化列表
            // 迭代器开始遍历
            while (faceIterator!!.hasNext()) {
                val faceFeatsNext = faceIterator.next()
                val tableFace = Table_Face()
                tableFace.face_id = faceFeatsNext!!.face_id
                tableFace.name = faceFeatsNext.name
                tableFace.sexual = faceFeatsNext.sexual
                tableFace.save()
                faceFeats.add(tableFace) // 向列表中添加对象
            }

            replyTableGreetConfig.faceFeats = faceFeats // 设置所有对象的列表
        }

        // 如果输出存储成功
        if (replyTableGreetConfig.save()) {
            RobotStatus.newUpdata.postValue(2)
        }
    }

    fun fileName(url: String): String {
        return url.substring(url.lastIndexOf(".") + 1)
    }
}