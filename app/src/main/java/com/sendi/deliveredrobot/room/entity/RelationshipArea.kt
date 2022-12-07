package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 关系表-公共区域
 */
@Entity(tableName = "relationship_area")
class RelationshipArea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "root_map_id") val rootMapId: Int?,
    @ColumnInfo(name = "public_area_id") val publicAreaId: Int?,
    @ColumnInfo(name = "point_id") val pointId: Int?
)
