package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 结束业务办理（某一段，并非从起点到终点）
 */
class FinishBusinessTask (var status:Int = 1, taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        taskDto.apply {
            status = status
            mileage = RobotMileageHelper.robotMileage()
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishBusinessTask
    }

    override suspend fun execute() {
        Universal.speakInt = 0
        BillManager.currentBill()?.executeNextTask()
    }
}