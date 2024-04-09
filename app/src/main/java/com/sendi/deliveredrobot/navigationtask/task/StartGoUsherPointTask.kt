package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_WELCOME
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe 去往迎宾点任务开始
 * @author heky
 * @date 2023-03-01
 */
class StartGoUsherPointTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            TYPE_WELCOME, this@StartGoUsherPointTask.taskModel?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_WELCOME
//        IdleGateDataHelper.reportIdleGateCount(0)
//        TaskQueue.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}