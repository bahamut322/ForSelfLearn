package com.sendi.deliveredrobot.service

import android.content.ContentValues
import android.os.Environment
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MapConfig
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.wislie.charging.helper.Builder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.litepal.LitePal
import java.io.File
import java.util.*


class UpdateReturn {

    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    private lateinit var debugDao: DebugDao
    lateinit var dao: DeliveredRobotDao
    private var pointIdList: ArrayList<Int>? = ArrayList()

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
            withContext(Dispatchers.Default) {
//                listData = debugDao.queryTargetPointMap()!!
                val mMapResult = mapTargetPointServiceImpl.mapsName
                val listData = mMapResult.data["mapsName"] as List<*>
                val mapList: ArrayList<String> = ArrayList()
                for (i in listData.indices) {
                    mapList.add(listData[i].toString())
                }
                val jsonObject = JSONObject()//实例话JsonObject()
                jsonObject["type"] = "queryConfigTime"
                jsonObject["robotTimeStamp"] = timeStampRobotConfigSql
                jsonObject["gateTimeStamp"] = timeStampReplyGateConfig
                jsonObject["maps"] = mapList
                //发送Mqtt
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)
            }
        }
    }

    fun fileSize(fileName: String): Int {
        val file: File = File(fileName)
        val files = file.listFiles()
        return files.size
    }

    fun assignment() {
        Log.d("TAG", "LitePal数据库数据获取")
        dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        debugDao = DataBaseDeliveredRobotMap.getDatabase(
            Objects.requireNonNull(
                MyApplication.instance
            )!!
        ).getDebug()
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
            if (robotConfigDatas.mapName != "") {
                val mapId = debugDao.selectMapId(robotConfigDatas.mapName)
                Log.d(ContentValues.TAG, "地图ID： $mapId")
                //设置总图
                dao.updateMapConfig(MapConfig(1, mapId, null))
                //查询充电桩
                val pointId = dao.queryChargePointList()
                pointId?.map {
                    pointIdList?.add(it.pointId!!)
                }
                //设置充电桩；默认查询到的第一个数据
                dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0)))
            }
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