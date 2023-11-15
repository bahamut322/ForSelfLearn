package com.sendi.deliveredrobot.model

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe
 */
data class GuideSendModel (
    val maps : List<MapConfig?>? = null
)
data class MapConfig(
    val mapName : String? = "",
    val mapTimeStamp : Long? = 0
)