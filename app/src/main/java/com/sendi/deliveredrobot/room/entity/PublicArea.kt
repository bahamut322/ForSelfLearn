package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @describe 公共区域
 */
@Entity(tableName = "public_area")
class PublicArea(
    @PrimaryKey(autoGenerate = true) val id:Int=0,
    @ColumnInfo(name = "name")val name:String?,
    @ColumnInfo(name = "type")val type:Int?
)
