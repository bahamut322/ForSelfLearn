package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 关系表-目标点
 */
@Entity(tableName = "relationship_point")
class RelationshipPoint(
    @PrimaryKey(autoGenerate = true) val id: Int= 0,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int?,
    @ColumnInfo(name = "sub_map_id") val subMapId: Int?,
    @ColumnInfo(name = "route_id") val routeId: Int?,
    @ColumnInfo(name = "point_id") val pointId: Int?
)

