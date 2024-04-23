package com.sendi.deliveredrobot.navigationtask

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @describe:某一段讲解到达
 */
class ExplainArriveTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishArrayBroadcast
    }

    override suspend fun execute() {
        DialogHelper.loadingDialog.show()
        if (BillManager.billList().size<=1) {
            LogUtil.i("all讲解任务结束")
            MyApplication.instance?.sendBroadcast(Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.explanArriveFragment)
            })
        }
        virtualTaskExecute(2, "讲解到达")
        taskModel?.bill?.executeNextTask()
        DialogHelper.loadingDialog.dismiss()
    }
}