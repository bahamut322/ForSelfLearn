package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.launch

/**
 * @describe:引领中
 */
class GuidingTask(
    taskModel: TaskModel?, private val navigateId:Int, needReportData: Boolean = true
) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GuidingTask
    }

    override suspend fun execute() {
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, navigateId)
        })
        RobotStatus.ArrayPointExplan.postValue(0)

        //step1设置速度
//        ROSHelper.setSpeed("${basicSettingViewModel.value.basicConfig.guideSpeed}")
        val result = ROSHelper.navigateTo(taskModel!!.location!!)
        if(result == 1){
            MyApplication.instance!!.sendBroadcast(Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, navigateId)
            })
        }
        DialogHelper.loadingDialog.dismiss()

    }
}