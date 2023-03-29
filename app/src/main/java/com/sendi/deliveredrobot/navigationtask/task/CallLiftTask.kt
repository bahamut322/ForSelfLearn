package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.launch
import java.util.*

/**
 * @describe:呼叫电梯
 */
class CallLiftTask(taskModel: TaskModel) : AbstractTask(taskModel) {

    var recallTime = 30

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallLiftTask
    }

    //    var recheckStatusTime = 60
    override suspend fun execute() {
        if(taskModel?.taskId?.startsWith("D") == true || taskModel?.taskId?.startsWith("wx") == true){
            AudioMngHelper(MyApplication.instance!!).setVoice100(basicSettingViewModel.value.basicConfig.sendVolumeLift / 2)
        }
        if(taskModel?.taskId?.startsWith("G") == true){
            AudioMngHelper(MyApplication.instance!!).setVoice100(basicSettingViewModel.value.basicConfig.guideVolumeLift / 2)
        }
        //查询楼层编码
//        val relationshipLift =
//            dao.queryFloorBySubMapId(taskModel!!.location!!.subMapId!!)
//                ?: return
        //发送呼叫请求
        RobotStatus.expectLocation = RobotStatus.currentLocation
        val expectLocation = RobotStatus.expectLocation
        RobotStatus.callingLift = true
        RobotStatus.inLiftFlow = true
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.callLiftFragment)
        })
        if (RobotStatus.needDelay) {
            RobotStatus.needDelay = false
            // 延时前释放开门
            LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
            ToastUtil.show("进梯失败，20s后重试")
            LogUtil.i("进梯失败，20s后重试")
            virtualTaskExecute(20, "进梯失败，20s后重试")
        }
        val waitSeconds = 15L
        LiftHelper.timer.schedule(object : TimerTask() {
            override fun run() {
                if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) {
                    return
                }
                if (recallTime > 0) {
                    LiftHelper.sendLift(
                        control = 1,
                        floorName = expectLocation?.floorName?:"",
                        time = RobotCommand.LIFT_CONTROL_TIME,
                        elevator = taskModel?.elevator?:""
                    )
                    recallTime--
                } else {
                    //释放梯门
                    LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
                    //超过重试次数
                    if ((RobotStatus.currentLocation?.floorName ?: "") == (RobotStatus.originalLocation?.floorName ?: "0")) {
                        mainScope.launch(Dispatchers.Default) {
                            taskModel?.bill?.earlyFinish()
                            RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                            LiftHelper.resetTimer()
                            BillManager.currentBill()?.executeNextTask()
                        }
                    } else {
                        mainScope.launch(Dispatchers.Default) {
                            taskModel?.bill?.exception()
                            DialogHelper.troubleDialog.show()
                            RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                            LogUtil.e("报障:呼来电梯超次数")
                            CloudMqttService.publish(
                                PhoneCallModel(
                                    number = "前台",
                                    note = "2",
                                    floor = RobotStatus.currentLocation?.floorName ?: ""
                                ).toString()
                            )
//                        TaskQueues.clearQueue()
                            LiftHelper.resetTimer()
                        }
                    }
                }
            }
        }, Date(), 1000 * waitSeconds)
    }
}