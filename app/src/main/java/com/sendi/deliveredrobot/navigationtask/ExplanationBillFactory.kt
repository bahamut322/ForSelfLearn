package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object ExplanationBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return ExplainTaskBill(taskModel).billBuild()
    }

    fun createHeadBill(taskModel: TaskModel?): List<ITaskBill> {
        return ExplainTaskBill(taskModel, true).billBuild()
    }

    fun createRegularBill(taskModel: TaskModel?): List<ITaskBill> {
        return createBill(taskModel)
    }
}