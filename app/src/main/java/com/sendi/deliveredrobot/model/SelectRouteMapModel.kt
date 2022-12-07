package com.sendi.deliveredrobot.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 路径地图
 */
class SelectRouteMapModel(
    val id: Int = 0,
    val name: String?,
    val path: String?,
    val subMapId:Int?,
    var selected:Boolean? = false
)