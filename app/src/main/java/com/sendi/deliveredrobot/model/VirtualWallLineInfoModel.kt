package com.sendi.deliveredrobot.model

import java.io.Serializable

/**
 * @author heky
 * @describe 虚拟墙LineInfo
 * @date 2022-02-25
 * @param pose line的点的集合
 * @param name lineName
 * @param state
 */
data class VirtualWallLineInfoModel(
    val pose:List<PointCompat>?,
    var name:String,
    var state:Int
):Serializable
