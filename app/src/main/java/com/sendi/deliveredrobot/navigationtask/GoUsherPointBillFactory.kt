package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object GoUsherPointBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return GoUsherPointTaskBill(taskModel).billBuild()
    }
}