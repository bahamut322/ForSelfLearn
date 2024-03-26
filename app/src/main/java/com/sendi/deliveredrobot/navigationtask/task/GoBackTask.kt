package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_GO_BACK
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:开始返回
 */
class GoBackTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        RobotStatus.currentStatus = TYPE_GO_BACK
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GoBackTask
    }

    override suspend fun execute() {
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}