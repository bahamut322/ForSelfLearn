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
 * @describe:出电梯结束
 */
class OutLiftFinishTask(taskModel: TaskModel, val reset: Boolean = true) : AbstractTask(taskModel) {
    var point: QueryPointEntity? = null
    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
        point = dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_OUTSIDE,taskModel?.elevator?:"")
        taskModel?.location = point
        super.taskModel = taskModel
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.OutLiftFinishTask
    }

    override suspend fun execute() {
        LiftHelper.resetTimer()
        RobotStatus.currentLocation = RobotStatus.expectLocation
        RobotStatus.inLiftFlow = false
        RobotStatus.outOfLift = true
        if(reset){
            RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
            // 释放开门
            LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}