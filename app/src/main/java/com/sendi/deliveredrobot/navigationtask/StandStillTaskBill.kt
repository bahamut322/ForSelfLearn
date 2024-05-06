package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

/**
 * @author heky
 * @date 2024-04-09
 * @description 原地不动任务清单
 */
class StandStillTaskBill(taskModel: TaskModel?, private val needNavigate: Boolean) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
        setTaskId(taskModel?.taskId ?: "")
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override fun billBuild(): List<ITaskBill>{
        return listOf(this)
    }

    override suspend fun earlyFinish() {
        BillManager.clearBill(this)
    }

    override suspend fun exception() {
//        IdleGateDataHelper.reportIdleGateCount(0)
        RobotStatus.currentStatus = TYPE_EXCEPTION
        earlyFinish()
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(StandStillTask(
                TaskModel(
                    taskId = taskId(),
                    bill = this@StandStillTaskBill
                ),
                needNavigate = needNavigate
            ))
            add(
                AllFinishTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@StandStillTaskBill
                    )
                )
            )
        }
        return tempQueue
    }
}