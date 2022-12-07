package com.sendi.deliveredrobot.model

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
 */
data class LineInfoModel(
    val pose:List<PointCompat>? = null,
    var type:Int? = 0,
    var name:String,
    var radius:Float? = 0f,
    var speed:Float? = 0f,
    var visibleRange:Float? = 0f,
    var state:Int? = -1
):Serializable
