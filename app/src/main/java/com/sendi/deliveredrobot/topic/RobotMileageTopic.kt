package com.sendi.deliveredrobot.topic

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import std_msgs.Float64

object RobotMileageTopic {
    fun handle(rosResult: RosResult<*>) {
        if (!rosResult.isFlag) return
        try {
            val response = rosResult.response as Float64
            LogUtil.i("robotMileage:$response")
            RobotMileageHelper.robotMilePlus(response.data)
        }catch (e: Exception){
            LogUtil.i("robotMileage:${e.message}")
        }
    }
}