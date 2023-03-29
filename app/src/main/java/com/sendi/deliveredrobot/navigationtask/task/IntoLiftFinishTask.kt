package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:进电梯结束
 */
class IntoLiftFinishTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    var point: QueryPointEntity? = null

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
        point = dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
        taskModel?.location = point?.apply {
            binMark = taskModel?.location?.binMark?: 0x11
        }
        super.taskModel = taskModel
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.IntoLiftFinishTask
    }

    override suspend fun execute() {
        LiftHelper.resetTimer()
        // 释放开门
        LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
        // 重置呼叫电梯次数
        RobotStatus.callLiftAndMoveTimes = 0
        // 重置正在进入电梯状态
//        RobotStatus.enteringLift = false
        // 设置与电梯位置关系
        RobotStatus.outOfLift = false
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}