package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @Author Swn
 * @describe 结束讲解（某一段，并非从起点到终点）
 * @Data 2023-04-24 16:12
 */
class FinishExplainTask (var status:Int = 1, taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        taskDto.apply {
            status = status
            mileage = RobotMileageHelper.robotMileage()
        }
    }

    override fun reportTaskDto() {
        if (BillManager.billList().size<=1) {
            super.reportTaskDto()
        }
    }

    override fun configEnum(): TaskStageEnum {
            return TaskStageEnum.FinishExplainTask
    }

    override suspend fun execute() {
        SafeStateTopic.resetSafeStateListener()
        BillManager.currentBill()?.executeNextTask()
    }
}