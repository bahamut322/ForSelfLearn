package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.CallRoomFinishTask
import com.sendi.deliveredrobot.navigationtask.task.FinishSendTask
import com.sendi.deliveredrobot.navigationtask.task.StartDoubleSecondSendTask
import com.sendi.deliveredrobot.service.DoorEnum
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import java.util.*

/**
 * @author heky
 * @date 2022-08-29
 * @description 双仓送物-地点相同
 */
class DoubleSameSendTaskBillTwo(private val taskModel: TaskModel?, date: Date): AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location2?.pointName ?: "")
        val tempTaskId = TaskIdGenerator.getInstance()
            .generateTaskId(TaskTypeEnum.DELIVERY, DoorEnum.SECOND_DOOR, date)
        setTaskId(tempTaskId)
        viewModelBin2.value.setBill(this)
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
                StartDoubleSecondSendTask(
                    TaskModel(
                        taskModel?.location2,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillTwo
                    )
                )
            )
            add(
                CallRoomFinishTask(
                    TaskModel(
                        location = taskModel?.location2,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillTwo
                    )
                )
            )
            add(
                FinishSendTask(
                    TaskModel(
                        location = taskModel?.location2,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@DoubleSameSendTaskBillTwo
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