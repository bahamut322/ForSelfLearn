package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 路径地图
 */
@Entity(tableName = "map_route")
class RouteMap(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "path") val path: String?,
    @ColumnInfo(name= "sub_map_id")val subMapId:Int?
)