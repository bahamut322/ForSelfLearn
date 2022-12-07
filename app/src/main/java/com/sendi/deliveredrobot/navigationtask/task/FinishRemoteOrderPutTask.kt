package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe: 小程序下单{放物}结束
 */
class FinishRemoteOrderPutTask(taskModel: TaskModel, val type: Int, var exceptioned: Boolean = false) : AbstractTask(taskModel) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                taskDto.status = when (viewModelBin1.value.previousRemoteOrderPutFinished) {
                    true -> {
                        1
                    }
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                taskDto.status = when (viewModelBin2.value.previousRemoteOrderPutFinished) {
                    true -> {
                        1
                    }
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishSendTask
    }

    override suspend fun execute() {
        // 如果放物成功, 则动态生成送location2的任务链，如果失败，则直接执行下一次任务
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                viewModelBin1.value.resetBill()
                when (viewModelBin1.value.previousRemoteOrderPutFinished) {
                    true -> {
//                        TaskQueueFactory.
//                        createRemoteOrderSendTask(
//                            remoteOrderModel = taskModel!!.remoteOrderModel!!,
//                            type = type
//                        )
                        val tempList = RemoteOrderSendBillFactory.createBill(TaskModel(remoteOrderModel = taskModel?.remoteOrderModel), type = type)
                        BillManager.addAllAtIndex(tempList, 1)
                    }
                    false -> {
                        IdleGateDataHelper.addCount()
                        IdleGateDataHelper.reportIdleGateCount()
                        viewModelBin1.value.previousRemoteOrderPutFinished = true
                        viewModelBin1.value.previousRemoteOrderSendFinished = true
//                        TaskQueue.queue.add(0,AllFinishSendTask(taskModel = taskModel!!))
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                viewModelBin2.value.resetBill()
                when (viewModelBin2.value.previousRemoteOrderPutFinished) {
                    true -> {
//                        TaskQueueFactory.createRemoteOrderSendTask(
//                            remoteOrderModel = taskModel!!.remoteOrderModel!!,
//                            type = type
//                        )
                        val tempList = RemoteOrderSendBillFactory.createBill(TaskModel(remoteOrderModel = taskModel?.remoteOrderModel), type = type)
                        BillManager.addAllAtIndex(tempList, 1)
                    }
                    false -> {
                        IdleGateDataHelper.addCount()
                        IdleGateDataHelper.reportIdleGateCount()
                        viewModelBin2.value.previousRemoteOrderPutFinished = true
                        viewModelBin2.value.previousRemoteOrderSendFinished = true
//                        TaskQueue.queue.add(0,AllFinishSendTask(taskModel = taskModel!!))
                    }
                }
            }
        }
        BillManager.currentBill()?.executeNextTask()
    }
}

