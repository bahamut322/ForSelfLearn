package com.sendi.deliveredrobot.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 目标点
 */
class SelectPointModel(
    val id: Int = 0,
    val name: String = "",
    val direction: String?,
    val x: Float?,
    val y: Float?,
    val w: Double?,
    val subMapId:Int?,
    val type:Int?,
    var selected:Boolean = false
)