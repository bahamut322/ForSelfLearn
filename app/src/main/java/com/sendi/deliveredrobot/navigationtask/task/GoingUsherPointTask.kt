package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil

/**
 * @describe:前往迎宾点中
 */
class GoingUsherPointTask(
    taskModel: TaskModel?, needReportData: Boolean = true
) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GoingUsherPoint
    }

    override suspend fun execute() {
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
//        ROSHelper.setSpeed("${basicSettingViewModel.value.basicConfig.usherSpeed}")
        val result = ROSHelper.navigateTo(taskModel!!.location!!)
        if(result == 1){
            MyApplication.instance!!.sendBroadcast(Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.goingGreetFragment)
            })
        }
        DialogHelper.loadingDialog.dismiss()
    }
}