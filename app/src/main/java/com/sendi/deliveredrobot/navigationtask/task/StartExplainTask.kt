package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_EXPLAN
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
class StartExplainTask(taskModel: TaskModel) : AbstractTask(taskModel)  {


    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            TYPE_EXPLAN, this@StartExplainTask.taskModel?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.StartExplaining
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_EXPLAN
        IdleGateDataHelper.reportIdleGateCount()
        withContext(Dispatchers.Main){
            RobotStatus.targetName?.postValue(taskModel?.location?.pointName?:"")

        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}