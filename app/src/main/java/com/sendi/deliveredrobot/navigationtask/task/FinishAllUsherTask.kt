package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:结束迎宾
 */
class FinishAllUsherTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishUsherTask
    }

    override suspend fun execute() {
        BillManager.currentBill()?.executeNextTask()
    }
}