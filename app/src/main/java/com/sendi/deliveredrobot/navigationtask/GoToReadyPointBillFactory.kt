package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object GoToReadyPointBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return GoToReadyPointBill(taskModel).billBuild()
    }
}