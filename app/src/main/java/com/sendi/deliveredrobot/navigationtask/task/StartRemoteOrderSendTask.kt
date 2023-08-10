package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.PAGE_TYPE_TAKE
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe 小程序下单{送物}任务开始
 * @author heky
 * @date 2022-06-07
 */
class StartRemoteOrderSendTask(taskModel: TaskModel, val type: Int, needReportData: Boolean = true):AbstractTask(taskModel, needReportData) {

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            type,
            taskModel = this@StartRemoteOrderSendTask.taskModel?.apply {
                remoteOrderType = PAGE_TYPE_TAKE
            }?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = type
        when (taskModel?.remoteOrderModel?.to?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                viewModelBin1.value.setBill(taskModel?.bill)
            }
            viewModelBin2.value.binMarkBin2 -> {
                viewModelBin2.value.setBill(taskModel?.bill)
            }
        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}