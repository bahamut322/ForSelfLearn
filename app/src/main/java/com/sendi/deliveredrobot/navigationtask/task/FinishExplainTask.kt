package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @Author Swn
 * @describe 结束讲解（某一段，并非从起点到终点）
 * @Data 2023-04-24 16:12
 */
class FinishExplainTask (var status:Int = 1, taskModel: TaskModel) : AbstractTask(taskModel) {

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