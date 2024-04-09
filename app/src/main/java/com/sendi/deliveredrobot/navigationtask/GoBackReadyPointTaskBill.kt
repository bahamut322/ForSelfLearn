package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_GO_BACK
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

/**
 * @author heky
 * @date 2022-08-25
 * @description 返回任务清单
 */
class GoBackReadyPointTaskBill(taskModel: TaskModel?) : AbstractTaskBill(taskModel) {
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
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackReadyPointTaskBill
                    )
                )
            )
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackReadyPointTaskBill
                    ),
                    TYPE_GO_BACK
                )
            )
            add(
                GuidingTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackReadyPointTaskBill
                    ),
                    navigateId = R.id.goBackFragment
                )
            )
            add(
                FinishGoBackReadyPointTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackReadyPointTaskBill
                    )
                )
            )
            add(
                AllFinishTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoBackReadyPointTaskBill
                    )
                )
            )
        }
        return tempQueue
    }
}