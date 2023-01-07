package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

/**
 * @describe 子地图名字
 */
@Entity(tableName = "map_sub")
open class SubMapName(
    @ColumnInfo(name = "name") open val name: String?,
): Serializable {
    override fun toString(): String {
        return "$name"
    }
}