package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_config")
class MapConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int?,
    @ColumnInfo(name = "charge_point_id")val chargePointId:Int?
)
