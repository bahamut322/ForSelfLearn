package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.TYPE_CHARGING
import com.sendi.deliveredrobot.TYPE_IDLE
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 *   @author: heky
 *   @date: 2021/8/24 18:07
 *   @describe: 全流程结束（此时在充电桩上）
 */
class AllFinishTask(taskModel: TaskModel) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.AllFinishTask
    }

    override suspend fun execute() {
        judgeNeedPhone()
        resetRobotStatus()
//        if (RobotStatus.autoCruise) {
//            virtualTaskExecute()
//            if (TopicHandler.navController.currentDestination?.label == "homeFragment") {
//                //自动巡航
//                if (TaskQueues.autoCruiseQueue.size > 0) {
//                    TaskQueues.queue.addAll(TaskQueues.autoCruiseQueue)
//                    TaskQueues.executeNextTask()
//                }
//            }
//        }
    }

    private fun resetRobotStatus() {
        RobotStatus.lowPowerBacking = false
        //机器人状态置为空闲
        if (RobotStatus.currentStatus != TYPE_CHARGING) {
            RobotStatus.currentStatus = TYPE_IDLE
        }
        BillManager.removeBill(taskModel?.bill)
    }

    /**
     * @describe 是否需要打电话通知前台有仓未取物
     */
    private fun judgeNeedPhone() {
        val needPhone = (!viewModelBin1.value.previousTaskFinished
                || !viewModelBin2.value.previousTaskFinished
                || !viewModelBin1.value.previousRemoteOrderSendFinished
                || !viewModelBin2.value.previousRemoteOrderSendFinished)
        if (needPhone) {
            val message = when {
                !viewModelBin1.value.previousTaskFinished && !viewModelBin2.value.previousTaskFinished -> "${viewModelBin1.value.place.value}${viewModelBin2.value.place.value}"
                !viewModelBin1.value.previousRemoteOrderSendFinished && !viewModelBin2.value.previousRemoteOrderSendFinished -> "${viewModelBin1.value.remoteOrderModel?.to?.pointName ?: ""}${viewModelBin2.value.remoteOrderModel?.to?.pointName ?: ""}"
                !viewModelBin1.value.previousTaskFinished && !viewModelBin2.value.previousRemoteOrderSendFinished -> "${viewModelBin1.value.place.value}${viewModelBin2.value.remoteOrderModel?.to?.pointName ?: ""}"
                !viewModelBin1.value.previousRemoteOrderSendFinished && !viewModelBin2.value.previousTaskFinished -> "${viewModelBin1.value.remoteOrderModel?.to?.pointName ?: ""}${viewModelBin2.value.place.value}"
                !viewModelBin1.value.previousTaskFinished -> viewModelBin1.value.place.value
                !viewModelBin2.value.previousTaskFinished -> viewModelBin2.value.place.value
                !viewModelBin1.value.previousRemoteOrderSendFinished -> viewModelBin1.value.remoteOrderModel?.to?.pointName
                    ?: ""
                !viewModelBin2.value.previousRemoteOrderSendFinished -> viewModelBin2.value.remoteOrderModel?.to?.pointName
                    ?: ""
                else -> ""
            }
            //通知前台
            CloudMqttService.publish(
                PhoneCallModel(
                    number = message ?: "",
                    note = "6",
                    floor = RobotStatus.currentLocation?.floorName ?: "1"
                ).toString()
            )
        }
    }
}