package com.sendi.deliveredrobot.navigationtask.task

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *   @author: heky
 *   @date: 2021/8/24 15:43
 *   @describe: 结束自动充电
 */
class FinishDockTask(taskModel: TaskModel) : AbstractTask(taskModel){

    @SuppressLint("SimpleDateFormat")
    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss")

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishDockTask
    }

    override suspend fun execute() {
        val file = File("${Environment.getExternalStorageDirectory()}/DockTest.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.appendText("${simpleDateFormat.format(Date())},FINISH\r\n")
        //跳转连接成功页面
        MyApplication.instance!!.sendBroadcast(
            Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.finishDockFragment)
            }
        )
//        DialogHelper.dockFailDialog.dismiss()
        DialogHelper.troubleDialog.dismiss()
//        RobotStatus.dockFail = false
        RobotStatus.docking = false
        //重置重试次数
        RobotStatus.retryDockTimes = 0
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
    }
}