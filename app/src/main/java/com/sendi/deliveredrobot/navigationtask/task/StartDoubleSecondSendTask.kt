package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_SEND
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe 双仓第二个仓送物任务开始
 * @author heky
 * @date 2021/11/15
 */
class StartDoubleSecondSendTask(taskModel:TaskModel, needReportData: Boolean = true):AbstractTask(taskModel, needReportData) {


    override suspend fun beforeReportData(taskDto: TaskDto) {
        RobotStatus.currentStatus = TYPE_SEND
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}