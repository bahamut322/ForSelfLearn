package com.sendi.deliveredrobot.helpers

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sendi.deliveredrobot.model.RemoteOrderModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RemoteOrderPutBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @author heky
 * @date 2022-06-08
 * @description
 */
object RemoteOrderHelper {
    val mutex = Mutex()
    val mainScope = MainScope()

    fun receiveRemoteOrder(jsonObject: JsonObject){
        var remoteOrderModel: RemoteOrderModel? = null
        try{
            // 包装实体，创建任务链
            val from = Gson().fromJson(jsonObject.get("from"),QueryPointEntity::class.java)
            val fromName = jsonObject.get("fromName").asString
            val fromPhone = jsonObject.get("fromPhone").asString
            val to = Gson().fromJson(jsonObject.get("to"),QueryPointEntity::class.java)
            val toName = jsonObject.get("toName").asString
            val toPhone = jsonObject.get("toPhone").asString
            val store = jsonObject.get("store").asString
            val taskId = jsonObject.get("taskId").asString
            val taskType = jsonObject.get("taskType").asString
            val remarks = jsonObject.get("remarks").asString
            remoteOrderModel = RemoteOrderModel(
                from = from,
                fromName = fromName,
                fromPhone = fromPhone,
                to = to,
                toName = toName,
                toPhone = toPhone,
                store = store,
                taskId = taskId,
                taskType = taskType,
                remarks = remarks
            )
        }finally {
            if(RobotStatus.originalLocation == null){
                ToastUtil.show("创建远程任务失败，充电点为null")
                LogUtil.i("创建远程任务失败，充电点为null")
                IdleGateDataHelper.reportIdleGateCount(
                    orderId = remoteOrderModel?.taskId?:"",
                    accept = false,
                    reportType = 2
                )
            }else{
                if(remoteOrderModel != null){
                    mainScope.launch {
                        mutex.withLock {
//                        val result = TaskQueueFactory.createTask(remoteOrderModel)
                            val billList = RemoteOrderPutBillFactory.createBill(taskModel = TaskModel(
                                remoteOrderModel = remoteOrderModel))
                            val result = billList.isEmpty()
                            if(result){
                                ToastUtil.show("创建远程任务失败，没有空闲仓体")
                                LogUtil.i("创建远程任务失败，没有空闲仓体")
                            }else{
                                RemoteOrderPutBillFactory.addBillToQueue(billList)
                            }
                            IdleGateDataHelper.reportIdleGateCount(
                                orderId = remoteOrderModel.taskId,
                                accept = !result,
                                reportType = 2
                            )
                        }
                    }
                }else{
                    ToastUtil.show("创建远程任务失败，解析错误")
                    LogUtil.i("创建远程任务失败，解析错误")
                    IdleGateDataHelper.reportIdleGateCount(
                        orderId = remoteOrderModel?.taskId?:"",
                        accept = false,
                        reportType = 2
                    )
                }
            }
        }
    }
}