package com.sendi.deliveredrobot.model

/**
 * @author swn
 * @describe 路线列表
 */
data class RouteList(
    val routeName : String?,
    val rootMapName : String?,
    val backgroundPic : String?,
    val introduction : String?,
    val timeStamp : Long,
    val pointConfigVOList : List<pointConfigVOList>

)
