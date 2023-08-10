package com.sendi.deliveredrobot.navigationtask

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @describe:某一段讲解到达
 */
class ExplainArriveTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    override fun reportTaskDto() {}
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishArrayBroadcast
    }

    override suspend fun execute() {
//        if (TaskQueues.previousTask != null) {
//            taskModel = TaskQueues.previousTask!!.taskModel
//        }
//        if (TaskQueues.previousTask != null && TaskQueues.previousTask !is NavToFarPointTask) {
        if (BillManager.billList().size<=1) {
            LogUtil.i("all讲解任务结束")
            RobotStatus.explanationTaskFinish.postValue(1)
            var pointName = taskModel?.location?.pointName ?: ""
            pointName = pointName.toList().joinToString(" ")
//            SpeakHelper.speak(
//                String.format(
//                    MyApplication.instance!!.getString(R.string.point_arrived),
//                    pointName
//                )
//            )
//        }
            MyApplication.instance?.sendBroadcast(Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.explanArriveFragment)
            })
        }
        virtualTaskExecute(2, "讲解到达")
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}