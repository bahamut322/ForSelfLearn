package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.ACTION_SEND_TASK_FINISH
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @describe:某一段{送物}流程结束
 */
class FinishSendTask(taskModel: TaskModel, var exceptioned:Boolean = false, needReportData: Boolean = true ) : AbstractTask(taskModel, needReportData) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                // 1号仓
                viewModelBin1.value.resetBill()
                taskDto.status = when(viewModelBin1.value.previousTaskFinished){
                    true -> {
                        IdleGateDataHelper.addCount()
                        1
                    }
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                // 2号仓
                viewModelBin2.value.resetBill()
                taskDto.status = when(viewModelBin2.value.previousTaskFinished){
                    true -> {
                        IdleGateDataHelper.addCount()
                        1
                    }
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
        }
        taskDto.apply {
            mileage = RobotMileageHelper.robotMileage()
        }
        IdleGateDataHelper.reportIdleGateCount()
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishSendTask
    }

    override suspend fun execute() {
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_SEND_TASK_FINISH
        })
//        TaskQueues.popEndTarget()
//        TaskQueues.remainMinus()
//        TaskQueues.executeNextTask()
//        if(waitForResume) return
//        TaskQueues.executeNextTask()
//        if (BillManager.findNextBill() == null) {
//            // 后续没有任务，则返回充电桩
//            val bill = GoBackTaskBillFactory.createBill(taskModel)
//            BillManager.addAllLast(bill)
//        }
//        taskModel?.bill?.executeNextTask()
        BillManager.currentBill()?.executeNextTask()
    }
}