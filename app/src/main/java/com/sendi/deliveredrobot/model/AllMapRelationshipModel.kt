package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.room.entity.RouteMap
import com.sendi.deliveredrobot.room.entity.SubMap

/**
 * @describe 总图绑定关系
 */
class AllMapRelationshipModel(
    var root_map_id :Int?,
    var root_map_name :String?,
    var mSubMap : SubMap,
    var selectRouteId :Int = -1,
    var selectRouteName :String = "",
    var routeMap : List<RouteMap>?,
    var mPoint : List<SelectPointModel>?,
    var selected:Boolean = false
)
