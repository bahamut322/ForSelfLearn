package com.sendi.deliveredrobot.topic

import chassis_msgs.DockState
import chassis_msgs.SafeState
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.navigationtask.task.BeginDockTask
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DockStateTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val state = rosResult?.response as DockState
                val mag = when (state.state) {
                    DockState.Docking -> {
                        //对接中
                        "对接中"
                    }
                    DockState.Charging -> {
                        //充电中
                        if(BillManager.currentBill() != null && BillManager.currentBill()?.firstPeek() is BeginDockTask){
                            BillManager.currentBill()?.executeNextTask()
                        }
                        //设置当前位置
                        if(RobotStatus.adapterState.value!! != SafeState.TYPE_ADAPTER && RobotStatus.batteryStateNumber.value == false){
                            UpdateReturn().mapSetting(true)
                            RobotStatus.batteryStateNumber.postValue(true)
                            DialogHelper.briefingDialog.dismiss()
                        }
                        "充电中"
                    }
                    DockState.Outdocking -> {
                        //退出充电中
                        "退出充电中"

                    }
                    DockState.DockingTimeOut -> {
                        RobotStatus.docking = false
                        //自主充电超时
                        if (RobotStatus.retryDockTimes < RobotStatus.RETRY_DOCK_MAX_TIMES) {
                            //创建返回队列
                            val bill = BillManager.currentBill() as GoBackTaskBill
                            bill.dockFail()
                            //重试次数自增
                            RobotStatus.retryDockTimes++
                            LogUtil.i("第${RobotStatus.retryDockTimes + 1}次尝试试对接充电桩")
                            ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                        } else {
                            BillManager.currentBill()?.exception()
                            DialogHelper.troubleDialog.show()
                            RobotStatus.retryDockTimes = 0 //重置重试次数
                            LogUtil.e("报障:对接充电桩超次数")
                        }
                        "退出充电超时"
                    }
                    DockState.NoSignal -> {
                        //无信号
                        "无信号"
                    }
                    DockState.ChassisErr, DockState.ObstacleErr -> {
                        "error"
                    }
                    DockState.IDLE -> {
                        //空闲
                        "空闲"
                    }
                    DockState.OutdockingOk -> {
                        //退出充电成功
                        BillManager.currentBill()?.executeNextTask()

                        "退出充电成功"
                    }
                    else -> ""
                }
                LogUtil.i("自主充电--->${mag} :${state.state}")
            }
        }
    }
}