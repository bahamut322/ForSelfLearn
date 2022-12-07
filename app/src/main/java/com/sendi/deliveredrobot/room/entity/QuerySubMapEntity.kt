package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo

/**
 * @describe 子地图和绑定的楼层
 */
class QuerySubMapEntity(
    id: Int = 0,
    name: String?,
    path: String?,
    @ColumnInfo(name = "floor_code")val floorCode: Int?,
    @ColumnInfo(name = "floor_name")val floorName: String?,
    limitSpeed: Int? = 0,
    virtualWall: Int? = 0,
    oneWay: Int? = 0
): SubMap(id, name, path, limitSpeed, virtualWall, oneWay) {
    override fun toString(): String {
        return "SubMap(id=$id, name=$name, path=$path, floor_code=$floorCode, floor_name=$floorName)"
    }
}
