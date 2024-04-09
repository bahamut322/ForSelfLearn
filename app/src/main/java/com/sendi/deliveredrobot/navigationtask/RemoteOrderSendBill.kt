package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_SEND
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

class RemoteOrderSendBill(private val taskModel: TaskModel?, private val type: Int): AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.remoteOrderModel?.to?.pointName ?: "")
        setTaskId(taskModel?.remoteOrderModel?.taskId?:"")
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override suspend fun earlyFinish() {
        for (task in taskQueue) {
            if (task is CallTakeObjectFinishTask) {
                task.exceptioned = true
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
            }
            if (task is FinishRemoteOrderSendTask) {
                task.exceptioned = true
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
            if (task is CallTakeObjectFinishTask) {
                task.exceptioned = true
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
            }
            if (task is FinishRemoteOrderSendTask) {
                task.exceptioned = true
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
                break
            }
        }
        taskQueue.addAll(recreateQueue(taskModel))
//        IdleGateDataHelper.reportIdleGateCount(0)
        RobotStatus.currentStatus = TYPE_EXCEPTION
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        val tempList = LinkedList<AbstractTask>()
        tempList.apply {
            add(0, StartRemoteOrderSendTask(
                TaskModel(
                    remoteOrderModel = taskModel?.remoteOrderModel,
                    endTarget = endTarget(),
                    taskId = taskId(),
                    bill = this@RemoteOrderSendBill
                )
                , type)
            )
            add(1, StartTakeTask(
                TaskModel(
                    taskModel?.remoteOrderModel?.to,
                    endTarget = endTarget(),
                    taskId = taskId(),
                    bill = this@RemoteOrderSendBill
                )
            )
            )
            add(2,
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.remoteOrderModel?.to,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderSendBill
                    ),
                    TYPE_SEND
                )
            )
            add(3,
                SendingTask(
                    TaskModel(
                        location = taskModel?.remoteOrderModel?.to,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderSendBill
                    )
                )
            )
            add(4, FinishTakeTask(
                TaskModel(
                    taskModel?.remoteOrderModel?.to,
                    endTarget = endTarget(),
                    taskId = taskId(),
                    bill = this@RemoteOrderSendBill
                )
            )
            )
            add(5,
                CallTakeObjectTask(
                    TaskModel(
                        location = taskModel?.remoteOrderModel?.to,
                        remoteOrderModel = taskModel?.remoteOrderModel,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderSendBill
                    ),
                    type = type
                )
            )
            add(6,
                CallTakeObjectFinishTask(
                    TaskModel(
                        location = taskModel?.remoteOrderModel?.to,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderSendBill
                    ),
                    type = type
                )
            )
            add(7,
                FinishRemoteOrderSendTask(
                    TaskModel(
                        location = taskModel?.remoteOrderModel?.to,
                        remoteOrderModel = taskModel?.remoteOrderModel,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderSendBill
                    ),
                    type = type
                )
            )
//            add(8,
//                AllFinishSendTask(
//                    TaskModel(
//                        location = taskModel?.remoteOrderModel?.to,
//                        endTarget = endTarget(),
//                        taskId = taskId(),
//                        bill = this@RemoteOrderSendBill
//                    )
//                )
//            )
        }
        return tempList
    }

    override fun billBuild(): List<ITaskBill> {
        return listOf(this)
    }

    private fun recreateQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        taskQueue.clear()
        return createTaskQueue(taskModel)
    }
}