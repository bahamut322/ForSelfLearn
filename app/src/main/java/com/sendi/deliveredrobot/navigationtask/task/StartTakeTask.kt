package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-09
 * @description 开始前往送物
 */
class StartTakeTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.StartRemoteOrderSendTask
    }

    override suspend fun execute() {
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}