package com.sendi.deliveredrobot.topic

import android.app.Dialog
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DoorStateTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()
    private var doorStateListener: ((doorState:DoorState) -> Unit)? = null

    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                //仓门
                val state = rosResult?.response as DoorState
//                doorStateListener?.doorState(state)
                doorStateListener?.invoke(state)
                val msg = when (state.state) {
                    DoorState.STATE_OPENED -> {
//                        if (RobotStatus.settingControlDoor) {
//                            RobotStatus.settingControlDoor = false
//                            if(state.door == DoorState.DOOR_ONE){
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_OPEN,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
//                        if (RobotStatus.sendFail) {
//                            RobotStatus.sendFail = false
//                            if (RobotStatus.sendFailType == SEND_FAIL_TYPE_ONE_TWO_BOTH) {
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_OPEN,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
                        DialogHelper.loadingDialog.dismiss()
                        "仓门完全打开"
                    }
                    DoorState.STATE_CLOSED -> {
//                        if (RobotStatus.takingObject) {
//                            RobotStatus.takingObject = false
//                            if (RobotStatus.twoSamePlace) {
//                                RobotStatus.twoSamePlace = false
//                            } else {
//                                SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.i_continue_work))
//                            }
//                            TaskQueue.executeNextTask()
//                        }
//                        if (RobotStatus.settingControlDoor) {
//                            RobotStatus.settingControlDoor = false
//                            if(state.door == DoorState.DOOR_ONE) {
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_CLOSE,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
//                        if (RobotStatus.sendFail) {
//                            if (RobotStatus.sendFailType == SEND_FAIL_TYPE_ONE_TWO_BOTH) {
//                                //如果双仓有任务
//                                if (state.door == DoorState.DOOR_TWO) {
//                                    RobotStatus.sendFail = false
//                                    TaskQueue.executeNextTask()
//                                } else if (state.door == DoorState.DOOR_ONE) {
//                                    ROSHelper.controlBin(
//                                        RobotCommand.CMD_CLOSE,
//                                        DoorState.DOOR_TWO
//                                    )
//                                }
//                            } else {
//                                //如果非双仓有任务
//                                RobotStatus.sendFail = false
//                                TaskQueue.executeNextTask()
//                            }
//                        }

//                        if (RobotStatus.selfChecking == 0 && state.door == DoorState.DOOR_ONE) {
//                            val twoState = ROSHelper.controlBin(
//                                cmd = RobotCommand.CMD_CHECK,
//                                door = DoorState.DOOR_TWO
//                            )
//                            LogUtil.i("仓门" + state.door + "完全关闭")
//                            //开机自检的时候，仓门1和2都开着，仓门不能同时操作，通过仓门1关闭成功的topic再关闭仓门2
//                            if (twoState != 2) {
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_CLOSE,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
//                        when (RobotStatus.currentStatus){
//                            TYPE_REMOTE_ORDER_SEND, TYPE_REMOTE_ORDER_TAKE -> {
//                                SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.i_am_your_errand))
//                                TaskQueue.executeNextTask()
//                            }
//                        }
                        DialogHelper.loadingDialog.dismiss()
                        "仓门完全关闭"
                    }
                    DoorState.STATE_OPENING -> {
//                        if (RobotStatus.puttingObject) {
//                            RobotStatus.puttingObject = false
//                            SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_opening_please_put_things))
//                        }
//                        if (RobotStatus.selfChecking != 0) {
//                            DialogHelper.loadingDialog.show()
////                            virtualTaskExecute(4)
////                            DialogHelper.loadingDialog.dismiss()
//                        }
                        DialogHelper.loadingDialog.show()
                        "仓门打开中"
                    }
                    DoorState.STATE_CLOSING -> {
//                        if (RobotStatus.selfChecking != 0) {
//                            SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_closing_take_care_hands))
//                            DialogHelper.loadingDialog.show()
////                            virtualTaskExecute(4)
////                            DialogHelper.loadingDialog.dismiss()
//                        }
                        DialogHelper.loadingDialog.show()
                        "仓门关闭中"
                    }
                    DoorState.STATE_OPEN_FAILED -> {
//                        if (RobotStatus.settingControlDoor) {
//                            RobotStatus.settingControlDoor = false
//                            if(state.door == DoorState.DOOR_ONE) {
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_OPEN,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
                        DialogHelper.loadingDialog.dismiss()
                        "仓门打开失败"
                    }
                    DoorState.STATE_CLOSE_FAILED -> {
//                        if (RobotStatus.takingObject) {
//                            RobotStatus.takingObject = false
//                            TaskQueue.executeNextTask()
//                        }
//                        if (RobotStatus.settingControlDoor) {
//                            RobotStatus.settingControlDoor = false
//                            if(state.door == DoorState.DOOR_ONE) {
//                                ROSHelper.controlBin(
//                                    RobotCommand.CMD_CLOSE,
//                                    DoorState.DOOR_TWO
//                                )
//                            }
//                        }
                        DialogHelper.loadingDialog.dismiss()
                        "仓门关闭失败"
                    }
                    DoorState.STATE_HALF_OPEN -> {
                        DialogHelper.loadingDialog.dismiss()
                        "仓门半开合"
                    }
                    else -> {
                        DialogHelper.loadingDialog.dismiss()
                        "未知"
                    }
                }
                LogUtil.i("${state.door}仓${msg}")
            }
        }
    }

//    interface DoorStateListener{
//        fun doorState(doorState:DoorState)
//    }
//
    fun setDoorStateListener(doorStateListener: (doorState: DoorState) -> Unit){
        this.doorStateListener = null
        this.doorStateListener = doorStateListener
    }
}