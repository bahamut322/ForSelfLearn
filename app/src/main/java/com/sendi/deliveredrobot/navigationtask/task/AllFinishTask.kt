package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_CHARGING
import com.sendi.deliveredrobot.TYPE_IDLE
import com.sendi.deliveredrobot.TYPE_STAND_STILL
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 *   @author: heky
 *   @date: 2021/8/24 18:07
 *   @describe: 全流程结束（此时在充电桩上）
 */
class AllFinishTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.AllFinishTask
    }

    override suspend fun execute() {
        resetRobotStatus()
    }

    private fun resetRobotStatus() {
        RobotStatus.lowPowerBacking = false
        //机器人状态置为空闲
        if (taskModel?.bill !is StandStillTaskBill) {
            if (RobotStatus.currentStatus != TYPE_CHARGING) {
                RobotStatus.currentStatus = TYPE_IDLE
            }
        }
        BillManager.removeBill(taskModel?.bill)
    }
}