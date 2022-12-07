package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo

/**
 * @describe 按数据库结构定义的目标点
 */
class QueryPointEntity (
    @ColumnInfo(name= "public_area_id")val publicAreaId:Int?,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int?,
    @ColumnInfo(name= "public_area_name")val publicAreaName:String?,
    @ColumnInfo(name = "point_id") val pointId: Int?,
    @ColumnInfo(name = "sub_map_id") val subMapId: Int?,
    @ColumnInfo(name = "route_id") val routeId: Int?,
    @ColumnInfo(name = "route_path")val routePath:String?,
    @ColumnInfo(name = "sub_path")val subPath:String?,
    @ColumnInfo(name = "x") val x: Float?,
    @ColumnInfo(name = "y") val y: Float?,
    @ColumnInfo(name = "w") var w: Double?,
    @ColumnInfo(name = "point_name") var pointName: String?,
    @ColumnInfo(name = "point_direction") val pointDirection: String?,
//    @ColumnInfo(name = "floor_code") val floorCode:Int?,
    @ColumnInfo(name = "floor_name")val floorName:String?,
    @ColumnInfo(name = "type")val type:Int?,
    @ColumnInfo(name = "elevator")val elevator: String?,
    var binMark:Int? = 0,
    var selected:Boolean? = false
)