package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:某一段引领到达
 */
class GuideArriveTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GuideArriveTask
    }

    override suspend fun execute() {
//        if (TaskQueues.previousTask != null) {
//            taskModel = TaskQueues.previousTask!!.taskModel
//        }
//        if (TaskQueues.previousTask != null && TaskQueues.previousTask !is NavToFarPointTask) {
            var pointName = taskModel?.location?.pointName ?: ""
            pointName = pointName.toList().joinToString(" ")
        RobotStatus.arrayPointExplain.postValue(1)

//        SpeakHelper.speak(String.format(MyApplication.instance!!.getString(R.string.point_arrived),pointName))
//        }
//        MyApplication.instance?.sendBroadcast(Intent().apply {
//            action = ACTION_NAVIGATE
//            putExtra(NAVIGATE_ID, R.id.guideArriveFragment)
//        })
        virtualTaskExecute(1, "引领到达")
//        TaskQueues.executeNextTask()
//        taskModel?.bill?.executeNextTask()
    }
}