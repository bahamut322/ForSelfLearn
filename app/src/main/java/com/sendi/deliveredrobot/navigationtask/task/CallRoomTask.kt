package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:打电话呼叫房间
 */
class CallRoomTask(taskModel: TaskModel?) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallRoomTask
    }

    override suspend fun execute() {
        SpeakHelper.speak(MyApplication.instance!!.getString(R.string.your_things_arrived))
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.callRoomFragment)
        })
        CloudMqttService.publish(
            PhoneCallModel(
                number = taskModel!!.location!!.pointName!!,
                note = "1",
                floor = RobotStatus.currentLocation?.floorName?:""
            ).toString()
        )
//        TaskQueue.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}