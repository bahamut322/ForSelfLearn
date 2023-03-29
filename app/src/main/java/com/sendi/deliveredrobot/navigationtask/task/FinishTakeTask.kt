package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-09
 * @description 结束前往送物
 */
class FinishTakeTask(taskModel: TaskModel) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishRemoteOrderSendTask
    }

    override suspend fun execute() {
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}