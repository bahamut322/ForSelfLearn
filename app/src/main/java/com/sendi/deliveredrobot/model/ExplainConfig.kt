package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe X8机器人讲解配置
 */
data class ExplainConfig(
    //{
    //  "type": "replyExplanationConfig",
    //  "slogan": "标语",
    //  "stayTime": 30,
    //  "routeListText": "首次进入讲解模式",
    //  "pointListText": "点击查看路线",
    //  "startText": "开始讲解",
    //  "endText": "讲解完成",
    //  "interruptionText": "中断讲解",
    //  "timeStamp": 123456789621
    //}
    val slogan: String?,
    val stayTime: Int?,
    val routeListText : String?,
    val pointListText : String?,
    val startText : String?,
    val endText : String?,
    val interruptionText : String?,
    val timeStamp : Long?,
    )
