package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * @describe:电梯移动
 */
class LiftMoveTask(taskModel: TaskModel?, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    var recallTime = 30
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.LiftMoveTask
    }

    //    var recheckStatusTime = 60
    override suspend fun execute() {
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.pass_lift_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.pass_lift_point_is_null))
            return
        }
//        val elevator = LiftHelper.findElevatorContainsFromAndTo(RobotStatus.currentLocation?.floorName?:"", taskModel?.location?.floorName?:"")
        //记录目标楼层
        RobotStatus.expectLocation =
            dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_OUTSIDE, taskModel?.elevator?:"")
        taskModel!!.location = RobotStatus.expectLocation?.apply {
            binMark = taskModel?.location?.binMark
        }
        if (RobotStatus.expectLocation == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_lift_point_fail))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_lift_point_fail))
            return
        }
        val exceptionLocation = RobotStatus.expectLocation
        RobotStatus.callingLift = true
//        RobotStatus.liftArrive = false
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.liftFragment)
        })
        if (RobotStatus.needDelay) {
            RobotStatus.needDelay = false
            ToastUtil.show("出梯失败，20s后重试")
            LogUtil.i("出梯失败，20s后重试")
            // 延时前释放开门
            virtualTaskExecute(20,"出梯失败，20s后重试")
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
                        floorName = exceptionLocation?.floorName?:"",
                        time = RobotCommand.LIFT_CONTROL_TIME,
                        elevator = exceptionLocation?.elevator?:""
                    )
                    recallTime--
                } else {
                    LiftHelper.releaseLiftDoor(exceptionLocation?.elevator?:"")
                    //超过重试次数
                    mainScope.launch(Dispatchers.Default) {
                        taskModel?.bill?.exception()
                        DialogHelper.troubleDialog.show()
                        RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                        LogUtil.e("报障:呼走电梯超次数")
                        CloudMqttService.publish(
                            PhoneCallModel(
                                number = "前台",
                                note = "5",
                                floor = RobotStatus.currentLocation?.floorName ?: ""
                            ).toString()
                        )
                        LiftHelper.resetTimer()
                    }
                }
            }
        }, Date(), 1000 * waitSeconds)
    }
}