package com.sendi.deliveredrobot.model

/**
 * @author heky
 * @date 2022-04-14
 * @describe 查询租期Response
 */

data class ResponseTenancyModel(
    val useType: Int?, // -1-未出厂 0-永久 1-租赁 2-试用
    val days: Int?,
    val deadline: String?,
    val robotName: String?
)
