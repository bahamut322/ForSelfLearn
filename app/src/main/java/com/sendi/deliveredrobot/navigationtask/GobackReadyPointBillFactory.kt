package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object GoBackReadyPointBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return GoBackReadyPointTaskBill(taskModel).billBuild()
    }
}