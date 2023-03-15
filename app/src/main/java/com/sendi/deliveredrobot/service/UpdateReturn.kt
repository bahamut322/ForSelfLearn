package com.sendi.deliveredrobot.service

import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.entity.MapRevise
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.RouteDB
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MapConfig
import com.sendi.deliveredrobot.room.entity.QueryAllPointEntity
import com.sendi.deliveredrobot.room.entity.SendFloor
import com.sendi.deliveredrobot.room.entity.SendMapPoint
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.utils.LogUtil
import javassist.bytecode.stackmap.TypeData.ClassName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.LitePal.where
import java.io.File
import java.util.*


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

                for (i in 0 until mapList.size ){
                    val mapId = debugDao.selectMapId(mapList[i])

                    val query = queryAllMapPointsDao.queryAllMapsPoints().groupBy { it.name as String }
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
                        if (!isExist){
                            val sendMapPoint = SendMapPoint(0, key, sendFloor)
                            mapPoint.add(sendMapPoint)
                        }else {
                            val tipsList: List<MapRevise> = where(
                                "mapName = ?", key
                            ).find(MapRevise::class.java)
                            val sendMapPoint = SendMapPoint(tipsList[0].time, key, sendFloor)
                            mapPoint.add(sendMapPoint)
                        }
                    }
                }

                Universal.MapName = queryAllMapPointsDao.queryCurrentMapName();

                val jsonObject = JSONObject()//实例话JsonObject()
                jsonObject["type"] = "queryConfigTime"
                jsonObject["robotTimeStamp"] = timeStampRobotConfigSql
                jsonObject["gateTimeStamp"] = timeStampReplyGateConfig
                jsonObject["curMapName"] = queryAllMapPointsDao.queryCurrentMapName()
                jsonObject["explanationTimeStamp"] = QuerySql.QueryExplainConfig()[0].timeStamp
                jsonObject["advertTimeStamp"] = QuerySql.advTimeStamp()
                jsonObject["routes"] = QuerySql.QueryRoutesSendMessage(queryAllMapPointsDao.queryCurrentMapName())
                jsonObject["maps"] = mapPoint
                //发送Mqtt
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)


            }
        }
    }


    fun fileSize(fileName: String): Int {
        val file = File(fileName)
        val files = file.listFiles()
        return files.size
    }

    fun mapSetting() {
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
            //设置充电桩；默认查询到的第一个数据
            dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0)))
            val queryPoint =
                dao.queryChargePoint()
            RobotStatus.originalLocation = queryPoint
            mapSettingBoolean = ROSHelper.setNavigationMap(
                labelMapName = RobotStatus.bootLocation!!.subPath!!,
                pathMapName = RobotStatus.bootLocation!!.routePath!!
            )
            if (mapSettingBoolean) {
                LogUtil.d("mapSetting: 地图设置成功")
                DialogHelper.loadingDialog.dismiss()
            } else {
                LogUtil.d("mapSetting: 地图设置失败")
                DialogHelper.loadingDialog.dismiss()
            }
        }
    }

    fun assignment() {
        //机器人基础配置
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            Universal.audioType = robotConfigDatas.audioType
            Universal.wakeUpWord = robotConfigDatas.wakeUpWord
            Universal.sleep = robotConfigDatas.sleep
            Universal.sleepTime = robotConfigDatas.sleepTime
            Universal.wakeUpType = robotConfigDatas.wakeUpList
            Universal.sleepType = robotConfigDatas.sleepType
            Universal.picType = robotConfigDatas.picType
            Universal.timeStampRobotConfigSql = robotConfigDatas.timeStamp
            Universal.mapName = robotConfigDatas.mapName
            Universal.password = robotConfigDatas.password
            //设置默认密码
            if (robotConfigDatas.password == null) {
                Universal.password = "8888"
            }
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
     * 将以逗号分隔的字符串转换为字符串数组；
     * 第二种方法：
     * @param str
     * @return
     */
    fun splitStr(str: String): Array<String?>? {
        return str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
}