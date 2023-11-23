package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 用于发送引领配置
 */
data class GuideSendModel (
    val baseTimeStamp : Long? = 0,
    val maps : List<MapConfig?>? = null
)
data class MapConfig(
    val mapName : String? = "",
    val mapTimeStamp : Long? = 0
)