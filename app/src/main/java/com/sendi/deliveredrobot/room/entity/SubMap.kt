package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * @describe 子地图
 */
@Entity(tableName = "map_sub")
open class SubMap(
    @PrimaryKey(autoGenerate = true) open val id: Int = 0,
    @ColumnInfo(name = "name") open val name: String?,
    @ColumnInfo(name = "path") open val path: String?,
    @ColumnInfo(name = "limit_speed") var limitSpeed: Int? = 0,
    @ColumnInfo(name = "virtual_wall") var virtualWall: Int? = 0,
    @ColumnInfo(name = "one_way") var oneWay: Int? = 0
): Serializable {
    override fun toString(): String {
        return "SubMap(id=$id, name=$name, path=$path, limit_speed=$limitSpeed, virtual_wall=$virtualWall, one_way=$oneWay)"
    }
}
