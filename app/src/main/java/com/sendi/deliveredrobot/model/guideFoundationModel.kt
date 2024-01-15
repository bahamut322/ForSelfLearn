package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/11/14
 * @describe
 */
data class guideFoundationModel (
    val firstPrompt : String? = "",
    val movePrompt : String? = "",
    val arrivePrompt : String? = "",
    val interruptPrompt : String? = "",
    //大屏配置
    var bigScreenConfig: TopLevelConfig? = null,
    //小屏配置
    var touchScreenConfig: TopLevelConfig? = null,

    val timeStamp : Long? = 0
)