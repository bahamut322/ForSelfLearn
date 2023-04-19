package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_GUIDE
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * @describe 讲解任务开始
 * @author Swn
 * @date 2021/11/15
 */
class StartExplanTask(taskModel: TaskModel) : AbstractTask(taskModel)  {


    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        ReportDataHelper.reportTaskStartDto(
            TYPE_GUIDE, this@StartExplanTask.taskModel?:TaskModel()
        )
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_GUIDE
        IdleGateDataHelper.reportIdleGateCount()
        withContext(Dispatchers.Main){
//            if (Universal.lastValue == null || !Universal.lastValue.equals(taskModel?.location?.pointName?:"")) { // 检查新值和上一个值是否相同
//                Universal.lastValue = taskModel?.location?.pointName?:""; // 更新上一个值为新值
//                // 在这里处理新值，比如更新UI或执行其他逻辑
//                // 这里假设你需要将新值设置给某个变量，可以像下面这样写：
//            }

        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}