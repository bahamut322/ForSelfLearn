package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_EXPLAN
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskTypeEnum
import java.util.*

/**
 * @author Swn
 * @date 2023-04-23
 * @description 智能讲解任务清单
 */
class ExplainTaskBill(taskModel: TaskModel?) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName ?: "")
        setTaskId(TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.EXPLAIN))
        floorName = taskModel?.location?.floorName?:""
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override fun billBuild(): List<ITaskBill>{
        return listOf(this)
    }

    override suspend fun earlyFinish() {
        for (task in taskQueue) {
            if (task is FinishExplainTask) {
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
        RobotStatus.currentStatus = TYPE_EXCEPTION
        earlyFinish()
        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
    }

    override fun executeNextTask() {
        when(currentTask){
            is OnTheWayExplainTask -> {
                // 如果当前任务是途径讲解任务，则判断是否途径讲解是否完成，如果完成，则执行下一个任务，如果未完成，则通知页面更新，等待讲解完成
                val onTheWayExplainTask = (currentTask as OnTheWayExplainTask)
                if (onTheWayExplainTask.oneWayExplainFinish()) {
                    // 途径讲解完成
                    super.executeNextTask()
                }else{
                    // 途径讲解未完成，通知页面更新，等待讲解完成
                    onTheWayExplainTask.notifyFragmentUpdate()
                }
            }
            else -> {
                super.executeNextTask()
            }
        }
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
                StartExplainTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
                    )
                )
            )
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
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
                        bill = this@ExplainTaskBill
                    ),
                    type = TYPE_EXPLAN
                )
            )
            // step 10：到目的地task
            add(
                ExplainingTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
                    ),
                    R.id.StartExplantionFragment
                )
            )
            add(
                ArrayPointExplainTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
                    )
                )
            )
            // step 11：到达目的地
            add(
                ExplainArriveTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
                    )
                )
            )

            // step 12：完成任务task
            add(
                FinishExplainTask(
                    taskModel = TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@ExplainTaskBill
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