package com.sendi.deliveredrobot.topic

import android.content.Intent
import android.util.Log
import androidx.navigation.NavController
import chassis_msgs.SafeState
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.CheckSelfHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import sensor_msgs.BatteryState

object BatteryStateTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    private suspend fun refreshBatteryPower(batteryState: BatteryState){
        withContext(Dispatchers.Main){
            CheckSelfHelper.powerCheckComplete.value = true
            RobotStatus.batteryPower.value =
                batteryState.percentage
        }
    }

    fun handle(
        rosResult: RosResult<*>?,
        navController: NavController
    ) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                val batteryState = rosResult?.response as BatteryState
                if(RobotStatus.batterySupplyStatus.value != batteryState.powerSupplyStatus){
                    refreshBatteryPower(batteryState)
                    // =============== 更新电池状态 ================
                    if (batteryState.powerSupplyStatus == BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
                        // 切换到充电状态
                        LogUtil.i("正在充电")
                        withContext(Dispatchers.Main){
                            RobotStatus.chargeStatus.value = true
                        }
                        if (RobotStatus.adapterState.value!! != SafeState.TYPE_ADAPTER) {
                            RobotStatus.batteryStateNumber.postValue(true)
                        }
//                        RobotStatus.currentStatus = TYPE_CHARGING
                        RobotStatus.odomPose = ROSHelper.getOdomPose()
                        //自检状态下直接返回
                        if (RobotStatus.selfChecking == 0) return@launch
                        RobotStatus.currentStatus = TYPE_CHARGING
                        if (!RobotStatus.docking) {
                            //非自动回充状态下
                            if ((RobotStatus.batteryPower.value!! * 100).toInt() <= RobotStatus.LOW_POWER_VALUE) {
                                if ("chargeFragment" != navController.currentDestination?.label ?: "") {
                                    MyApplication.instance!!.sendBroadcast(
                                        Intent().apply {
                                            action = ACTION_NAVIGATE
                                            putExtra(NAVIGATE_ID, R.id.chargeFragment)
                                        }
                                    )
                                }
                            }
                        } else {
                            // 自动回充状态下
                            BillManager.currentBill()?.executeNextTask()
                        }
                    } else {
                        // 切换到非充电状态
                        LogUtil.i("退出充电")
                        RobotStatus.batteryStateNumber.postValue(false)
                        withContext(Dispatchers.Main){
                            RobotStatus.chargeStatus.value = false
                        }
                        if (RobotStatus.currentStatus == TYPE_CHARGING) {
                            RobotStatus.currentStatus = TYPE_IDLE
                        }
                    }
                    withContext(Dispatchers.Main){
                        RobotStatus.batterySupplyStatus.value =
                            batteryState.powerSupplyStatus
                    }
                }
                if (RobotStatus.batteryPower.value != batteryState.percentage) {
                    //自检
                    refreshBatteryPower(batteryState)
                }
                val batteryPower = (batteryState.percentage * 100).toInt()
                // 当电量小于LOW_POWER_VALUE 且 不在充电状态 且 不在自检 且 不在低电量自动回充过程中
                if (batteryPower <= RobotStatus.LOW_POWER_VALUE
                    && batteryState.powerSupplyStatus != BatteryState.POWER_SUPPLY_STATUS_CHARGING
                    && RobotStatus.selfChecking != 0
                    && !RobotStatus.lowPowerBacking
                ) {
                    if (batteryPower < RobotStatus.SHUT_DOWN_VALUE) {
                        val shutDown = MyApplication.instance!!.getString(
                            R.string.shut_down
                        )
                        ToastUtil.show(
                            shutDown
                        )
                        virtualTaskExecute(title = shutDown)
                        ROSHelper.shutDown()
                        return@launch
                    }

                    if (RobotStatus.originalLocation == null) {
                        ToastUtil.show(
                            MyApplication.instance!!.resources.getString(
                                R.string.charge_point_not_set_for_return
                            )
                        )
                        return@launch
                    }
                    // =============== 低电量回充 ==================
                    if (BillManager.billList().isEmpty()) {
                        // 如果处于无任务待机
                        RobotStatus.lowPowerBacking = true
                        ToastUtil.show(MyApplication.instance!!.getString(R.string.start_low_power_auto_charging))
                        val billList = GoBackTaskBillFactory.createBill(taskModel = TaskModel())
                        BillManager.addAllAtIndex(billList)
                        DialogHelper.lowPowerGoBack.show()
                        return@launch
                    }

                    // 判断是否在送物流程中
                    var inSendingFlow = false
                    when (BillManager.currentBill()) {
                        is SingleSendTaskBill,
                        is DoubleSameSendTaskBillTwo,
                        is DoubleSameSendTaskBillOne,
                        is DoubleDifferentSendTaskBillOne,
                        is DoubleDifferentSendTaskBillTwo,
                        is RemoteOrderSendBill,
                        is RemoteOrderPutBill  -> {
                            inSendingFlow = true
                        }
                    }

                    if (inSendingFlow) {
                        // 当正处于送物任务中，且没有执行到拨打电话页面、取物页面，且不在电梯内，则立即返回充电桩
                        if (navController.currentDestination?.label != "takeObjectFragment" && navController.currentDestination?.label != "callRoomFragment" && !RobotStatus.inLiftFlow) {
                            // 当前任务为非正在取物中则直接返回充电桩
                            RobotStatus.lowPowerBacking = true
                            ToastUtil.show(MyApplication.instance!!.getString(R.string.start_low_power_auto_charging))
                            val billList = RemoteOrderPutBillFactory.createBill(taskModel = TaskModel())
                            BillManager.addAllAtIndex(billList)
                            DialogHelper.lowPowerGoBack.show()
                        }
                    }
                }
            }
        }
    }
}