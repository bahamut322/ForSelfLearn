package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_BUSINESS
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.BusinessArriveTask
import com.sendi.deliveredrobot.navigationtask.task.BusinessIngTask
import com.sendi.deliveredrobot.navigationtask.task.FinishBusinessTask
import com.sendi.deliveredrobot.navigationtask.task.FinishGuideTask
import com.sendi.deliveredrobot.navigationtask.task.JudgeFloorTask
import com.sendi.deliveredrobot.navigationtask.task.OutDockTask
import com.sendi.deliveredrobot.navigationtask.task.StartBusinessTask
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import java.util.LinkedList

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理任务清单
 */
class BusinessTaskBill(taskModel: TaskModel?) : AbstractTaskBill(taskModel ) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
        setTaskId(TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.BUSINESS))
        floorName = taskModel?.location?.floorName?:""
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override fun billBuild(): List<ITaskBill>{
        return listOf(this)
    }

    override suspend fun earlyFinish() {
        for (task in taskQueue) {
            if (task is FinishGuideTask) {
                task.status = -1
                task.enum = task.configEnum()
                task.beforeReportData(task.taskDto)
                task.reportTaskDto()
                break
            }
        }
        BillManager.clearBill(this)
    }

    override suspend fun exception() {
        IdleGateDataHelper.reportIdleGateCount(0)
        RobotStatus.currentStatus = TYPE_EXCEPTION
        earlyFinish()
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    /**
     * @describe 创建引领任务队列
     */
    override fun createTaskQueue(
        taskModel: TaskModel?
    ): LinkedList<AbstractTask> {
        val tempQueue = LinkedList<AbstractTask>()
        tempQueue.apply {
            add(
                StartBusinessTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    )
                )
            )
            // step 2.5: 切换地图
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    ),
                    type = TYPE_BUSINESS
                )
            )
            // step 10：到目的地task
            add(
                BusinessIngTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    ),
                    R.id.businessIngFragment
                )
            )
            // step 11：到达目的地
            add(
                BusinessArriveTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    )
                )
            )
            // step 12：完成任务task
            add(
                FinishBusinessTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@BusinessTaskBill
                    )
                )
            )
//            add(
//                AllFinishGuideTask(
//                    TaskModel(
//                        location = taskModel?.location,
//                        endTarget = endTarget(),
//                        taskId = taskId(),
//                        bill = this@GuideTaskBill
//                    )
//                )
//            )
        }
        return tempQueue
    }
}