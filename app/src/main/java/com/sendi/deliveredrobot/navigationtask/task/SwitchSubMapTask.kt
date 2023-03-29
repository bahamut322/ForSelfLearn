package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil

/**
 * @describe:切换地图
 */
class SwitchSubMapTask(taskModel: TaskModel? = null, private val needSwitch: Boolean = true) :
    AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.SwitchSubMapTask
    }

    override suspend fun execute() {
        DialogHelper.loadingDialog.show()
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_data_null_switch_map_fail))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_data_null_switch_map_fail))
            return
        }
        //切换地图
        var result: Boolean
        var retryTimes = 10
        do {
            result = ROSHelper.setNavigationMap(
                taskModel!!.location!!.subPath!!,
                taskModel!!.location!!.routePath!!
            )
            if(!result) virtualTaskExecute(1, "切换地图")
        } while (!result && retryTimes-- > 0)
        if (retryTimes < 0) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.switch_map_fail))
            LogUtil.e(MyApplication.instance!!.getString(R.string.switch_map_fail))
            BillManager.billList().clear()
            ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
            return
        }
        if (needSwitch) {
            //切换锚点
            virtualTaskExecute(1,"切换锚点")
//            val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
            val currentAxis =
                dao.queryLiftPoint(RobotStatus.currentLocation!!.subMapId!!, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
            val targetAxis =
                dao.queryLiftPoint(taskModel!!.location!!.subMapId!!, PointType.LIFT_INSIDE,taskModel?.elevator?:"")
            if (currentAxis == null || targetAxis == null) {
                ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_lift_point_fail))
                LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_lift_point_fail))
                BillManager.billList().clear()
                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                return
            }
            RobotStatus.targetAxis = targetAxis
            val result2 = ROSHelper.setMultiAxis(currentAxis, targetAxis)
            if (!result2) {
                ToastUtil.show(MyApplication.instance!!.getString(R.string.switch_anchor_fail))
                LogUtil.e(MyApplication.instance!!.getString(R.string.switch_anchor_fail))
                BillManager.billList().clear()
                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                return
            }

        }
//        TaskQueues.executeNextTask()
        taskModel?.bill?.executeNextTask()
        DialogHelper.loadingDialog.dismiss()
    }
}