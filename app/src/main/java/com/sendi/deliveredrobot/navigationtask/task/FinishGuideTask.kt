package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:结束引领（某一段，并非从起点到终点）
 */
class FinishGuideTask(var status:Int = 1, taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        taskDto.apply {
            status = status
            mileage = RobotMileageHelper.robotMileage()
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishGuideTask
    }

    override suspend fun execute() {
        BillManager.currentBill()?.executeNextTask()
    }
}