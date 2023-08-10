package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * @describe 主地图
 */

class MyRootMap(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String?,
    var selected: Boolean?
)

