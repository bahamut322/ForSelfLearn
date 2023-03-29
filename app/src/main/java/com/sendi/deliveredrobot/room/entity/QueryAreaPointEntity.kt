package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo

class QueryAreaPointEntity (
    @ColumnInfo(name= "public_area_id")val id:Int?,
//    @ColumnInfo(name = "public_area_name") val publicName: String?,
    @ColumnInfo(name = "point_name") val name: String?,
//    @ColumnInfo(name = "name") var w: Double?,
)