package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import chassis_msgs.SafeState
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskStageEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sensor_msgs.BatteryState

/**
 *   @author: heky
 *   @date: 2021/8/24 19:57
 *   @describe: 退出自主充电
 */
class OutDockTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.OutDockTask
    }

    override suspend fun execute() {
        if (RobotStatus.batterySupplyStatus.value!! == BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
            if (RobotStatus.adapterState.value!! != SafeState.TYPE_ADAPTER) {
                //如果是在充电桩，添加退出自主充电任务
                //跳转开始工作页面
                MyApplication.instance!!.sendBroadcast(
                    Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, R.id.readyForTaskFragment)
                    }
                )
                ROSHelper.controlDock(RobotCommand.CMD_OUT_DOCK)
            } else {
                //如果是适配器
                DialogHelper.pullOutAdapterDialog.show()
                return
            }
        } else {
            mainScope.launch {
                DialogHelper.loadingDialog.show()
                MyApplication.instance!!.sendBroadcast(
                    Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, R.id.readyForTaskFragment)
                    }
                )
                if (judgeBeforeNavigate()) {
                    ROSHelper.controlDock(RobotCommand.CMD_OUT_DOCK)
                }else{
                    taskModel?.bill?.executeNextTask()
                }
                DialogHelper.loadingDialog.dismiss()
            }
        }
    }

    /**
     * @description 判断是否满足条件出发
     */
    private suspend fun judgeBeforeNavigate(): Boolean{
        val resultCode: Int
        withContext(Dispatchers.Default){
            val chargePoint = dao.queryChargePoint()
            resultCode = if(RobotStatus.currentLocation?.subMapId == chargePoint?.subMapId){
                ROSHelper.checkNearCharge(
                    location = chargePoint
                )
            }else{
                -1
            }
        }
        return resultCode == 1
    }
}