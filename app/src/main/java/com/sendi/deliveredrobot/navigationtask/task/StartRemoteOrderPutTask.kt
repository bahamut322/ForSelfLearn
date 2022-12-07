package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.PAGE_TYPE_PUT
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe 小程序下单{取物}任务开始
 * @author heky
 * @date 2022-06-07
 */
class StartRemoteOrderPutTask(taskModel: TaskModel, val type: Int):AbstractTask(taskModel) {

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            type,
            taskModel = this@StartRemoteOrderPutTask.taskModel?.apply {
                remoteOrderType = PAGE_TYPE_PUT
            }?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = type
        when (taskModel?.remoteOrderModel?.from?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                viewModelBin1.value.previousRemoteOrderPutFinished = false
                viewModelBin1.value.previousRemoteOrderSendFinished = false
            }
            viewModelBin2.value.binMarkBin2 -> {
                viewModelBin2.value.previousRemoteOrderPutFinished = false
                viewModelBin2.value.previousRemoteOrderSendFinished = false
            }
        }
//        TaskQueue.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}