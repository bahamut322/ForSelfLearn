package com.sendi.deliveredrobot.service

import android.text.TextUtils
import chassis_msgs.VersionGetResponse
import com.alibaba.fastjson.JSONObject
import com.baidu.tts.client.SpeechSynthesizer
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.baidutts.util.OfflineResource
import com.sendi.deliveredrobot.entity.MapRevise
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper.mainScope
import com.sendi.deliveredrobot.model.Area
import com.sendi.deliveredrobot.model.Floor
import com.sendi.deliveredrobot.model.Map
import com.sendi.deliveredrobot.model.Point
import com.sendi.deliveredrobot.model.UploadMapDataModel
import com.sendi.deliveredrobot.navigationtask.AbstractTaskBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.Vire
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
import java.util.Date
import java.util.Objects
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class UpdateReturn {

    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    private lateinit var debugDao: DebugDao
    lateinit var dao: DeliveredRobotDao
    private var pointIdList: ArrayList<Int>? = ArrayList()
    private val queryAllMapPointsDao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    fun method(boolean: Boolean = true) {
        val replyGateConfigData: List<ReplyGateConfig> =
            LitePal.findAll(ReplyGateConfig::class.java)
        for (replyGateConfigDatum in replyGateConfigData) {
            timeStampReplyGateConfig = replyGateConfigDatum.timeStamp
        }
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatum in robotConfigData) {
            timeStampRobotConfigSql = robotConfigDatum.timeStamp
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
                )
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
                if (boolean) {
                    sendMapData()
                }
                Universal.MapName = queryAllMapPointsDao.queryCurrentMapName()
                val jsonObject = JSONObject()//时间戳
                jsonObject["type"] = "queryConfigTime"
                jsonObject["robotTimeStamp"] = timeStampRobotConfigSql
                jsonObject["gateTimeStamp"] = timeStampReplyGateConfig
                jsonObject["explanationTimeStamp"] = QuerySql.QueryExplainConfig().timeStamp
                jsonObject["advertTimeStamp"] = QuerySql.advTimeStamp()
                jsonObject["routes"] =
                    QuerySql.QueryRoutesSendMessage(queryAllMapPointsDao.queryCurrentMapName())
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)
                //上报版本
                mainScope.launch {
                    var versionGetResponse: VersionGetResponse? = null
                    withContext(Dispatchers.Default) {
                        versionGetResponse = ROSHelper.getVersion(1) ?: return@withContext
                    }
                    if (versionGetResponse?.success == true) {
                        val jsonObjectVersion = JSONObject()
                        jsonObjectVersion["type"] = "uploadVersion"
                        jsonObjectVersion["chassisVersion"] = versionGetResponse?.version ?: ""
                        jsonObjectVersion["applicationVersion"] = BuildConfig.VERSION_NAME
                        CloudMqttService.publish(JSONObject.toJSONString(jsonObjectVersion), true)
                    }
                }
            }
        }
    }

    fun resume() {
        MainScope().launch {
            Universal.explainUnSpeak = false
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
        }
    }

    fun pause() {
        MainScope().launch {
            Universal.explainUnSpeak = true
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
        }
    }

    fun stop() {
        MainScope().launch {
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
                waitingPointName = AbstractTaskBill.dao.queryReadyPoint()?.pointName ?:"",
                chargePointName = AbstractTaskBill.dao.queryChargePoint()?.pointName ?:"",
                maps = maps
            )
            MqttService.publish(uploadMapDataModel.toString(), true)
        }
    }

//    fun fileSize(fileName: String): Int {
//        val file = File(fileName)
//        val files = file.listFiles()
//        return files!!.size
//    }

    suspend fun mapSetting(batteryState: Boolean = false) {
        dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        debugDao = DataBaseDeliveredRobotMap.getDatabase(
            Objects.requireNonNull(
                MyApplication.instance
            )
        ).getDebug()
        if (QuerySql.robotConfig().mapName != "") {
            val mapId = debugDao.selectMapId(QuerySql.robotConfig().mapName)
            //查询待命点
            dao.updateMapConfig(MapConfig(1, mapId,null,null))
//            val mapId = debugDao.selectMapId("map-0209-1")
            LogUtil.d("地图ID： $mapId")
            //查询充电桩
            val pointId = dao.queryChargePointList()
            pointId?.map {
                pointIdList?.add(it.pointId!!)
            }
            LogUtil.i("充电桩列表：${JSONObject.toJSONString(pointId)}")
            //设置充电桩；默认查询到的第一个数据

            if (pointIdList?.size!! > 0) {
                dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0), null))
            }
//            else{
//                dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0), pointIdList?.get(0)))
//            }

//            if (batteryState) {
            val floorId = pointId?.get(0)?.floorName?.hashCode() ?: -1
            ROSHelper.setDispatchFloorId(floorId)
            RobotStatus.originalLocation = pointId?.get(0)
            LogUtil.d("地图设置: " + JSONObject.toJSONString(pointId?.get(0)))
            RobotStatus.currentLocation = RobotStatus.originalLocation
            if (pointId != null) {
                var retryTime = 10  // 设置地图次数
                //切换地图
                var switchMapResult: Boolean
                do {
                    switchMapResult = ROSHelper.setNavigationMap(
                        pointId[0].subPath ?: "",
                        pointId[0].routePath ?: ""
                    )
                    retryTime--
                } while (!switchMapResult && retryTime > 0)
                if (batteryState) {
                    LogUtil.d("对接充电桩设置充电点: $batteryState")
                    ROSHelper.setChargePose(pointId[0])
                } else {
                    ROSHelper.setPoseClient(pointId[0])
                }
                //查看切换锚点是否成功
                var result: Boolean
                do {
                    result =
                        ROSHelper.getParam("/finish_update_pose") == "1"
                    if (!result) Vire(2, "设置页查看锚点")
                    retryTime--
                } while (!result && retryTime > 0)
                if (retryTime <= 0) {
                    ToastUtil.show("设置地图失败")
                } else {
                    LogUtil.i("finish_update_pose成功")
                }
                //查询待命点
                val readyPoint = AbstractTaskBill.dao.queryReadyPointList()
                //设置待命点
                if (readyPoint?.size!! > 0) {
                    dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0), readyPoint[0].pointId))
                }
                sendMapData()
                DialogHelper.loadingDialog.dismiss()
                Universal.mapType = true
            }
        }
    }

    fun assignment() {
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
            Universal.TempVideoLayout = replyGateConfigDatas.videolayout
        }
    }

    /**
     * 云平台返回的声音设置数字转为中文
     * @param Nume 收到云平台音色配置的数据
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
            this[SpeechSynthesizer.PARAM_PITCH] = "5"
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
                        if (file.listFiles()?.size == 0) { //目录下没有文件或者目录，删除
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
    /**
     * 删除目录
     */
    fun deleteDirectory(directory: File) {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
            LogUtil.d("deleteDirectory: $directory 删除成功")
            directory.delete()
        }else{
            LogUtil.d("deleteDirectory: $directory 不存在")
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

    fun settingMap(batteryState: Boolean = false) {
        mainScope.launch(Dispatchers.Default) {
            UpdateReturn().mapSetting(batteryState)
        }
    }

    fun taskDto(): TaskDto {
        return TaskDto().apply { status = 1 }
    }
}