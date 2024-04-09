package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object StandStillBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return StandStillTaskBill(taskModel).billBuild()
    }
}