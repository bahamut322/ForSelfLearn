package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe X8机器人门岗配置
 * type： 门岗配置
 * temperatureThreshold： float '温度阈值',
 * tipsTemperatureInfo： (255) '温度正常提示',
 * tipsTemperatureWarn： (255) '温度异常提示',
 * tipsMaskWarn： (255) '口罩异常提示',
 * timeStamp： `配置生成时间戳`
 * argConfig：配置类实体
 */
data class Gatekeeper(

    val temperatureThreshold: Float? = 0F,
    val tipsTemperatureInfo: String? = "",
    val tipsTemperatureWarn: String? = "",
    val tipsMaskWarn: String? = "",
    val timeStamp: Long? = 0,
    val argConfig: TopLevelConfig? = null

)
