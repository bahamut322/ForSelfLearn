package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.RemoteOrderModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

class RemoteOrderPutBill(private val taskModel: TaskModel?): AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.remoteOrderModel?.from?.pointName ?: "")
        setTaskId(taskModel?.remoteOrderModel?.taskId?:"")
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override suspend fun earlyFinish() {
        for (task in taskQueue) {
            if (task is CallPutObjectFinishTask) {
                task.exceptioned = true
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
            }
            if (task is FinishRemoteOrderPutTask) {
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
            if (task is CallPutObjectFinishTask) {
                task.exceptioned = true
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
            }
            if (task is FinishRemoteOrderPutTask) {
                task.exceptioned = true
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
        val tempList = LinkedList<AbstractTask>()
//        mutex.withLock {
        DialogHelper.loadingDialog.show()
        val bin1 = with(viewModelBin1.value){
            val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished && !hasBill()
            if(result){
                // 1仓有空
                with(taskModel?.remoteOrderModel){
                    this?.from?.apply {
                        binMark = binMarkBin1
                    }
                    this?.to?.apply {
                        binMark = binMarkBin1
                    }
                    viewModelBin1.value.remoteOrderModel = this
                    viewModelBin1.value.setBill(this@RemoteOrderPutBill)
                    val orderTask = createRemoteOrderTask(
                        remoteOrderModel = this,
                        taskType = when (this?.taskType) {
                            "送物" -> TYPE_REMOTE_ORDER_SEND
                            "取物" -> TYPE_REMOTE_ORDER_TAKE
                            else -> TYPE_REMOTE_ORDER_SEND
                        })
                    if (orderTask != null) {
                        tempList.addAll(orderTask)
                    }
                }
            }
            result
        }
        if(!bin1){
            with(viewModelBin2.value){
                val result = previousTaskFinished /*&& previousRemoteOrderPutFinished*/ && previousRemoteOrderSendFinished && !hasBill()
                if(result){
                    // 2仓有空
                    with(taskModel?.remoteOrderModel){
                        this?.from?.apply {
                            binMark = binMarkBin2
                        }
                        this?.to?.apply {
                            binMark = binMarkBin2
                        }
                        viewModelBin2.value.remoteOrderModel = this
                        viewModelBin2.value.setBill(this@RemoteOrderPutBill)
                        val orderTask = createRemoteOrderTask(
                            remoteOrderModel = this,
                            taskType = when (this?.taskType) {
                                "送物" -> TYPE_REMOTE_ORDER_SEND
                                "取物" -> TYPE_REMOTE_ORDER_TAKE
                                else -> TYPE_REMOTE_ORDER_SEND
                            })
                        if (orderTask != null) {
                            tempList.addAll(orderTask)
                        }
                    }
                }
            }
        }
        DialogHelper.loadingDialog.dismiss()
//            return bin1 || bin2
//        }
        return tempList
    }

    override fun billBuild(): List<ITaskBill> {
        return when(taskQueue.isNotEmpty()) {
            true-> listOf(this)
            else -> LinkedList<ITaskBill>()
        }
    }

    /**
     * @description 创建小程序任务链
     */
    private fun createRemoteOrderTask(
        remoteOrderModel: RemoteOrderModel?,
        taskType: Int
    ): LinkedList<AbstractTask>?{
        when (taskType) {
            TYPE_REMOTE_ORDER_SEND,
            TYPE_REMOTE_ORDER_TAKE -> {
                RobotStatus.lowPowerBacking = false
                return createRemoteOrderTaskQueue(remoteOrderModel, taskType)
            }
        }
        return null
    }

    /**
     * @describe 创建跑腿任务队列
     */
    private fun createRemoteOrderTaskQueue(
        remoteOrderModel: RemoteOrderModel?,
        type: Int
    ): LinkedList<AbstractTask>{
        val tempList = LinkedList<AbstractTask>()
        tempList.apply {
            add(
                StartRemoteOrderPutTask(
                    TaskModel(
                        remoteOrderModel = remoteOrderModel,
                        endTarget = endTarget(),
                        taskId = remoteOrderModel?.taskId ?:"",
                        bill = this@RemoteOrderPutBill
                    ), type)
            )
            add(
                StartPutTask(
                    TaskModel(
                        remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    )
                )
            )
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    ),
                    TYPE_SEND
                )
            )
            add(
                SendingTask(
                    TaskModel(
                        location = remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    )
                )
            )
            add(
                FinishPutTask(
                    TaskModel(
                        remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    )
                )
            )
            add(
                CallPutObjectTask(
                    TaskModel(
                        location = remoteOrderModel?.from,
                        remoteOrderModel = remoteOrderModel,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    ),
                    type = type
                )
            )
            add(
                CallPutObjectFinishTask(
                    TaskModel(
                        location = remoteOrderModel?.from,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    ),
                    type = type
                )
            )
            add(
                FinishRemoteOrderPutTask(
                    TaskModel(
                        location = remoteOrderModel?.from,
                        remoteOrderModel = remoteOrderModel,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@RemoteOrderPutBill
                    ),
                    type = type
                )
            )
        }
        return tempList
    }

    private fun recreateQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        taskQueue.clear()
        return createTaskQueue(taskModel)
    }
}