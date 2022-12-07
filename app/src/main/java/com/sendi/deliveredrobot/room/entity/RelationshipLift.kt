package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   @author: heky
 *   @date: 2021/8/18 9:40
 *   @describe:
 */
@Entity(tableName = "relationship_lift")
class RelationshipLift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sub_map_id") var subMapId: Int?,
//    @ColumnInfo(name = "floor_code") var floorCode: Int?,
    @ColumnInfo(name = "floor_name") var floorName: String?

) {
    override fun toString(): String {
        return "RelationshipLift(id=$id, subMapId=$subMapId, floorName=$floorName)"
    }
}
