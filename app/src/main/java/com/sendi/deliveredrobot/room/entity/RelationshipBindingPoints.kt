package com.sendi.deliveredrobot.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *   @author: heky
 *   @date: 2021/9/3 15:06
 *   @describe: 点的绑定关系表
 *
 *   points_type: 0
 *      point1_id: 充电桩点
 *      point2_id: 充电桩出发准备点（退出自动回充后的点）
 *      point3_id:  充电桩远端重置点（自动回充失败后自动导航到的较远的一个点）
 *
 *   points_type: 1
 *      point1_id: 电梯内点
 *      point2_id: 电梯外点
 */
@Entity(tableName = "relationship_binding_points")
class RelationshipBindingPoints(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    @ColumnInfo(name = "point1_id") val pointOneId:Int?,
    @ColumnInfo(name = "point2_id") val pointTwoId:Int?,
    @ColumnInfo(name = "point3_id") val pointThreeId:Int?,
)
