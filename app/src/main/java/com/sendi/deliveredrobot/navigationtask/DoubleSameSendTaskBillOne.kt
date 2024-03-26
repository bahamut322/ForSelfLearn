package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_SEND
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
 * @description 双仓送物-地点相同
 */
class DoubleSameSendTaskBillOne(private val taskModel: TaskModel?, date: Date): AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
//        setEndTarget2(taskModel?.location2?.pointName ?: "")
        val tempTaskId = TaskIdGenerator.getInstance()
            .generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.FIRST_DOOR, date)
//        val tempTaskId2 = TaskIdGenerator.getInstance()
//            .generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR, date)
        setTaskId(tempTaskId)
//        setTaskId2(tempTaskId2)
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
        RobotStatus.currentStatus = TYPE_EXCEPTION
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
//        RobotStatus.twoSamePlace = true
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(
                StartDoubleSendTask(
                    TaskModel(
                        location = taskModel?.location,
                        location2 = taskModel?.location2,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillOne
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillOne
                    )
                )
            )
            // 两个任务为同一地点
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillOne
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
                        bill = this@DoubleSameSendTaskBillOne
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
                        bill = this@DoubleSameSendTaskBillOne
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
                        bill = this@DoubleSameSendTaskBillOne
                    )
                )
            )
            add(
                FinishSendTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillOne
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