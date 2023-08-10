package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import android.os.Bundle
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-07
 * @description 呼叫取物
 */
class CallTakeObjectTask(taskModel: TaskModel, type:Int, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return  TaskStageEnum.CallTakeObjectTask
    }

    override suspend fun execute() {
        MyApplication.instance!!.sendBroadcast(
            Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.inputVerificationCodeFragment)
                putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                    putString(PAGE_TYPE, PAGE_TYPE_TAKE)
                    putInt(BIN_MARK, taskModel?.location?.binMark?:0x11)
                })
            }
        )
        //拨打电话通知人来取东西
        CloudMqttService.publish(
            PhoneCallModel(
                number = taskModel?.remoteOrderModel?.toPhone?:"1",
                note = "10",
                floor = taskModel?.remoteOrderModel?.to?.floorName?:"1"
            ).toString())
    }
}