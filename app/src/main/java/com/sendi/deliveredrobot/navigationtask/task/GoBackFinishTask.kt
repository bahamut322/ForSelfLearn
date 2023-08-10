package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:整个{返回}流程结束
 */
class GoBackFinishTask(taskModel: TaskModel?, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GoBackFinishTask
    }

    override suspend fun execute() {
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}