package com.sendi.deliveredrobot.navigationtask.task

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *   @author: heky
 *   @date: 2021/8/24 15:41
 *   @describe: 开始自动充电
 */
class BeginDockTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    @SuppressLint("SimpleDateFormat")
    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd-HH:mm:ss")

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.BeginDockTask
    }

    override suspend fun execute() {
        RobotStatus.docking = true
        MyApplication.instance!!.sendBroadcast(
            Intent().apply {
                action = ACTION_NAVIGATE
                putExtra(NAVIGATE_ID, R.id.dockingFragment)
            }
        )
        val file = File("${Environment.getExternalStorageDirectory()}/DockTest.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.appendText("${simpleDateFormat.format(Date())},BEGIN\r\n")
        var result = ROSHelper.controlDock(RobotCommand.CMD_RUN)
        var beginDockTimes = 0
        while (!result && beginDockTimes < RobotStatus.RETRY_DOCK_MAX_TIMES) {
            beginDockTimes++
            LogUtil.i("自动回充服务重试第${RobotStatus.retryDockTimes}次")
            result = ROSHelper.controlDock(RobotCommand.CMD_RUN)
        }
        if (!result) {
            RobotStatus.docking = false
            if (RobotStatus.retryDockTimes < RobotStatus.RETRY_DOCK_MAX_TIMES) {
                //创建返回队列
                val bill = BillManager.currentBill() as GoBackTaskBill
                bill.dockFail()
                //重试次数自增
                RobotStatus.retryDockTimes++
                LogUtil.i("第${RobotStatus.retryDockTimes}次尝试试对接充电桩")
//                TaskQueues.executeNextTask()
                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
            } else {
                // begin_dock返回false
                taskModel?.bill?.exception()
                DialogHelper.troubleDialog.show()
                RobotStatus.retryDockTimes = 0 //重置重试次数
                LogUtil.e("报障:对接充电桩超次数")
            }
        }
    }
}