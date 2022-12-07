package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_GO_BACK
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

/**
 * @author heky
 * @date 2022-08-25
 * @description 返回任务清单
 */
class GoBackTaskBill(taskModel: TaskModel?) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(RobotStatus.originalLocation?.pointName ?: "")
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
        IdleGateDataHelper.reportIdleGateCount(0)
        RobotStatus.currentStatus = TYPE_EXCEPTION
        earlyFinish()
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            //step 13: 回到充电桩
            add(
                GoBackTask(
                    TaskModel(
                        location = RobotStatus.originalLocation!!,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    )
                )
            )
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = RobotStatus.originalLocation!!,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    ),
                    TYPE_GO_BACK
                )
            )
            add(
                AdvanceGuidingTask(
                    cmd = 1,
                    taskModel = TaskModel(
                        location = RobotStatus.originalLocation,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    ),
                    navigateId = R.id.goBackFragment
                )
            )
            add(
                GoBackFinishTask(
                    TaskModel(
                        location = RobotStatus.originalLocation,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    )
                )
            )
            add(
                BeginDockTask(
                    TaskModel(
                        location = RobotStatus.originalLocation,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    )
                )
            )
            add(
                FinishDockTask(
                    TaskModel(
                        location = RobotStatus.originalLocation,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    )
                )
            )
            add(
                AllFinishTask(
                    TaskModel(
                        location = RobotStatus.originalLocation,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackTaskBill
                    )
                )
            )
        }
        return tempQueue
    }

    fun dockFail(): LinkedList<AbstractTask>{
        val tempQueue = createTaskQueue(null)
        val outDockTask = OutDockTask(
            TaskModel(
                location = RobotStatus.originalLocation,
                endTarget = endTarget(),
                taskId = taskId(),
                bill = this@GoBackTaskBill
            )
        )
        val navToFarPointTask = NavToFarPointTask(
            TaskModel(
                location = RobotStatus.originalLocation,
                endTarget = endTarget(),
                taskId = taskId(),
                bill = this@GoBackTaskBill
            )
        )
        taskQueue.apply {
            clear()
            add(outDockTask)
            add(navToFarPointTask)
            addAll(tempQueue)
        }
        return tempQueue
    }
}