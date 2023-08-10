package com.sendi.deliveredrobot.model

import geometry_msgs.Pose2D
import java.io.Serializable

/**
 * @author heky
 * @describe LineInfo
 * @date 2022-02-25
 * @param pose line的点的集合
 * @param type 类型   0：坡道    1：其他
 * @param name lineName
 * @param radius 半径
 * @param speed 速度
 * @param visibleRange 可视半径
 * @param state
 * @param pose1 控制点
 * @param pose2 停靠点
 */
data class LineInfoModel(
    var pose:ArrayList<PointCompat>? = null,
    var type:Int? = 0,
    var name:String,
    var radius:Float? = 0f,
    var speed:Float? = 0f,
    var visibleRange:Float? = 0f,
    var state:Int? = -1,
    var pose1: Pose2D? = null,
    var pose2: Pose2D? = null
):Serializable
