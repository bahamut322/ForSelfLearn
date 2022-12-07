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
 * @description 呼叫放物
 */
class CallPutObjectTask(taskModel: TaskModel, type:Int) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallPutObjectTask
    }

    override suspend fun execute() {
        MyApplication.instance!!.sendBroadcast(
            Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.inputVerificationCodeFragment)
                putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                    putString(PAGE_TYPE, PAGE_TYPE_PUT)
                    putInt(BIN_MARK, taskModel?.location?.binMark?:0x11)
                })
            }
        )
        //拨打电话通知人来放东西
        CloudMqttService.publish(
            PhoneCallModel(
                number = taskModel?.remoteOrderModel?.fromPhone?:"1",
                note = "10",
                floor = taskModel?.remoteOrderModel?.from?.floorName?:"1"
        ).toString())
    }
}