package com.sendi.deliveredrobot.helpers

import com.alibaba.fastjson.JSON
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object UploadMapHelper {
    val mainScope = MainScope()
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    fun uploadMap(){
        mainScope.launch(Dispatchers.Default) {
            val queryFloorPoints = dao.queryAllPoints()
            val jsonString = JSON.toJSONString(queryFloorPoints)
            CloudMqttService.publish("""
                {
                  "type": "robotPointList",
                  "pointList": ${jsonString},
                }
            """.trimIndent())
        }
    }
}