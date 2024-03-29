package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:迎宾中
 */
class UsherTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.UsherStart
    }

    override suspend fun execute() {
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.greetFragment)
        })
    }
}