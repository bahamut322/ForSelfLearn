package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_SEND
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import com.sendi.deliveredrobot.service.DoorEnum
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import java.util.*

/**
 * @author heky
 * @date 2022-08-29
 * @description 双仓送物-地点不同
 */
class DoubleDifferentSendTaskBillOne(private val taskModel: TaskModel?, date: Date) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
        val tempTaskId = TaskIdGenerator.getInstance()
            .generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR, date)
        setTaskId(tempTaskId)
        viewModelBin1.value.setBill(this)
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override fun billBuild(): List<ITaskBill> {
        return listOf(this)
    }

    override suspend fun earlyFinish() {
        for (task in taskQueue) {
            if (task is FinishSendTask) {
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
                break
            }
        }
        BillManager.clearBill(this)
    }

    override suspend fun exception() {
        for (task in taskQueue) {
            if (task is FinishSendTask) {
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
                break
            }
        }
        taskQueue.addAll(recreateQueue(taskModel))
        IdleGateDataHelper.reportIdleGateCount(0)
        RobotStatus.currentStatus = TYPE_EXCEPTION
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        // 双任务
//        RobotStatus.twoSamePlace = false
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(
                StartDoubleSendTask(
                    TaskModel(
                        location = taskModel?.location,
                        location2 = taskModel?.location2,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    ),
                    type = TYPE_SEND
                )
            )
            // step 10：到目的地task
            add(
                SendingTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
            // step 11：送物：呼叫客房task 引领：step 11
            add(
                CallRoomTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
            // step 12：完成任务task
            add(
                CallRoomFinishTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
            add(
                FinishSendTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleDifferentSendTaskBillOne
                    )
                )
            )
        }
        return tempQueue
    }

    private fun recreateQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        taskQueue.clear()
        return createTaskQueue(taskModel)
    }
}