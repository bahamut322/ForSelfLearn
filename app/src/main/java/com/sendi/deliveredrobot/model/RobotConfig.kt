package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.RobotStatus
import java.util.*

/**
 * @author swn
 * @date 2022-12-17
 * @describe X8机器人配置
 */
data class RobotConfig (
val robotId : String? = RobotStatus.SERIAL_NUMBER,
val audioType : Int? = 1,
val wakeUpWord :String? = " ",
val sleep : Int? = 1,
val sleepTime : Int? = 10,
val wakeUpList : String? = "1",
val sleepType : Int? = 1,
val sleepContentName : String? = "",
val picType : Int? = 1,
val timeStamp : Long? = Date().time,
val mapName : String? = "",
val password : String? = ""
)
