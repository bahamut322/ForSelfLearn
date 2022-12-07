package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 目标点
 */
@Entity(tableName = "map_point")
class Point(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "direction") var direction: String?,
    @ColumnInfo(name = "x") var x: Float?,
    @ColumnInfo(name = "y") var y: Float?,
    @ColumnInfo(name = "w") var w: Double?,
    @ColumnInfo(name= "sub_map_id")var subMapId:Int?,
    @ColumnInfo(name = "type")var type:Int?,
    @ColumnInfo(name = "elevator")var elevator:String?
)