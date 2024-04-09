package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe: 小程序下单{送物}结束
 */
class FinishRemoteOrderSendTask(taskModel: TaskModel, val type: Int, var exceptioned: Boolean = false, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                taskDto.status = when (viewModelBin1.value.previousRemoteOrderSendFinished) {
                    true -> {
                        1
                    }// 成功
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
                taskDto.status = when (viewModelBin2.value.previousRemoteOrderSendFinished) {
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
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                viewModelBin1.value.resetBill()
                when (viewModelBin1.value.previousRemoteOrderSendFinished) {
                    true -> {
                    }// 成功
                    false -> {
                        // 失败
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                viewModelBin2.value.resetBill()
                when (viewModelBin2.value.previousRemoteOrderSendFinished) {
                    true -> {
                        // 成功
                    }
                    false -> {
                        // 失败
                    }
                }
            }
        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }

}