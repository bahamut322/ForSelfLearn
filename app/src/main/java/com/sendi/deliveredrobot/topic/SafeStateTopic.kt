package com.sendi.deliveredrobot.topic

import chassis_msgs.SafeState
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_CHARGING
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.TYPE_IDLE
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.task.BeginDockTask
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.TaskArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object SafeStateTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()
    private var previousStatus = RobotStatus.currentStatus

    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            val safeState = rosResult?.response as SafeState
            if (safeState.safeType == SafeState.TYPE_ADAPTER && RobotStatus.adapterState.value != safeState.safeState) {
                withContext(Dispatchers.Main) {
                    //自检
                    RobotStatus.adapterState.value = safeState.safeState
                }
            }
            if (safeState.safeType == SafeState.TYPE_EMERGENCY_STOP) {
                mutex.withLock {
                    if (safeState.safeState == SafeState.STATE_IS_TRIGGING) {
                        LogUtil.d("急停按下")
                        TaskArray.setToDo("3")
                        //播报语音音量
                        MediaPlayerHelper.getInstance().pause()
                        BaiduTTSHelper.getInstance().pause()
//                        IdleGateDataHelper.reportIdleGateCount(0)
                        withContext(Dispatchers.Main) {
                            RobotStatus.stopButtonPressed.value = RobotCommand.STOP_BUTTON_PRESSED
                        }
                        DialogHelper.stopDialog.show()
                        previousStatus = RobotStatus.currentStatus
                        if (RobotStatus.currentStatus == TYPE_EXCEPTION) return@launch //如果已经报错了，则不用往下走
                        RobotStatus.currentStatus = TYPE_EXCEPTION
                        if (safeStateListener == null) {
                            when (RobotStatus.manageStatus) {
                                RobotCommand.MANAGE_STATUS_STOP -> {
                                    if (BillManager.currentBill()?.firstPeek() is BeginDockTask) {
                                        ROSHelper.controlDock(RobotCommand.CMD_PAUSE)
                                    } else {
                                        BillManager.currentBill()?.addCurrentTask()
                                    }
                                }

                                RobotCommand.MANAGE_STATUS_PAUSE -> {}
                                RobotCommand.MANAGE_STATUS_CONTINUE -> {
                                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                                }
                            }
                        } else {
                            safeStateListener?.invoke(safeState)
                        }
                        //按下急停时，释放开门，提升用户体验
                        LiftHelper.releaseLiftDoor(
                            BillManager.currentBill()?.currentTask()?.taskModel?.elevator ?: ""
                        )
                    } else if (safeState.safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                        DialogHelper.stopDialog.dismiss()
                        LogUtil.d("急停抬起")
                        if (!Universal.speakIng && !Universal.Process && !Universal.Changing && !Universal.Finish) {
                            MediaPlayerHelper.getInstance().resume()
                            BaiduTTSHelper.getInstance().resume()
                        }
//                        IdleGateDataHelper.reportIdleGateCount()
                        withContext(Dispatchers.Main) {
                            RobotStatus.stopButtonPressed.value = RobotCommand.STOP_BUTTON_UNPRESSED
                        }
                        if (previousStatus == TYPE_EXCEPTION) return@launch //如果按下急停之前已经处于报错的状态，则不用往下走
                        when (previousStatus) {
                            TYPE_CHARGING -> {
                                if (RobotStatus.chargeStatus.value == false) {
                                    RobotStatus.currentStatus = TYPE_IDLE
                                } else {
                                    RobotStatus.currentStatus = previousStatus
                                }
                            }

                            else -> RobotStatus.currentStatus = previousStatus
                        }
//                        IdleGateDataHelper.reportIdleGateCount()
                        if (safeStateListener == null) {
                            when (RobotStatus.manageStatus) {
                                RobotCommand.MANAGE_STATUS_STOP -> {
                                    if (Universal.explainUnSpeak) {
                                        TaskArray.setToDo("5")
                                        return@launch
                                    }
                                    if (BillManager.currentBill()?.firstPeek() is BeginDockTask) {
                                        ROSHelper.controlDock(RobotCommand.CMD_RESUME)
                                    } else {
                                        BillManager.currentBill()?.executeNextTask()
                                    }
                                }

                                RobotCommand.MANAGE_STATUS_PAUSE -> {
                                    if (Universal.explainUnSpeak) {
                                        TaskArray.setToDo("5")
                                        return@launch
                                    }
                                    Universal.explainUnSpeak = false
                                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
                                }

                                RobotCommand.MANAGE_STATUS_CONTINUE -> {}

                            }

                        } else {
                            safeStateListener?.invoke(safeState)
                        }
                    }
                }
            }
        }
    }

    private var safeStateListener: ((safeState: SafeState) -> Unit)? = null
    fun setSafeStateListener(safeStateListener: (safeState: SafeState) -> Unit) {
        resetSafeStateListener()
        this.safeStateListener = safeStateListener
    }

    fun resetSafeStateListener() {
        this.safeStateListener = null
    }
}