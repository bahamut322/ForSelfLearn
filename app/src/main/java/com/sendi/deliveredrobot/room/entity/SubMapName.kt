package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @describe 总图名字
 */
@Entity(tableName = "map_root")
open class SubMapName(
    @ColumnInfo(name = "name") open val name: String?,
): Serializable {
    override fun toString(): String {
        return "$name"
    }
}