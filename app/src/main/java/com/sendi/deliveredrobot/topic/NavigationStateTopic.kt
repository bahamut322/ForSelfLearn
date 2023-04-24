package com.sendi.deliveredrobot.topic

import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.MyApplication.Companion.instance
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXCEPTION
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.Order
import com.sendi.deliveredrobot.view.widget.Stat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import navigation_base_msgs.State

object NavigationStateTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                // 导航状态 1,终止（到达）  2,暂停  3,继续
                val state = rosResult?.response as State
                // 记录状态机状态
                RobotStatus.manageStatus = state.state
                if (state.state == 1) {
//                    //判断isNotEmpty是因为底盘不知何时会上传这个状态，避免影响逻辑，只在任务栈内有任务时才走这段代码
                    if (RobotStatus.currentStatus == TYPE_EXCEPTION) return@launch
                    if (RobotStatus.callingLift) return@launch
//                    if (!Universal.selectMapPoint) {
//                        RobotStatus.ready.postValue(1)
//                    }
                    BillManager.currentBill()?.executeNextTask()

//                    if (Universal.Model != "讲解" || Universal.selectMapPoint) {
//                        LogUtil.i("123123")
//                        BillManager.currentBill()?.executeNextTask()
//                    }
                } else if (state.state == -1) {
                    val msg = when (state.infoCode) {
                        -1 -> {
                            handleException()
                            "导航异常-导航请求终止"
                        }

                        -20 -> {
                            handleException()
                            "传感器异常-激光传感器异常"
                        }
                        -21 -> {
                            handleException()
                            "传感器异常-摄像头异常"
                        }
                        -30 -> {
                            handleException()
                            SpeakHelper.speak(MyApplication.instance!!.getString(R.string.i_lose_my_way))
                            "导航异常-非正常迷路"
                        }
                        -50 -> {
                            handleException()
                            "传感器异常-缺失里程计异常"
                        }
                        -51 -> {
                            handleException()
                            "传感器异常-电机锁异常"
                        }
                        -60 -> {
                            LiftHelper.resetTimer()
                            if (RobotStatus.outOfLift) {
                                if (RobotStatus.callLiftAndMoveTimes < RobotStatus.CALL_LIFT_AND_MOVE_TIMES) {
                                    SpeakHelper.speak("你们先走，我等下一趟哈")
                                    //进入电梯失败
                                    //查询电梯外点
                                    var point: QueryPointEntity?
                                    withContext(Dispatchers.Default) {
                                        point =
                                            dao.queryLiftPoint(
                                                RobotStatus.currentLocation!!.subMapId!!,
                                                PointType.LIFT_OUTSIDE,
                                                elevator = BillManager.currentBill()
                                                    ?.currentTask()?.taskModel?.elevator ?: ""
                                            )
                                    }
                                    if (point != null) {
                                        if (ROSHelper.checkOutOfLift(point!!)) {
                                            //电梯外
                                            //释放开门
                                            LiftHelper.releaseLiftDoor(
                                                BillManager.currentBill()
                                                    ?.currentTask()?.taskModel?.elevator ?: ""
                                            )
                                            BillManager.currentBill()
                                                ?.addRetryIntoLiftQueue(needHoldLiftDoor = false)
                                        } else {
                                            BillManager.currentBill()
                                                ?.addRetryIntoLiftQueue(needHoldLiftDoor = true)
                                        }
                                    }
                                    RobotStatus.callLiftAndMoveTimes++
                                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                                } else {
                                    LogUtil.e("报障:电梯超次数")
                                    BillManager.currentBill()?.exception()
                                    DialogHelper.troubleDialog.show()
                                    RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                                    CloudMqttService.publish(
                                        PhoneCallModel(
                                            number = "前台",
                                            note = "2",
                                            floor = RobotStatus.currentLocation?.floorName ?: ""
                                        ).toString()
                                    )
                                }
                            } else {
                                //出电梯超时
                                //释放开门
                                LiftHelper.releaseLiftDoor(
                                    BillManager.currentBill()
                                        ?.currentTask()?.taskModel?.elevator ?: ""
                                )
                                if (RobotStatus.callLiftAndMoveTimes < RobotStatus.CALL_LIFT_AND_MOVE_TIMES) {
                                    SpeakHelper.speak("你们先走，我等下一趟哈")
                                    RobotStatus.callLiftAndMoveTimes++
                                    BillManager.currentBill()?.addRetryOutLiftQueue()
//                                    RobotStatus.needDelay = true
                                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)

                                } else {
                                    with(CloudMqttService) {
                                        // 电话报障
                                        publish(
                                            PhoneCallModel(
                                                number = "前台",
                                                note = "5",
                                                floor = RobotStatus.currentLocation?.floorName
                                                    ?: ""
                                            ).toString()
                                        )
                                    }
                                    LogUtil.e("报障:电梯超次数")
                                    BillManager.currentBill()?.exception()
                                    DialogHelper.troubleDialog.show()
                                    RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                                }
                            }
                            "进出电梯超时"
                        }
                        -61 -> {
                            LiftHelper.resetTimer()
                            if (RobotStatus.callLiftAndMoveTimes < RobotStatus.CALL_LIFT_AND_MOVE_TIMES) {
                                SpeakHelper.speak("你们先走，我等下一趟哈")
                                RobotStatus.callLiftAndMoveTimes++
                                //获取当前位置
//                                val pose2D = ROSHelper.getPose()
                                //查询电梯外点
                                var point: QueryPointEntity?
                                withContext(Dispatchers.Default) {
                                    point =
                                        dao.queryLiftPoint(
                                            RobotStatus.currentLocation!!.subMapId!!,
                                            PointType.LIFT_OUTSIDE,
                                            elevator = BillManager.currentBill()
                                                ?.currentTask()?.taskModel?.elevator ?: ""
                                        )
                                }
                                if (point != null) {
                                    if (ROSHelper.checkOutOfLift(point!!)) {
                                        //电梯外
                                        //释放开门
                                        LiftHelper.releaseLiftDoor(
                                            BillManager.currentBill()
                                                ?.currentTask()?.taskModel?.elevator ?: ""
                                        )
                                        BillManager.currentBill()
                                            ?.addRetryIntoLiftQueue(needHoldLiftDoor = false)
                                    } else {
                                        BillManager.currentBill()
                                            ?.addRetryIntoLiftQueue(needHoldLiftDoor = true)
                                    }
                                }
                                //执行任务
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                            } else {
                                // 释放梯门
                                LiftHelper.releaseLiftDoor(
                                    BillManager.currentBill()
                                        ?.currentTask()?.taskModel?.elevator ?: ""
                                )
                                // 电话报障
                                CloudMqttService.publish(
                                    PhoneCallModel(
                                        number = "前台",
                                        note = "2",
                                        floor = RobotStatus.currentLocation?.floorName ?: ""
                                    ).toString()
                                )
                                LogUtil.e("报障:电梯超次数")
                                BillManager.currentBill()?.exception()
                                DialogHelper.troubleDialog.show()
                                RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数

                            }
                            "进电梯被围堵"
                        }
                        -62 -> {
                            SpeakHelper.speak("你们先走，我等下一趟哈")
                            //释放开门
                            LiftHelper.resetTimer()
                            LiftHelper.releaseLiftDoor(
                                BillManager.currentBill()?.currentTask()?.taskModel?.elevator
                                    ?: ""
                            )
                            if (RobotStatus.callLiftAndMoveTimes < RobotStatus.CALL_LIFT_AND_MOVE_TIMES) {
                                RobotStatus.callLiftAndMoveTimes++
                                BillManager.currentBill()?.addRetryOutLiftQueue()
                                //执行任务
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                            } else {
                                with(CloudMqttService) {
                                    // 电话报障
                                    publish(
                                        PhoneCallModel(
                                            number = "前台",
                                            note = "5",
                                            floor = RobotStatus.currentLocation?.floorName ?: ""
                                        ).toString()
                                    )
                                }
                                LogUtil.e("报障:电梯超次数")
                                BillManager.currentBill()?.exception()
                                DialogHelper.troubleDialog.show()
                                RobotStatus.callLiftAndMoveTimes = 0 //重置重试次数
                            }
                            "出电梯被围堵"
                        }
                        -63 -> {
                            // SUB_INFO_CODE_DEFAULT_OBSTACLE_ERROR
                            // #机器人常规导航(非电梯)被围堵时间过长
                            BillManager.currentBill()?.exception()
                            DialogHelper.troubleDialog.show()
                            CloudMqttService.publish(
                                PhoneCallModel(
                                    number = "前台",
                                    note = "2",
                                    floor = RobotStatus.currentLocation?.floorName ?: ""
                                ).toString()
                            )
                            "机器人常规导航(非电梯)被围堵时间过长"
                        }
                        -64 -> {
                            //SUB_INFO_CODE_DEFAULNAVIGATION_OUTTIME_ERROR
                            //机器人导航(非电梯)总时间超时
                            BillManager.currentBill()?.exception()
                            DialogHelper.troubleDialog.show()
                            CloudMqttService.publish(
                                PhoneCallModel(
                                    number = "前台",
                                    note = "2",
                                    floor = RobotStatus.currentLocation?.floorName ?: ""
                                ).toString()
                            )
                            "机器人导航(非电梯)总时间超时"
                        }
                        else -> {
                            LogUtil.e("未知 info_code = {} ${state.infoCode}")
                            "导航异常-导航请求终止"
                        }
                    }
                    ToastUtil.show(msg)
                    LogUtil.e(msg)
                } else if (state.state == 2) {
                    Stat.setFlage(2)
                } else if (state.state == 3) {
                    Stat.setFlage(3)
                }
            }
        }
    }

    private fun handleException() {
        mainScope.launch(Dispatchers.Default) {
            BillManager.currentBill()?.exception()
            DialogHelper.troubleDialog.show()
            CloudMqttService.publish(
                PhoneCallModel(
                    number = "前台",
                    note = "2",
                    floor = RobotStatus.currentLocation?.floorName ?: ""
                ).toString()
            )
        }
    }
}