package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.RobotStatus
import java.util.*

/**
 * @author swn
 * @describe X8机器人配置
 * type： "机器人配置 replyRobotConfig",
 * audioType： 声音类型 0-女声（默认）,
 * wakeUpWord： "唤醒词",
 * sleep： "是否启用待机 1-启用 0-不启用",
 * sleepTime： "多少分钟没操作进入待机",
 * wakeUpList： "唤醒方式 1-点击屏幕，2-检测到人脸，3-唤醒词",
 * timeStamp： 时间戳ms
 * mapName：当前选择地图名称
 * password：机器人密码
 * argConfig：配置类实体
 */
data class RobotConfig  (
//val robotId : String? = RobotStatus.SERIAL_NUMBER,
val audioType : Int? = 1,
val wakeUpWord :String? = " ",
val sleep : Int? = 1,
val sleepTime : Int? = 10,
val wakeUpList : String? = "1",
//val sleepType : Int? = 1,
//val sleepContentName : String? = "",
//val picType : Int? = 1,
val timeStamp : Long? = Date().time,
val mapName : String? = "",
val password : String? = "",
val argConfig : TopLevelConfig?
)
