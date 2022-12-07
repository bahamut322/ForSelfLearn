package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 关系表-电梯点
 */
@Entity(tableName = "relationship_lift_point")
class RelationshipLiftPoint(
    @PrimaryKey(autoGenerate = true)val id:Int = 0,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int?,
    @ColumnInfo(name = "sub_map_id") val subMapId: Int?,
    @ColumnInfo(name = "point_id") val pointId: Int?
)
