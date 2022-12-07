package com.sendi.deliveredrobot.helpers

import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.model.LiftControlLoraModel
import com.sendi.deliveredrobot.model.LiftControlMqttModel
import com.sendi.deliveredrobot.model.ReplyElevatorListModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author heky
 * @date 2022-04-28
 * @describe 电梯Helper
 */
object LiftHelper {
    private val mainScope = MainScope()
    private val mutex = Mutex()
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    var timer = Timer() //公用timer


    /**
     * @describe 发送mqtt/lora
     */
    fun sendLift(control: Int, floorName: String, time: Int, elevator: String) {
        val timeStamp = System.currentTimeMillis()
        // mqtt
        CloudMqttService.publish(
            LiftControlMqttModel(
                control = control,
                floorName = floorName,
                time = time,
                elevator = elevator,
                timeStamp = timeStamp
            ).toString(),
            qos = 0
        )
        // lora
        ROSHelper.sendLoraLiftMessage(
            LiftControlLoraModel(
                control = control,
                floorName = floorName,
                time = time,
                elevator = elevator,
                timeStamp = timeStamp
            )
        )
    }

    /**
     * @describe 接受mqtt/lora
     */
    fun liftReach(currentFloorName: String, elevator: String) {
        // 记录当前所在楼层
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                if(RobotStatus.currentStatus == TYPE_EXCEPTION) return@launch
                if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                if (!RobotStatus.callingLift) return@launch
                withContext(Dispatchers.Default){
//                    RobotStatus.liftCurrentLocation = dao.queryLiftPointByFloorCode(currentFloorIndex)
                    RobotStatus.liftCurrentLocation = dao.queryLiftPointByFloorName(currentFloorName, elevator)
                }
                if (RobotStatus.liftCurrentLocation?.subMapId != RobotStatus.expectLocation?.subMapId) return@launch
                if(BillManager.currentBill() == null) return@launch
                RobotStatus.callingLift = false
                resetTimer()
                LogUtil.i("电梯到了")
                //电梯到了
                SpeakHelper.speak(MyApplication.instance!!.getString(R.string.i_just_was_born))
                when (RobotStatus.outOfLift) {
                    false -> {
                        //出电梯
                        BillManager.currentBill()?.executeNextTask()
                    }
                    true -> {
                        //进电梯
                        BillManager.currentBill()?.executeNextTask()
                    }
                }
            }
        }
    }

    fun resetTimer(){
        timer.cancel()
        timer = Timer()
    }

    /**
     * @description 释放梯门
     */
    fun releaseLiftDoor(elevator: String){
        sendLift(
            control = 0,
//            floorIndex = RobotCommand.LIFT_RELEASE_CONTROL_DOOR,
            floorName = "开门",
            time = 0,
            elevator = elevator
        )
    }

    /**
     * @description find elevator
     */
    private fun findElevatorContainsFromAndTo(fromFloorName: String, toFloorName: String): String{
        val elevatorListModel = ElevatorObject.originElevatorModel
        val elevatorList = elevatorListModel?.elevatorList?: return ""
        for (elevatorModel in elevatorList) {
            val floorNameList = ArrayList<String>()
            for (floorModel in elevatorModel.floorList) {
                floorNameList.add(floorModel.name)
            }
            if (floorNameList.contains(fromFloorName) && floorNameList.contains(toFloorName)) {
                return elevatorModel.elevator
            }
        }
        return ""
    }

    fun needTransfer(fromFloorName: String, toFloorName: String): NeedTransferResultModel{
        val findElevator = findElevatorContainsFromAndTo(fromFloorName, toFloorName)
        val needTransferResultModel = NeedTransferResultModel()
        if (findElevator.isEmpty()) {
            // 换乘
            LogUtil.i("需要换乘")
//            return findElevatorTransfer(fromFloorName, toFloorName)
            return findElevatorTransfer(fromFloorName, toFloorName)
        }else{
            // 不换乘
            LogUtil.i("不需换乘")
            needTransferResultModel.apply {
                needTransfer = false
                elevator = findElevator
            }
        }
        return needTransferResultModel
    }

    /**
     * @param fromFloorName 起始楼层
     * @param toFloorName 到达楼层
     */
    private fun findElevatorTransfer(fromFloorName: String, toFloorName: String): NeedTransferResultModel{
        val needTransferResultModel = NeedTransferResultModel()
        val elevatorListModel = ElevatorObject.originElevatorModel
        var elevatorList = elevatorListModel?.elevatorList?: return needTransferResultModel
        var findElevator = ""
        var transfer = ""
        var tempToFloorName = toFloorName
        LogUtil.i("开始计算换乘")
        var resetToFloorName = true
        w@while (findElevator.isEmpty()){
            // 遍历电梯列表
            for (elevatorModel in elevatorList) {
//                LogUtil.i("targetElevator:${elevatorModel.elevator}")
                // 找出包含终点的电梯
                if (elevatorModel.floorNameList.contains(tempToFloorName)) {
//                    LogUtil.i("tempToFloorName:$tempToFloorName")
                    // 遍历该电梯的换乘楼层列表
                   for (innerTransfer in elevatorModel.transferList) {
//                        LogUtil.i("innerTransfer:${innerTransfer}")
                        // 遍历电梯列表内的其他电梯
                        for (innerElevatorModel in elevatorList.filter { it.elevator != elevatorModel.elevator }) {
//                            LogUtil.i("innerElevator:${innerElevatorModel.elevator}")
                            // 如果换乘楼层包含该电梯的该换乘楼层
                            if (innerElevatorModel.transferList.contains(innerTransfer)) {
                                if (innerElevatorModel.floorNameList.contains(fromFloorName)) {
                                    // 可达楼层包含起点
                                    findElevator = innerElevatorModel.elevator
                                    transfer = innerTransfer
                                    break@w
                                }else{
                                    // 可达楼层不包含起点
                                    tempToFloorName = innerTransfer
                                    resetToFloorName = false
                                    continue
                                }
                            }
                            resetToFloorName = true
                        }
                    }
                    elevatorList = elevatorList.filter {
                        it != elevatorModel
                    }.toTypedArray()
                }
            }
            if(resetToFloorName)tempToFloorName = toFloorName
        }
        LogUtil.i("结束计算换乘")
        return needTransferResultModel.apply {
            needTransfer = true
            elevator = findElevator
            transferFloorName = transfer
        }
    }

    fun findFloorScore(floorName: String): Double{
        ElevatorObject.originFloorModel?.floorList?.forEach {
            if (floorName == it.floor) {
                return it.score
            }
        }
        return 0.0
    }

    data class NeedTransferResultModel(
        var needTransfer: Boolean = false,
        var transferFloorName: String = "",
        var elevator: String = ""
    ){
        override fun toString(): String {
            return """{"needTransfer":$needTransfer, "transferFloorName":"$transferFloorName", "elevator":"$elevator"}"""
        }
    }
}