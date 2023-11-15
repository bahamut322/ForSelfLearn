package com.sendi.deliveredrobot.entity

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 引领点的配置
 */
data class GuideConfig(
    var mapTimeStamp : Long? = 0,
    var mapName : String? = "",
    var pointList : List<GuidePointPicDB?>? = null
)
