package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.model.IdleGateDataModel
import com.sendi.deliveredrobot.service.CloudMqttService

/**
 * @author heky
 * @date 2022-06-08
 * @description 空仓管理类
 */
object IdleGateDataHelper {
    var idleGateCount: Int = 2
        private set

    fun addCount(): Boolean{
        if(idleGateCount < 2){
            idleGateCount++
            return true
        }
        return false
    }

    fun minusCount(): Boolean{
        if(idleGateCount > 0){
            idleGateCount--
            return true
        }
        return false
    }

    fun reportIdleGateCount(
        num: Int = idleGateCount,
        orderId: String = "",
        accept: Boolean? = null,
        reportType: Int = 1
    ) {
//        CloudMqttService.publish(
//            IdleGateDataModel(
//                num = num,
//                orderId = orderId,
//                accept = accept,
//                reportType = reportType
//            ).toString()
//        )
    }
}