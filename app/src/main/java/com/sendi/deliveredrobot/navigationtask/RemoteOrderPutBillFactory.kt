package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RemoteOrderPutBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return RemoteOrderPutBill(taskModel).billBuild()
    }

    suspend fun addBillToQueue(billList: List<ITaskBill>){
        if (BillManager.currentBill() is GoBackTaskBill || RobotStatus.currentStatus == TYPE_GO_BACK || RobotStatus.currentStatus == TYPE_CHARGING || RobotStatus.currentStatus == TYPE_IDLE) {
            if(RobotStatus.docking){//重置重试次数
                RobotStatus.retryDockTimes = 0
                RobotStatus.docking = false
                ROSHelper.controlDock(RobotCommand.CMD_STOP)
            }
            BillManager.currentBill()?.earlyFinish()
            //step:0.5:
            BillManager.addAllAtIndex(billList)
            withContext(Dispatchers.Default){
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
            }
        }else{
            BillManager.addAllLast(billList)
        }
    }
}