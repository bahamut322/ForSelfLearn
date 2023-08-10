package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo

/**
 * @describe 按数据库结构定义的目标点
 */
class QueryPointEntity (
    @ColumnInfo(name= "public_area_id")var publicAreaId:Int?,
    @ColumnInfo(name = "root_map_id") var rootMapId: Int?,
    @ColumnInfo(name= "public_area_name")var publicAreaName:String?,
    @ColumnInfo(name = "point_id") var pointId: Int?,
    @ColumnInfo(name = "sub_map_id") var subMapId: Int?,
    @ColumnInfo(name = "route_id") var routeId: Int?,
    @ColumnInfo(name = "route_path")var routePath:String?,
    @ColumnInfo(name = "sub_path")var subPath:String?,
    @ColumnInfo(name = "x") var x: Float?,
    @ColumnInfo(name = "y") var y: Float?,
    @ColumnInfo(name = "w") var w: Double?,
    @ColumnInfo(name = "point_name") var pointName: String?,
    @ColumnInfo(name = "point_direction") var pointDirection: String?,
//    @ColumnInfo(name = "floor_code") val floorCode:Int?,
    @ColumnInfo(name = "floor_name")var floorName:String?,
    @ColumnInfo(name = "type")var type:Int?,
    @ColumnInfo(name = "elevator")var elevator: String?,
    var binMark:Int? = 0,
    var selected:Boolean? = false
){
    fun copy(): QueryPointEntity{
        return QueryPointEntity(
            publicAreaId = publicAreaId,
            rootMapId,
            publicAreaName,
            pointId,
            subMapId,
            routeId,
            routePath,
            subPath,
            x,
            y,
            w,
            pointName,
            pointDirection,
            floorName,
            type,
            elevator,
            binMark,
            selected
        )
    }
}