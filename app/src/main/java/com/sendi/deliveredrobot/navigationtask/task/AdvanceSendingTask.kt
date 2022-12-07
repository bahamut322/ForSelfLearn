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
 * @describe:送物中电梯外点专用
 */
class AdvanceSendingTask(private val cmd:Int, taskModel: TaskModel?) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.SendingTask
    }

    override suspend fun execute() {
        //step2导航路径
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.sendingFragment)
        })
        ROSHelper.setSpeed("${basicSettingViewModel.value.basicConfig.sendSpeed}")
//        ROSHelper.navigateTo(taskModel!!.location!!)
        ROSHelper.advanceMoveTo(cmd,taskModel!!.location!!)
        DialogHelper.loadingDialog.dismiss()
    }
}