package com.sendi.deliveredrobot.service

import com.alibaba.fastjson.JSONObject
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.UploadMapHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.model.Map
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MapConfig
import com.sendi.deliveredrobot.room.entity.SendFloor
import com.sendi.deliveredrobot.room.entity.SendMapPoint
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.LitePal.where
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.dropLastWhile
import kotlin.collections.forEach
import kotlin.collections.groupBy
import kotlin.collections.indices
import kotlin.collections.iterator
import kotlin.collections.map
import kotlin.collections.mapValues
import kotlin.collections.set
import kotlin.collections.toTypedArray


class UpdateReturn {

    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    private lateinit var debugDao: DebugDao
    lateinit var dao: DeliveredRobotDao
    private var pointIdList: ArrayList<Int>? = ArrayList()
    private val queryAllMapPointsDao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    fun method() {
        val replyGateConfigData: List<ReplyGateConfig> =
            LitePal.findAll(ReplyGateConfig::class.java)
        for (replyGateConfigDatas in replyGateConfigData) {
            timeStampReplyGateConfig = replyGateConfigDatas.timeStamp
        }
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            timeStampRobotConfigSql = robotConfigDatas.timeStamp
        }
        val timeGetTime = Date().time //时间戳
        if (timeStampRobotConfigSql == null) {
            timeStampRobotConfigSql = timeGetTime
        }
        if (timeStampReplyGateConfig == null) {
            timeStampReplyGateConfig = timeGetTime
        }
        //查询地图名字
        MainScope().launch {
            debugDao = DataBaseDeliveredRobotMap.getDatabase(
                Objects.requireNonNull(
                    MyApplication.instance
                )!!
            ).getDebug()
            withContext(Dispatchers.Default) {
//                listData = debugDao.queryTargetPointMap()!!
                val mMapResult = mapTargetPointServiceImpl.mapsName
                val listData = mMapResult.data["mapsName"] as List<*>
                val mapList: ArrayList<String> = ArrayList()
                for (i in listData.indices) {
                    mapList.add(listData[i].toString())
                }

                val mapPoint: ArrayList<SendMapPoint> = ArrayList()

                for (i in 0 until mapList.size) {
                    val mapId = debugDao.selectMapId(mapList[i])

                    val query =
                        queryAllMapPointsDao.queryAllMapsPoints().groupBy { it.name as String }
                            .mapValues { (_, maps) ->
                                maps.groupBy { it.floorName as String }
                            }
                    var sendFloor: ArrayList<SendFloor> = ArrayList()
                    val iterator = query.iterator()
                    //解析query
                    while (iterator.hasNext()) {
                        val (key, value) = iterator.next()
                        //解析query的value
                        val iterator1 = value.iterator()
                        while (iterator1.hasNext()) {
                            sendFloor = ArrayList()
                            val (key1, value1) = iterator1.next()
                            val sendFloor1 = SendFloor(key1, value1)
                            sendFloor.add(sendFloor1)
                        }
                        //添加数据
                        //查询地图修改的时间戳
                        val isExist = where("mapName = ?", key).count(MapRevise::class.java) > 0
                        if (!isExist) {
                            val sendMapPoint = SendMapPoint(0, key, sendFloor)
                            mapPoint.add(sendMapPoint)
                        } else {
                            val tipsList: List<MapRevise> = where(
                                "mapName = ?", key
                            ).find(MapRevise::class.java)
                            val sendMapPoint = SendMapPoint(tipsList[0].time, key, sendFloor)
                            mapPoint.add(sendMapPoint)
                        }
                    }
                }

                sendMapData()
                Universal.MapName = queryAllMapPointsDao.queryCurrentMapName();
                val jsonObject = JSONObject()//时间戳
                jsonObject["type"] = "queryConfigTime"
                jsonObject["robotTimeStamp"] = timeStampRobotConfigSql
                jsonObject["gateTimeStamp"] = timeStampReplyGateConfig
                jsonObject["explanationTimeStamp"] = QuerySql.QueryExplainConfig().timeStamp
                jsonObject["advertTimeStamp"] = QuerySql.advTimeStamp()
                jsonObject["routes"] =
                    QuerySql.QueryRoutesSendMessage(queryAllMapPointsDao.queryCurrentMapName())
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)


            }
        }
    }

    fun resume() {
        MainScope().launch() {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
        }
    }

    fun pause() {
        MainScope().launch() {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
        }
    }

    fun stop() {
        MainScope().launch() {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
        }
    }

    fun sendMapData() {
        MainScope().launch(Dispatchers.Default) {
            val rootMapList = queryAllMapPointsDao.queryRootMap()
            val maps = ArrayList<Map>()
            val areasOriginal = queryAllMapPointsDao.queryPublicArea()
            val areas = ArrayList<Area>().apply {
                areasOriginal.map {
                    this.add(
                        Area(
                            id = it.id,
                            name = it.name ?: ""
                        )
                    )
                }
            }
            val currentMapName = queryAllMapPointsDao.queryCurrentMapName()
            rootMapList?.forEach { rootMap ->
                val queryFloorPoints = queryAllMapPointsDao.queryAllPoint(rootMap.id)
                val floorPointsMap = queryFloorPoints.groupBy {
                    it.floorName
                }
                val floors = ArrayList<Floor>()
                for (entry in floorPointsMap) {
                    val points = ArrayList<Point>()
                    for (queryPointEntity in entry.value) {
                        points.add(
                            Point(
                                areaId = queryPointEntity.type ?: -1,
                                pointName = queryPointEntity.pointName ?: "",
                                x = "${queryPointEntity.x ?: "0.0"}".toDouble(),
                                y = "${queryPointEntity.y ?: "0.0"}".toDouble(),
                                w = "${queryPointEntity.w ?: "0.0"}".toDouble()
                            )
                        )
                    }
                    floors.add(Floor("${entry.key}", points))
                }
                maps.add(
                    Map(
                        floorList = floors,
                        mapName = rootMap.name ?: "",
                        mapTimeStamp = System.currentTimeMillis()
                    )
                )
            }
            val uploadMapDataModel = UploadMapDataModel(
                areas = areas,
                curMapName = currentMapName ?: "",
                maps = maps
            )
            MqttService.publish(uploadMapDataModel.toString(), true)
        }
    }

    fun fileSize(fileName: String): Int {
        val file = File(fileName)
        val files = file.listFiles()
        return files.size
    }

    fun mapSetting(batteryState:Boolean = false) {
        val mapSettingBoolean: Boolean
        DialogHelper.loadingDialog.show()
        dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        debugDao = DataBaseDeliveredRobotMap.getDatabase(
            Objects.requireNonNull(
                MyApplication.instance
            )!!
        ).getDebug()
        if (Universal.mapName != "") {
            val mapId = debugDao.selectMapId(Universal.mapName)
//            val mapId = debugDao.selectMapId("map-0209-1")
            LogUtil.d("地图ID： $mapId")
            //设置总图
            dao.updateMapConfig(MapConfig(1, mapId, null))
            //查询充电桩
            val pointId = dao.queryChargePointList()
            pointId?.map {
                pointIdList?.add(it.pointId!!)
            }
            LogUtil.i("充电桩列表：${JSONObject.toJSONString(pointIdList)}")
            //设置充电桩；默认查询到的第一个数据
            if (pointIdList != null) {
                dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0)))
                RobotStatus.originalLocation = pointId?.get(0)!!
            }
            if (batteryState) {
                if (pointId != null) {
                    var retryTime = 10  // 设置地图次数
                    DialogHelper.loadingDialog.show()
                    //切换地图
                    var switchMapResult: Boolean
                    do {
                        switchMapResult = ROSHelper.setNavigationMap(
                            pointId[0].subPath ?: "",
                            pointId[0].routePath ?: ""
                        )
                        retryTime--
                    } while (!switchMapResult && retryTime > 0)
                    ROSHelper.setPoseClient(pointId[0])
                    //查看切换锚点是否成功
                    var result: Boolean
                    do {
                        result =
                            ROSHelper.getParam("/finish_update_pose") == "1"
                        if (!result) {
//                        virtualTaskExecute(2, "设置页查看锚点")
                            retryTime--
                        }
                    } while (!result && retryTime > 0)
                    if (retryTime <= 0) {
                        ToastUtil.show("设置地图失败")
                    } else {
                        LogUtil.i("finish_update_pose成功")
                    }
                    UploadMapHelper.uploadMap()
                    DialogHelper.loadingDialog.dismiss()
                }
            }
        }
    }


    fun assignment() {
        //机器人基础配置
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            Universal.timeStampRobotConfigSql = robotConfigDatas.timeStamp
            Universal.mapName = robotConfigDatas.mapName
        }
        //门岗配置
        val replyGateConfigData: List<ReplyGateConfig> =
            LitePal.findAll(ReplyGateConfig::class.java)
        for (replyGateConfigDatas in replyGateConfigData) {
            Universal.picTypeNum = replyGateConfigDatas.picType
            Universal.picPlayTime = replyGateConfigDatas.picPlayTime
            Universal.videoAudio = replyGateConfigDatas.videoAudio
            Universal.fontContent = replyGateConfigDatas.fontContent
            Universal.fontColor = replyGateConfigDatas.fontColor
            Universal.fontSize = replyGateConfigDatas.fontSize
            Universal.fontLayout = replyGateConfigDatas.fontLayout
            Universal.fontBackGround = replyGateConfigDatas.fontBackGround
            Universal.tipsTemperatureInfo = replyGateConfigDatas.tipsTemperatureInfo
            Universal.tipsTemperatureWarn = replyGateConfigDatas.tipsTemperatureWarn
            Universal.tipsMaskWarn = replyGateConfigDatas.tipsMaskWarn
            Universal.timeStampReplyGateConfig = replyGateConfigDatas.timeStamp
            Universal.bigScreenType = replyGateConfigDatas.bigScreenType
            Universal.textPosition = replyGateConfigDatas.textPosition
            Universal.TemperatureMax = replyGateConfigDatas.temperatureThreshold
        }
    }

    /**
     * 云平台返回的声音设置数字转为中文
     * @param Num 收到云平台音色配置的数据
     */
    fun audioName(Num: Int): String {
        var audioModel = ""
        //注：云平台下发的0代表女声，在BaiduTTS中1代表女生
        if (Num == 0) {
            audioModel = "女声"
        } else if (Num == 2) {
            audioModel = "男声"
        } else if (Num == 3) {
            audioModel = "童声"
        }
        return audioModel
    }

    private fun getParam(speak: String): kotlin.collections.Map<String, String> {
        return HashMap<String, String>().apply {
            this[SpeechSynthesizer.PARAM_SPEAKER] = "4"
            this[SpeechSynthesizer.PARAM_VOLUME] = "15"
            this[SpeechSynthesizer.PARAM_SPEED] = speak
            this[SpeechSynthesizer.PARAM_PITCH] = "15"
        }
    }

    /**
     * BaiduTTS音色
     */
    fun randomVoice(i: Int, speak: String) {
        val params = getParam(speak)
        if (i == 1) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_FEMALE)//女
        } else if (i == 2) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_MALE)//男
        } else if (i == 3) {
            BaiduTTSHelper.getInstance().setParam(params, OfflineResource.VOICE_DUYY)//童
        }
    }

    /**
     * BaiduTTS语速
     */
    fun timbres(speed: String) {
        if ("男声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(2, speed)
        } else if ("女声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(1, speed)
        } else if ("童声" == QuerySql.QueryBasic().robotMode) {
            randomVoice(3, speed)
        }
    }

    /**
     * 将以逗号分隔的字符串转换为字符串数组；
     * 第二种方法：
     * @param str
     * @return
     */
    fun splitStr(str: String): Array<String?> {
        return str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
    fun taskDto(): TaskDto {
        return TaskDto().apply { status = 1 }
    }
}