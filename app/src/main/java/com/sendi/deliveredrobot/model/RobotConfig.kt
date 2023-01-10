package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.RobotStatus
import java.util.*

/**
 * @author swn
 * @date 2022-12-17
 * @describe X8机器人配置
 */
data class RobotConfig (
//"type": "机器人配置 replyRobotConfig",
//"robotId": 机器人序列号,
//"audioType": 声音类型 0-女声（默认）,
//"wakeUpWord": "唤醒词",
//"sleep": "是否启用待机 1-启用 0-不启用",
//"sleepTime": "多少分钟没操作进入待机",
//"wakeUpType": "唤醒方式 1-点击屏幕，2-检测到人脸，3-唤醒词",
//"sleepType": "待机内容 1--表情组 2--图片 3--视频",
//"sleepContentName": "关联文件名称.jpg",
//"picType": "图片布局 1-全图（默认），2-铺平",
//"timeStamp": 时间戳ms
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
