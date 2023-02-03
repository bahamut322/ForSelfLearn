package com.sendi.deliveredrobot.service

import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import java.io.File
import java.util.*

class UpdateReturn {

    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()

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

}