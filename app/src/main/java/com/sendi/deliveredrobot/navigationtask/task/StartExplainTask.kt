package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_EXPLAN
import com.sendi.deliveredrobot.TYPE_GUIDE
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * @describe 讲解任务开始
 * @author Swn
 * @date 2021/11/15
 */
class StartExplainTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {


    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        if (Universal.twice) {
            ReportDataHelper.reportTaskStartDto(
                TYPE_EXPLAN, this@StartExplainTask.taskModel ?: TaskModel()
            )
            Universal.twice = false
        }

    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_GUIDE
        IdleGateDataHelper.reportIdleGateCount()
//        withContext(Dispatchers.Main){
//            RobotStatus.targetName?.postValue(taskModel?.location?.pointName?:"")
//
//        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}