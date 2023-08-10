package com.sendi.deliveredrobot.navigationtask.task

import android.util.Log
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.service.TaskStageEnum
import java.util.*
import kotlin.math.log

/**
 * @author heky
 * @date 2022-06-06
 * @description 判断是否同层任务
 */
class JudgeFloorTask(taskModel: TaskModel, private val type: Int, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {
    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.JudgeFloorTask
    }

    override suspend fun execute() {
        DialogHelper.loadingDialog.show()
        val fromFloor = RobotStatus.currentLocation
        val toFloor = taskModel?.location
        val tempQueue = LinkedList<AbstractTask>()
        val tempTaskId = taskModel?.taskId?:""
        if (toFloor?.subMapId != fromFloor?.subMapId) {
            // 查看当前楼层与目标楼层是否包含与同一个电梯中
            val needTransferResultModel = LiftHelper.needTransfer(fromFloorName = fromFloor?.floorName?:"", toFloorName = toFloor?.floorName?:"")
            val toLocation = when(needTransferResultModel.needTransfer){
                true -> dao.queryLiftPointByFloorName(needTransferResultModel.transferFloorName, needTransferResultModel.elevator)
                false -> taskModel?.location
            }
            tempQueue.apply {
                Log.d("TAG", "execute1: "+taskModel?.location)
                Log.d("TAG", "execute2: "+taskModel?.endTarget ?: "")
                Log.d("TAG", "execute3: "+tempTaskId)
                add(
                    PreLoadMapTask(
                        TaskModel(
                            location = taskModel?.location,
                            endTarget = taskModel?.endTarget ?: "",
                            taskId = tempTaskId,
                            bill= taskModel?.bill
                        )
                    )
                )
                val liftOutSide = dao.queryLiftPoint(
                    subMapId = RobotStatus.currentLocation?.subMapId!!,
                    type = PointType.LIFT_OUTSIDE,
                    elevator = needTransferResultModel.elevator
                )
                // 不同层
                when (type) {
                    TYPE_GUIDE -> {
                        add(
                            AdvanceGuidingTask(
                                3,
                                TaskModel(
                                    location = liftOutSide,
                                    endTarget = taskModel?.endTarget?:"",
                                    taskId = tempTaskId,
                                    bill= taskModel?.bill
                                ),
                                R.id.guidingFragment
                            )
                        )
                    }
                    TYPE_SEND -> {
                        add(
                            AdvanceSendingTask(
                                3,
                                TaskModel(
                                    location = liftOutSide?.apply {
                                        binMark = taskModel?.location?.binMark
                                    },
                                    endTarget = taskModel?.endTarget?:"",
                                    taskId = tempTaskId,
                                    bill= taskModel?.bill
                                )
                            )
                        )
                    }
                    TYPE_GO_BACK -> {
                        add(
                            AdvanceGuidingTask(
                                3,
                                TaskModel(
                                    location = liftOutSide,
                                    endTarget = taskModel?.endTarget?:"",
                                    taskId = tempTaskId,
                                    bill= taskModel?.bill
                                ),
                                R.id.goBackFragment
                            )
                        )
                    }
                }

                // step 4：呼叫电梯task
                add(
                    CallLiftTask(
                        TaskModel(
                            location = liftOutSide,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                add(ScanLiftTask(
                    TaskModel(
                        location = liftOutSide,
                        endTarget = taskModel?.endTarget?:"",
                        taskId = tempTaskId,
                        bill= taskModel?.bill,
                        elevator = needTransferResultModel.elevator
                    )
                ))
                // step 5: 进电梯
                add(
                    IntoLiftTask(
                        TaskModel(
                            location = RobotStatus.currentLocation,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                add(
                    IntoLiftFinishTask(
                        TaskModel(
                            location = RobotStatus.currentLocation,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                add(
                    SwitchSubMapTask(
                        TaskModel(
                            location = toLocation,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                // step 6: 到楼层task
                add(
                    LiftMoveTask(
                        TaskModel(
                            location = toLocation,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                // step 7：出电梯task
                add(
                    OutLiftTask(
                        TaskModel(
                            location = toLocation,
                            endTarget = taskModel?.endTarget?:"",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                // step 8:出电梯完成
                add(
                    OutLiftFinishTask(
                        TaskModel(
                            location = toLocation,
                            endTarget = taskModel?.endTarget ?: "",
                            taskId = tempTaskId,
                            bill= taskModel?.bill,
                            elevator = needTransferResultModel.elevator
                        )
                    )
                )
                if (needTransferResultModel.needTransfer) {
                    // 需要换乘
                    // 如果不是，JudgeFloorTask
                    add(
                        JudgeFloorTask(taskModel?: TaskModel(), type)
                    )
                }
            }
            taskModel?.bill?.addAll(0,tempQueue)
        }
//        TaskQueue.executeNextTask()
        taskModel?.bill?.executeNextTask()
        DialogHelper.loadingDialog.dismiss()
    }
}