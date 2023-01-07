package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.navigationtask.RobotStatus

/**
 * @author swn
 * @date 2022-12-17
 * @describe X8机器人门岗配置
 */
data class Gatekeeper(
//  "robotId": "机器人ID",
//  "temperatureThreshold": 38.9,
//  "bigScreenType": 4,
//  "pics": "3jpg,5.jpg,9.jpg,10.mp3",
//  "picPlayType": 1,
//  "picPlayTime": 10,
//  "videos": "2.mp3,4.mp3,10",
//  "videoFrame": "4",
//  "videoAudio": 1,
//  "fontContent": "屏幕显示的就是我",
//  "fontColor": 2,
//  "fontSize": 1,
//  "fontLayout": 1,
//  "fontBackGround": 1,
//  "tipsTemperatureInfo": "温度正常提示",
//  "tipsTemperatureWarn": "温度异常提示",
//  "tipsMaskWarn": "口罩异常提示",
//  "timeStamp": 1658713538416
    val robotId: String? ,
    val temperatureThreshold: Float? ,
    val bigScreenType: Int?,
    val pics: String? ,
    val picPlayType: Int? ,
    val picPlayTime: Int? ,
    val videos: String?,
    val videoFrame: Int?,
    val videoAudio: Int? ,
    val fontContent: String?,
    val fontColor: String? ,
    val fontSize: Int? ,
    val fontLayout: Int? ,
    val fontBackGround: String? ,
    val tipsTemperatureInfo: String? ,
    val tipsTemperatureWarn: String? ,
    val tipsMaskWarn: String? ,
    val timeStamp: Long?,
    val picType : Int? ,
    val textPosition : Int?

)
