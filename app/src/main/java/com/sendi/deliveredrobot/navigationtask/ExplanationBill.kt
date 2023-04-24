package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object ExplanationBill: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return ExplainTaskBill(taskModel).billBuild()
    }
}