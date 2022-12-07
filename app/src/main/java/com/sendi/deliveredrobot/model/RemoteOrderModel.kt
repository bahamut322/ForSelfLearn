package com.sendi.deliveredrobot.model

import com.sendi.deliveredrobot.room.entity.QueryPointEntity

/**
 * @author heky
 * @date 2022-06-08
 * @description 小程序下单
 */
data class RemoteOrderModel(
    val from: QueryPointEntity,
    val fromName: String,
    val fromPhone: String,
    val to: QueryPointEntity,
    val toName: String,
    val toPhone: String,
    val store: String,
    val taskId: String,
    val taskType: String,
    val remarks: String
)