package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import android.os.Bundle
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.BIN_MARK
import com.sendi.deliveredrobot.NAVIGATE_BUNDLE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:呼叫房间结束
 */
class CallRoomFinishTask(taskModel: TaskModel?, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallRoomFinishTask
    }

    override suspend fun execute() {
        when (basicSettingViewModel.value.basicConfig.needTakeObjectPassword) {
            0 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.takeObjectFragment)
                    putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                        putSerializable(BIN_MARK, taskModel?.location?.binMark)
                    })
                })
            }
            1 -> {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, R.id.inputTakeObjectCodeFragment)
                    putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                        putSerializable(BIN_MARK, taskModel?.location?.binMark)
                    })
                })
            }
        }

    }
}