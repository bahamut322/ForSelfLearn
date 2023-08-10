package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskStageEnum

class FinishGoToReadyPointTask (taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishSendTask
    }

    override suspend fun execute() {
        if (RobotStatus.currentStatus != TYPE_CHARGING) {
            RobotStatus.currentStatus = TYPE_IDLE
        }
        MyApplication.instance!!.sendBroadcast(
            Intent().apply {
                action = ACTION_NAVIGATE
                //哎，在回家的路上多一个判断吧，看看要不要回家
                putExtra(NAVIGATE_ID, R.id.FinishReadyFragment)
                //不直接回家了😑
//                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
            }
        )
        BillManager.clearBillList()
    }
}