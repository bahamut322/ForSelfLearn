package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_SEND
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe 双仓送物任务开始
 * @author heky
 * @date 2021/11/15
 */
class StartDoubleSendTask(taskModel: TaskModel):AbstractTask(taskModel) {
    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }


    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            TYPE_SEND,
            taskModel = this@StartDoubleSendTask.taskModel?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_SEND
        IdleGateDataHelper.minusCount()
        IdleGateDataHelper.minusCount()
        IdleGateDataHelper.reportIdleGateCount()
//        TaskQueue.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}