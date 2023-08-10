package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-22
 * @description 预加载地图
 */
class PreLoadMapTask(taskModel: TaskModel, needReportData: Boolean = true): AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.PreLoadMapTask
    }

    override suspend fun execute() {
        ROSHelper.preLoadMap(taskModel?.location?.subPath?:"")
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}