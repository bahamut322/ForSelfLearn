package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @date 2022-12-17
 * @describe X8机器人门岗配置
 */
data class Gatekeeper(
    val robotId: String?  = "",
    val temperatureThreshold: Float? = 0F ,
    val bigScreenType: Int? = 0,
    val pics: String? = "" ,
    val picPlayType: Int? = 0 ,
    val picPlayTime: Int? = 0 ,
    val videos: String? = "",
    val videoFrame: Int? = 0,
    val videoAudio: Int?= 0 ,
    val fontContent: String?= "",
    val fontColor: String? = "",
    val fontSize: Int? = 0,
    val fontLayout: Int?= 0 ,
    val fontBackGround: String? = "" ,
    val tipsTemperatureInfo: String?  = "",
    val tipsTemperatureWarn: String? = "",
    val tipsMaskWarn: String? = "" ,
    val timeStamp: Long? = 0,
    val picType : Int?= 0 ,
    val textPosition : Int?= 0

)
