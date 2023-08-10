package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.TYPE_GUIDE
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.task.*
import java.util.*

/***
 * @author Swn
 * @date 2023-07-14
 * @description 前往待命点
 */
class GoToReadyPointBill (val taskModel: TaskModel?) : AbstractTaskBill(taskModel) {
    init {
        setEndTarget(taskModel?.location?.pointName?:"")
        setTaskId("")
        val tempList = createTaskQueue(taskModel)
        taskQueue.addAll(tempList)
    }

    override suspend fun earlyFinish() {
    }

    override suspend fun exception() {
    }

    override fun createTaskQueue(taskModel: TaskModel?): LinkedList<AbstractTask> {
        val tempQueue = LinkedList<AbstractTask>()
        val needReportData = false
        tempQueue.apply {
            add(
                OutDockTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoToReadyPointBill
                    ),
                    needReportData = needReportData
                )
            )
            // step 2.5: 切换地图
            add(
                JudgeFloorTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoToReadyPointBill
                    ),
                    type = TYPE_GUIDE,
                    needReportData = needReportData
                )
            )
            // step 10：到目的地task
            add(
                GuidingTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoToReadyPointBill
                    ),
                    R.id.guidingFragment,
                    needReportData = needReportData
                )
            )
            // step 11：到达目的地/可以取消。多个任务其实也没啥🐔8⃣️吊用
//            add(
//                ReadyArriveTask(
//                    TaskModel(
//                        location = taskModel?.location,
//                        endTarget = endTarget(),
//                        taskId = taskId(),
//                        bill = this@GoToReadyPointBill
//                    ),
//                    needReportData = needReportData
//                )
//            )
            add(
                FinishGoToReadyPointTask(
                    TaskModel(
                        location = taskModel?.location,
                        endTarget = endTarget(),
                        taskId = taskId(),
                        bill = this@GoToReadyPointBill
                    ),
                    needReportData = needReportData
                )
            )
        }
        return tempQueue
    }

    override fun billBuild(): List<ITaskBill> {
        return listOf(this)
    }
}