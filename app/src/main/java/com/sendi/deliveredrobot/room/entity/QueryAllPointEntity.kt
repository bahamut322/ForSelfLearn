package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo

/**
 * @describe 按数据库结构定义的查询所有目标点
 */
class QueryAllPointEntity (
    @ColumnInfo(name= "name")val name:String?,
    @ColumnInfo(name = "x") val x: Float?,
    @ColumnInfo(name = "y") val y: Float?,
    @ColumnInfo(name = "w") var w: Double?,
    @ColumnInfo(name = "point_name") var pointName: String?,
    @ColumnInfo(name = "floor_name")val floorName:String?,

)
