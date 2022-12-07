package com.sendi.deliveredrobot.model

/**
 * @describe:高德IP定位
 * https://restapi.amap.com/v3/ip
 * {"status":"1","info":"OK","infocode":"10000","province":"广东省","city":"广州市","adcode":"440100","rectangle":"113.1017375,22.93212254;113.6770499,23.3809537"}
 */
data class GLocationModel(
    var status: String="",
    var info: String="",
    var infocode: String="",
    var province: String= "",
    var city: String = "",
    var adcode: String = "",
    var rectangle: String=""
)