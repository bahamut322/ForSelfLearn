package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import java.util.*

/**
 * @describe:进入电梯
 */
class IntoLiftTask(taskModel: TaskModel) : AbstractTask(taskModel) {
    var point: QueryPointEntity? = null

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
        point = dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
        taskModel?.location = point?.apply {
            binMark = taskModel?.location?.binMark?: 0x11
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.IntoLiftTask
    }

    override suspend fun execute() {
        RobotStatus.outOfLift = true
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.intoLiftFragment)
        })

        if (point == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
        var retryHoldLiftTimes = 6
        var count = 20
        LiftHelper.resetTimer()
        LiftHelper.timer.schedule(object : TimerTask() {
            override fun run() {
                if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) {
                    return
                }
                when (count) {
                    0 -> {
                        count = 20
                        //提前1s释放梯门
                        LiftHelper.releaseLiftDoor(taskModel?.elevator?:"")
                    }
                    20 -> {
                        // hold梯门开30秒
                        if (retryHoldLiftTimes > 0) {
                            retryHoldLiftTimes--
                            LiftHelper.sendLift(
                                control = 1,
//                                floorIndex = RobotCommand.LIFT_RELEASE_CONTROL_DOOR,
                                floorName = "开门",
                                time = 30,
                                elevator = point?.elevator?:""
                            )
                        } else {
                            //释放梯门
                            LiftHelper.releaseLiftDoor(point?.elevator?:"")
                            LiftHelper.resetTimer()
                        }
                        count--
                    }
                    in 1..20 -> {
                        count--
                    }
                }
            }
        }, Date(), 1000)
        ROSHelper.enterLift(point)
    }
}