package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 路线列表
 */
data class RouteList(
    val routeName : String? = null,
    val rootMapName : String? = null,
    val backgroundPic : String? = null,
    val introduction : String? = null,
    val timeStamp : Long,
    val pointConfigVOList : List<pointConfigVOList>

)
