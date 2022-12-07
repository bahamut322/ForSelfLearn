package com.sendi.deliveredrobot.topic

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.ros.dto.RosResult

object LoraReceiveTopic {
    fun handle(rosResult: RosResult<*>) {
        if (!rosResult.isFlag) return
        try {
            val message = rosResult.response as std_msgs.String
            val jsonObject = JsonParser.parseString(message.data) as JsonObject
            when (jsonObject.get("type").asInt) {
                1 -> {
                    // 电梯状态
                }
                2 -> {
                    // 电梯楼层
                    val currentFloorName = jsonObject.get("currentFloorName").asString
                    val elevator = jsonObject.get("elevator").asString
                    LiftHelper.liftReach(currentFloorName, elevator)
                }
                else -> {}
            }

        } catch (e: Exception) {
        }
    }
}