package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import java.util.*

/**
 * @author heky
 * @date 2022-06-22
 * @description 扫描电梯门
 */
class ScanLiftTask(taskModel: TaskModel): AbstractTask(taskModel) {
    private val step = 2000L
    private val holdTime = 5

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ScanIntoLiftTask
    }

    override suspend fun execute() {
        val currentAxis = dao.queryLiftPoint(RobotStatus.currentLocation?.subMapId?:-1, PointType.LIFT_OUTSIDE,taskModel?.elevator?:"")
        val targetAxis = dao.queryLiftPoint(RobotStatus.currentLocation?.subMapId?:-1, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
        LiftHelper.timer.schedule(object : TimerTask() {
            override fun run() {
                if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) {
                    return
                }
                LiftHelper.sendLift(
                    control = 1,
//                    floorIndex = RobotCommand.LIFT_RELEASE_CONTROL_DOOR,
                    floorName = "开门",
                    time = holdTime,
                    elevator = ""
                )
            }
        }, Date(), step)
        val start = System.currentTimeMillis()
        var result: Boolean
        while (true){
            result = ROSHelper.scanLift(currentAxis = currentAxis, targetAxis = targetAxis)
            if(result) break // 扫描成功，跳出
            val end = System.currentTimeMillis()
            val second = (end - start) > 10000
            if(second) break // 大于10秒，跳出
            virtualTaskExecuteFloat(0.5f, "ScanLift")
        }
        if (!result) {
            LiftHelper.resetTimer()
            //释放梯门
            LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
            val tempList = addRetryIntoLiftQueue()
            taskModel?.bill?.addAll(0,tempList)
            ToastUtil.show("扫描失败，20s后重试")
            LogUtil.i("扫描失败，20s后重试")
            virtualTaskExecute(20, "扫描失败，20s后重试")
        }
//        TaskQueues.executeNextTask()
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }

    private fun addRetryIntoLiftQueue(): LinkedList<AbstractTask>{
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(
                CallLiftTask(
                    TaskModel(
                        RobotStatus.currentLocation,
                        endTarget = taskModel?.bill?.endTarget()?:"",
                        taskId = taskModel?.taskId?:"",
                        bill = taskModel?.bill,
                        elevator = taskModel?.elevator?:""
                    )
                )
            )
            add(
                ScanLiftTask(
                    TaskModel(
                        location = RobotStatus.currentLocation,
                        endTarget = taskModel?.bill?.endTarget()?:"",
                        taskId = taskModel?.taskId?:"",
                        bill = taskModel?.bill,
                        elevator = taskModel?.elevator?:""
                    )
                ))
        }
        return tempQueue
    }
}