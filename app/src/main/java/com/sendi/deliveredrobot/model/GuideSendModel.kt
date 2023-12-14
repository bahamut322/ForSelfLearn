package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.entity.entitySql.QuerySql

/**
 * @Author Swn
 * @Data 2023/11/13
 * @describe 用于发送引领配置
 */
data class GuideSendModel (
    val baseTimeStamp : Long? = QuerySql.sendGuideTimeStamp() ?: 0,
    val maps : List<MapConfig?>? = null
)
data class MapConfig(
    val mapName : String? = "",
    val mapTimeStamp : Long? = 0
)