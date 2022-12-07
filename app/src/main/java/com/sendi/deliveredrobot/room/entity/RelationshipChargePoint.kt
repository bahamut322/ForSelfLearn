package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   @author: heky
 *   @date: 2021/8/6 9:58
 *   @describe: 关系-充电桩
 */
@Entity(tableName = "relationship_charge_point")
class RelationshipChargePoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int,
    @ColumnInfo(name = "sub_map_id") val subMapId: Int,
    @ColumnInfo(name = "point_id") val pointId: Int
)