package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_GUIDE
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import java.util.*

/**
 * @author heky
 * @date 2023-03-01
 * @description 迎宾任务清单
 */
class GoUsherPointTaskBill(taskModel: TaskModel?) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
        setTaskId(TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.USHER))
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
//        IdleGateDataHelper.reportIdleGateCount(0)
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
                StartGoUsherPointTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoUsherPointTaskBill
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoUsherPointTaskBill
                    )
                )
            )
            if((taskModel?.location?.pointId ?: -1) > 0){
                // step 2.5: 切换地图
                add(
                    JudgeFloorTask(
                        TaskModel(
                            location = taskModel?.location,
                            endTarget = endTarget(),
                            taskId = taskId(),
                            bill = this@GoUsherPointTaskBill
                        ),
                        type = TYPE_GUIDE
                    )
                )
                // step 10：到目的地task
                add(
                    GoingUsherPointTask(
                        TaskModel(
                            location = taskModel?.location,
                            endTarget = endTarget(),
                            taskId = taskId(),
                            bill = this@GoUsherPointTaskBill
                        ),
                    )
                )
            }
            add(
                UsherTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoUsherPointTaskBill
                    ),
                )
            )
            // step 12：完成任务task
            add(
                FinishUsherTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoUsherPointTaskBill
                    )
                )
            )
            add(
                FinishAllUsherTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoUsherPointTaskBill
                    )
                )
            )

        }
        return tempQueue
    }
}