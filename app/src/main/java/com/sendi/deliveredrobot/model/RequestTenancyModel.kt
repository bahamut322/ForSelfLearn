package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-04-14
 * @describe 查询租期request
 */
data class RequestTenancyModel(
    val type: String = "robotQueryTenancy"
){
    override fun toString(): String {
        return "{\"type\":\"$type\"}"
    }
}
