package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * @describe 关系表-目标点
 */
class MyRelationshipPoint(
    @PrimaryKey(autoGenerate = true) var id: Int= 0,
    @ColumnInfo(name = "root_map_id") var rootMapId: Int? = -1,
    @ColumnInfo(name = "sub_map_id") var subMapId: Int? = -1,
    @ColumnInfo(name = "route_id") var routeId: Int? = -1,
    @ColumnInfo(name = "point_id") var pointId: Int? = -1,
    @ColumnInfo(name = "root_map_name") var rootMapName: String? = "",
    @ColumnInfo(name = "route_name") var routeName: String? = ""
){
    @Ignore
    constructor() : this(0,-1,-1,-1,-1,"","")
}

