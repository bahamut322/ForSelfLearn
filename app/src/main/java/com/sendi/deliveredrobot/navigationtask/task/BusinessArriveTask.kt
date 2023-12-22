package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.TaskNext

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理到达
 */
class BusinessArriveTask(taskModel: TaskModel, needReportData: Boolean = true) :
    AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.BusinessArriveTask
    }

    override suspend fun execute() {
//        if (TaskQueues.previousTask != null) {
//            taskModel = TaskQueues.previousTask!!.taskModel
//        }
//        if (TaskQueues.previousTask != null && TaskQueues.previousTask !is NavToFarPointTask) {
//        var pointName = taskModel?.location?.pointName ?: ""
//        pointName = pointName.toList().joinToString(" ")
        //到点语音
//        SpeakHelper.speak(String.format(MyApplication.instance!!.getString(R.string.point_arrived),pointName))
//        }

        virtualTaskExecute(2, "业务办理到达")
//        TaskQueues.executeNextTask()
        LogUtil.i("TODO 到达讲解点通知")
        RobotStatus.ArrayPointExplan.postValue(1)
        TaskNext.setOnChangeListener {
            if (TaskNext.getToDo() == "1") {
                LogUtil.i("TODO 到达讲解点${TaskNext.getToDo()}")
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.guideArriveFragment)
                })
                taskModel?.bill?.executeNextTask()
                TaskNext.setToDo("0")
                RobotStatus.ArrayPointExplan.postValue(0)
            }
        }
    }
}