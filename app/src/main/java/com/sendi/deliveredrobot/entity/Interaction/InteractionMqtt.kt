package com.sendi.deliveredrobot.entity.Interaction

import android.content.ContentValues
import android.util.Log
import com.google.gson.Gson
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.entity.BigScreenConfigDB
import com.sendi.deliveredrobot.entity.PointConfigVODB
import com.sendi.deliveredrobot.entity.RouteDB
import com.sendi.deliveredrobot.entity.ShoppingActionDB
import com.sendi.deliveredrobot.entity.ShoppingConfigDB
import com.sendi.deliveredrobot.entity.TouchScreenConfigDB
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.UpDataSQL
import com.sendi.deliveredrobot.entity.entitySql.DeleteSql
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.handler.MqttMessageHandler
import com.sendi.deliveredrobot.model.RouteConfig
import com.sendi.deliveredrobot.model.ShoppingGuideConfing
import com.sendi.deliveredrobot.navigationtask.DownloadBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import org.litepal.LitePal
import java.io.File


/**
 * @Author Swn
 * @Data 2023/11/7
 * @describe
 */
class InteractionMqtt {

    var values: ContentValues? = null

    fun ActionShoppingType(message: String) {
        val gson = Gson()
        values = ContentValues()
        val map = LinkedHashMap<String, Long>()
        val shoppingConfig = gson.fromJson(message, ShoppingGuideConfing::class.java)
        RobotStatus.shoppingConfigList?.value = shoppingConfig
        Log.d("TAG", "收到导购配置")
        //导购配置更新
        LitePal.deleteAll(ShoppingConfigDB::class.java)
        val shoppingConfigDB = ShoppingConfigDB()
        shoppingConfigDB.name = shoppingConfig.name
        shoppingConfigDB.firstPrompt = shoppingConfig.firstPrompt
        shoppingConfigDB.completePrompt = shoppingConfig.completePrompt
        shoppingConfigDB.interruptPrompt = shoppingConfig.interruptPrompt
        shoppingConfigDB.baseTimeStamp = shoppingConfig.baseTimeStamp
        val actionList = shoppingConfig.actions// 路线列表对象
        val iterator = actionList?.iterator()
        var actionDBItem: List<ShoppingActionDB>
        //开始通过迭代器遍历导购点
        while (iterator!!.hasNext()) {
            val action = iterator.next()
            actionDBItem = ArrayList()
            val shoppingActionDB = ShoppingActionDB()
            //删除某个点
            if (action?.timestamp!! <= 0) {
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
                continue
            }
            //将数据添加到LinkHashMap
            for (actionHash in QuerySql.SelectShoppingAction(action.rootMapName)) {
                map[actionHash.name!!] = actionHash.timestamp!!
            }
            //HashMap查询数据
            val timestamp = map[action.name]
            if (timestamp != null && action.timestamp != timestamp) {
                Log.e("TAG", "ActionShoppingType: 查询到时间戳更改" )
                // 删除map中的数据
                map.remove(action.name)
                //删除对应的图片文件夹
                MqttMessageHandler.deleteFiles(File(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name))
                //删除数据库中的数据
                DeleteSql.deleteShoppingAction(action.name, action.rootMapName)
                println("Name: $action.name, Timestamp: $timestamp")
            }else if (action.timestamp == timestamp){
                continue
            }

            shoppingActionDB.name = action.name
            shoppingActionDB.actionType = action.actionType
            shoppingActionDB.pointName = action.pointName
            shoppingActionDB.waitingTime = action.waitingTime
            shoppingActionDB.standText = action.standText
            shoppingActionDB.arriveText = action.arriveText
            shoppingActionDB.moveTest = action.moveTest
            shoppingActionDB.timestamp = action.timestamp
            shoppingActionDB.rootMapName = action.rootMapName
            //大屏幕配置
            if (action.bigScreenConfig!!.screen == 1) {
                val bigScreenConfigDB = BigScreenConfigDB()
                //配置类型
                bigScreenConfigDB.type =
                    action.bigScreenConfig!!.type!!
                if (action.bigScreenConfig!!.argPic != null) {
                    //图片布局
                    bigScreenConfigDB.picType =
                        action.bigScreenConfig!!.argPic?.picType!!
                    //轮播时间
                    bigScreenConfigDB.picPlayTime =
                        action.bigScreenConfig!!.argPic!!.picPlayTime
                    //图片
                    MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/")
                    val picfile =
                        UpdateReturn().splitStr(action.bigScreenConfig!!.argPic!!.pics)
                    //创建对应文件夹。以路线名字命名(存放大屏幕)
                    bigScreenConfigDB.imageFile =
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"
                    Thread {
                        for (i in picfile.indices) {
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
                    bigScreenConfigDB.fontContent =
                        action.bigScreenConfig!!.argFont?.fontContent
                    //文字颜色
                    bigScreenConfigDB.fontColor =
                        action.bigScreenConfig!!.argFont?.fontColor
                    //文字大小 1-大，2-中，3-小,
                    bigScreenConfigDB.fontSize =
                        action.bigScreenConfig!!.argFont?.fontSize!!
                    //文字方向 1-横向，2-纵向
                    bigScreenConfigDB.fontLayout =
                        action.bigScreenConfig!!.argFont?.fontLayout!!
                    //背景颜色
                    bigScreenConfigDB.fontBackGround =
                        action.bigScreenConfig!!.argFont?.fontBackGround
                    //文字显示位置  0-居中 1-居上 2-居下
                    bigScreenConfigDB.textPosition =
                        action.bigScreenConfig!!.argFont?.textPosition!!
                }
                if (action.bigScreenConfig!!.argVideo != null) {
                    //视频是否播放声音
                    bigScreenConfigDB.videoAudio =
                        action.bigScreenConfig!!.argVideo?.videoAudio!!
                    bigScreenConfigDB.videolayout =
                        action.bigScreenConfig!!.argVideo?.videoLayOut!!
                    //视频储存位置
                    //创建对应文件夹。以路线名字命名(存放大屏幕)
                    MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/")

                    val picfile =
                        UpdateReturn().splitStr(action.bigScreenConfig!!.argVideo?.videos!!)
                    bigScreenConfigDB.videoFile =
                        Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/"
                    Thread {
                        for (i in picfile.indices) {
                            DownloadBill.getInstance().addTask(
                                Universal.pathDownload + picfile[i],
                                Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/big/",
                                MqttMessageHandler.FileName(picfile[i]!!),
                                MyApplication.listener
                            )
                        }
                    }.start()
                }
                bigScreenConfigDB.save()
                shoppingActionDB.bigScreenConfig = bigScreenConfigDB
            }

            if (action.touchScreenConfig!!.screen == 0) {
                //创建对应文件夹。以路线名字命名(存放主屏幕)
                MqttMessageHandler.openFile(Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/")
                val touchScreenConfigDB = TouchScreenConfigDB()
                //配置类型
                touchScreenConfigDB.touch_type =
                    action.touchScreenConfig!!.type!!
                if (action.touchScreenConfig!!.argPic != null) {
                    //图片布局
                    touchScreenConfigDB.touch_picType =
                        action.touchScreenConfig!!.argPic?.picType!!
                    //轮播时间
                    touchScreenConfigDB.touch_picPlayTime =
                        action.touchScreenConfig!!.argPic?.picPlayTime!!
                    //图片路径
                    if (action.touchScreenConfig!!.argPic?.pics!!.isNotEmpty()) {
                        val picfile =
                            UpdateReturn().splitStr(action.touchScreenConfig!!.argPic?.pics!!)

                        touchScreenConfigDB.touch_imageFile =
                            Universal.robotFile + "shopping/" + action.rootMapName + "/" + action.name + "/touch/"
                        Thread {
                            for (i in picfile.indices) {
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
                    touchScreenConfigDB.touch_fontContent =
                        action.touchScreenConfig!!.argFont!!.fontContent
                    //文字颜色
                    touchScreenConfigDB.touch_fontColor =
                        action.touchScreenConfig!!.argFont!!.fontColor
                    //文字大小 1-大，2-中，3-小,
                    touchScreenConfigDB.touch_fontSize =
                        action.touchScreenConfig!!.argFont!!.fontSize
                    //文字方向 1-横向，2-纵向
                    touchScreenConfigDB.touch_fontLayout =
                        action.touchScreenConfig!!.argFont!!.fontLayout
                    //背景颜色
                    touchScreenConfigDB.touch_fontBackGround =
                        action.touchScreenConfig!!.argFont!!.fontBackGround
                    //文字显示位置  0-居中 1-居上 2-居下
                    touchScreenConfigDB.touch_textPosition =
                        action.touchScreenConfig!!.argFont!!.textPosition
                }
                if (action.touchScreenConfig!!.argPicGroup != null) {
                    //行走中
                    if (action.touchScreenConfig!!.argPicGroup!!.walkPic != "") {
                        touchScreenConfigDB.touch_walkPic =
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
                        touchScreenConfigDB.touch_walkPic = Universal.gifDefault
                    }
                    //被阻挡
                    if (action.touchScreenConfig!!.argPicGroup!!.blockPic != "") {
                        touchScreenConfigDB.touch_blockPic =
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
                        touchScreenConfigDB.touch_blockPic =
                            Universal.gifDefault
                    }
                    //到点
                    if (action.touchScreenConfig!!.argPicGroup!!.arrivePic != "") {
                        touchScreenConfigDB.touch_arrivePic =
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
                        touchScreenConfigDB.touch_arrivePic =
                            Universal.gifDefault
                    }
                    //返回
                    if (action.touchScreenConfig!!.argPicGroup!!.overTaskPic != "") {
                        touchScreenConfigDB.touch_overTaskPic =
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
                        touchScreenConfigDB.touch_overTaskPic =
                            Universal.gifDefault
                    }
                }
                touchScreenConfigDB.save()
                shoppingActionDB.touchScreenConfig = touchScreenConfigDB
            }
            shoppingActionDB.save()
            actionDBItem.add(shoppingActionDB)
            shoppingConfigDB.actions = actionDBItem
        }
        if (shoppingConfigDB.save()) {
            // 数据保存成功
            Log.d("TAG", "云平台下发导购配置保存成功")
        } else {
            // 数据保存失败
            Log.d("TAG", "云平台下发导购配置数据保存失败")
        }
    }

    fun ExplainType(message: String) {
        val gson = Gson()
        val routeConfig = gson.fromJson(message, RouteConfig::class.java)
        RobotStatus.routeConfig?.value = routeConfig
        //所有图片存储的总路径
        LogUtil.d("收到讲解路线配置")
        val routeDB = RouteDB()
        //存储图片的数据库
        val routeList = routeConfig.routeList// 路线列表对象
        val isExist =
            LitePal.where("routename = ?", routeList!![0].routeName)
                .count(RouteDB::class.java) > 0
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
                TouchScreenConfigDB::class.java,
                "pointconfigvodb_id = ?",
                pointConfigVoDB_id.toString()
            )
            val deleteBigScreen = LitePal.deleteAll(
                BigScreenConfigDB::class.java,
                "pointconfigvodb_id = ?",
                pointConfigVoDB_id.toString()
            )
            val deletePointConfig = LitePal.deleteAll(
                PointConfigVODB::class.java,
                "routedb_id = ?",
                routeDBID.toString()
            )
            val deleteRouteDB =
                LitePal.deleteAll(RouteDB::class.java, "routename = ?", routeList[0].routeName)
            if (deleteTouchScreen > 0) {
                Log.e("TAG", "TouchScreenConfigDB数据删除成功")
            }
            if (deleteBigScreen > 0) {
                Log.e("TAG", "BigScreenConfigDB数据删除成功")
            }
            if (deletePointConfig > 0) {
                Log.e("TAG", "PointConfigVODB数据删除成功")
            }
            if (deleteRouteDB > 0) {
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
                routeDB.routeName = route.routeName
                //总图名字
                routeDB.rootMapName = route.rootMapName
                //简介
                routeDB.introduction = route.introduction
                //配置时间戳
                routeDB.timeStamp = route.timeStamp
                //路线背景图
                if (route.backgroundPic?.isNotEmpty() == true) {
                    MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/")
                    MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                    val sdcardFile =
                        MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch")
                    val picfile =
                        UpdateReturn().splitStr(route.backgroundPic)

                    val backPic = MqttMessageHandler.compareArrays(sdcardFile, picfile)
                    for (i in backPic.SameOne!!.indices) {
                        UpdateReturn().deleteFolderFile(backPic.SameOne!![i], true)
                    }
                    routeDB.backgroundPic =
                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + route.backgroundPic.substring(
                            route.backgroundPic.lastIndexOf("/") + 1
                        ) //路线背景图
                    if (backPic.SameAll?.isNotEmpty() == true) {
                        for (i in backPic.SameTwo!!.indices)
                            UpdateReturn().deleteFolderFile(
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + (backPic.SameTwo!![i]!!),
                                true
                            )
                    }
                    if (backPic.SameTwo?.isNotEmpty() == true) {
                        Thread {
                            for (i in backPic.SameTwo!!.indices) {
                                DownloadBill.getInstance().addTask(
                                    Universal.pathDownload + backPic.SameTwo!![i],
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch",
                                    MqttMessageHandler.FileName(backPic.SameTwo!![i]!!),
                                    MyApplication.listener
                                )
                            }
                        }.start()
                    }

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
                            MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
                        val picfile =
                            UpdateReturn().splitStr(point.walkVoice)

                        val walkMp3 = MqttMessageHandler.compareArrays(sdcardFile, picfile)

                        if (walkMp3.SameTwo?.isNotEmpty() == true) {
                            Thread {
                                for (i in walkMp3.SameTwo!!.indices) {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + walkMp3.SameTwo!![i],
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                        MqttMessageHandler.FileName(walkMp3.SameTwo!![i]!!),
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
                            MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3/")
                        val picfile =
                            UpdateReturn().splitStr(point.explanationVoice)

                        val explanationMp3 =
                            MqttMessageHandler.compareArrays(sdcardFile, picfile)
                        if (explanationMp3.SameTwo?.isNotEmpty() == true) {
                            Thread {
                                for (i in explanationMp3.SameTwo!!.indices) {
                                    DownloadBill.getInstance().addTask(
                                        Universal.pathDownload + explanationMp3.SameTwo!![i],
                                        Universal.robotFile + route.rootMapName + "/" + route.routeName + "/mp3",
                                        MqttMessageHandler.FileName(explanationMp3.SameTwo!![i]!!),
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
                            MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                            val sdcardFile =
                                MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                            val picfile =
                                UpdateReturn().splitStr(point.bigScreenConfig.argPic.pics)
                            val bigPic = MqttMessageHandler.compareArrays(sdcardFile, picfile)

                            for (i in bigPic.SameOne!!.indices) {
                                UpdateReturn().deleteFolderFile(
                                    bigPic.SameOne!![i],
                                    true
                                )
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
                                            MqttMessageHandler.FileName(bigPic.SameTwo!![i]!!),
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
                            MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)

                            val sdcardFile =
                                MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name)
                            val picfile =
                                UpdateReturn().splitStr(point.bigScreenConfig.argVideo.videos)
                            val argVideoName =
                                MqttMessageHandler.compareArrays(sdcardFile, picfile)
                            for (i in argVideoName.SameOne!!.indices) {
                                UpdateReturn().deleteFolderFile(
                                    argVideoName.SameOne!![i],
                                    true
                                )
                            }
                            bigScreenConfigDB.videoFile =
                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name
                            if (argVideoName.SameTwo?.isNotEmpty() == true) {
                                Thread {
                                    for (i in argVideoName.SameTwo!!.indices) {
                                        DownloadBill.getInstance().addTask(
                                            Universal.pathDownload + argVideoName.SameTwo!![i],
                                            Universal.robotFile + route.rootMapName + "/" + route.routeName + "/big/" + point.name,
                                            MqttMessageHandler.FileName(argVideoName.SameTwo!![i]!!),
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
                        //创建对应文件夹。以路线名字命名(存放主屏幕)
                        MqttMessageHandler.openFile(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name + "/")
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
                                    MqttMessageHandler.selectImagePath(Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name)
                                val picfile =
                                    UpdateReturn().splitStr(point.touchScreenConfig.argPic.pics)
                                val touchFileName =
                                    MqttMessageHandler.compareArrays(sdcardFile, picfile)

                                for (i in touchFileName.SameOne!!.indices) {
                                    UpdateReturn().deleteFolderFile(
                                        touchFileName.SameOne!![i],
                                        true
                                    )
                                }
                                touchScreenConfigDB.touch_imageFile =
                                    Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name + "/"

                                if (touchFileName.SameTwo!!.isNotEmpty()) {
                                    Thread {
                                        for (i in touchFileName.SameTwo!!.indices) {
                                            DownloadBill.getInstance().addTask(
                                                Universal.pathDownload + touchFileName.SameTwo!![i],
                                                Universal.robotFile + route.rootMapName + "/" + route.routeName + "/touch/" + point.name,
                                                MqttMessageHandler.FileName(touchFileName.SameTwo!![i]!!),
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
                            if (point.touchScreenConfig.argPicGroup.walkPic != "") {
                                touchScreenConfigDB.touch_walkPic =
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
                                touchScreenConfigDB.touch_walkPic = Universal.gifDefault
                            }
                            //被阻挡
                            if (point.touchScreenConfig.argPicGroup.blockPic != "") {
                                touchScreenConfigDB.touch_blockPic =
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
                                touchScreenConfigDB.touch_blockPic =
                                    Universal.gifDefault
                            }
                            //到点
                            if (point.touchScreenConfig.argPicGroup.arrivePic != "") {
                                touchScreenConfigDB.touch_arrivePic =
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
                                touchScreenConfigDB.touch_arrivePic =
                                    Universal.gifDefault
                            }
                            //返回
                            if (point.touchScreenConfig.argPicGroup.overTaskPic != "") {
                                touchScreenConfigDB.touch_overTaskPic =
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
                                touchScreenConfigDB.touch_overTaskPic =
                                    Universal.gifDefault
                            }
                        }
                        touchScreenConfigDB.save()
                        pointConfigVODB.touchScreenConfigDB = touchScreenConfigDB
                    }
                    pointConfigVODB.save()
                    pointItem.add(pointConfigVODB)
                    routeDB.mapPointName = pointItem
                    if (routeDB.save()) {
                        // 数据保存成功
                        Log.d("TAG", "receive: 讲解点数据保存成功")
                    } else {
                        // 数据保存失败
                        Log.d("TAG", "receive: 讲解点数据保存失败")
                    }
                }
            }
        }


    }
}