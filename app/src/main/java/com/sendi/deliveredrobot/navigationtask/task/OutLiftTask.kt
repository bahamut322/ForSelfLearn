package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import geometry_msgs.Pose2D
import java.util.*

/**
 * @describe:出电梯
 */
class OutLiftTask(taskModel: TaskModel, private val needGetPose:Boolean = true, private val needHoldLiftDoor: Boolean = true) : AbstractTask(taskModel) {
    var point: QueryPointEntity? = null

    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
        point = dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_OUTSIDE,taskModel?.elevator?:"")
        taskModel?.location = point
        super.taskModel = taskModel
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.OutLiftTask
    }

    override suspend fun execute() {
        MyApplication.instance?.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, R.id.outLiftFragment)
        })
        RobotStatus.outOfLift = false
//        val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        if (point == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
        val pointInside = dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
        var retryTimes = 10
        if(pointInside != null){
            var result: Boolean
            do {
                result = ROSHelper.sendInDoorPoint(pointInside)
            }while (!result && retryTimes-- > 0)
        }

        if (needGetPose) {
            //出电梯
            val timer = Timer()
            var second = 60
            timer.schedule(object : TimerTask() {
                override fun run() {
                    second--
                }
            }, Date(), 1000)
            //查看切换锚点是否成功
            if (RobotStatus.targetAxis != null) {
                var distance = 1000f
                var pose2D: Pose2D?
                do{
                    pose2D = ROSHelper.getPose()
                    if (pose2D != null) {
                        distance = CommonHelper.getDistance(
                            pose2D.x.toFloat(),
                            RobotStatus.targetAxis!!.x!!,
                            pose2D.y.toFloat(),
                            RobotStatus.targetAxis!!.y!!
                        )
                    }
                }while (distance > 2 && second > 0)
                timer.cancel()
                if (second < 0) {
                    BillManager.currentBill()?.exception()
                    DialogHelper.troubleDialog.show()
                    RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
//                    DialogHelper.exceptionWaitForHelpDialog.show()
                    LogUtil.e("报障:出梯getPose超次数")
//                    RobotStatus.currentStatus = TYPE_EXCEPTION
                    CloudMqttService.publish(
                        PhoneCallModel(
                            number = "前台",
                            note = "2",
                            floor = RobotStatus.currentLocation?.floorName ?: ""
                        ).toString()
                    )
//                    TaskQueues.clearQueue()
//                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                    return
                }
                LogUtil.i("getPose成功")
            }
        }
        if (needHoldLiftDoor) {
            var retryHoldLiftTimes = 6
            var count = 20
            LiftHelper.timer.schedule(object : TimerTask() {
                override fun run() {
                    if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) {
                        return
                    }
                    when (count) {
                        0 -> {
                            //提前1s释放梯门
                            LiftHelper.releaseLiftDoor(point?.elevator?:"")
                            count = 20
                        }
                        20 -> {
                            // hold梯门开30秒
                            if (retryHoldLiftTimes > 0) {
                                retryHoldLiftTimes--
                                LiftHelper.sendLift(
                                    control = 1,
//                                    floorIndex = RobotCommand.LIFT_RELEASE_CONTROL_DOOR,
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
        }
        ROSHelper.outLift(point)
    }
}