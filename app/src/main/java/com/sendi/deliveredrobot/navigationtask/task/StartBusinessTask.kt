package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_BUSINESS
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理任务开始
 */
class StartBusinessTask (taskModel: TaskModel) : AbstractTask(taskModel) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            TYPE_BUSINESS, this@StartBusinessTask.taskModel?: TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_BUSINESS
//        IdleGateDataHelper.reportIdleGateCount()
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}