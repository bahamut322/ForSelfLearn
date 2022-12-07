package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel
import java.lang.Exception

object RemoteOrderSendBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        throw Exception("error function")
    }

    fun createBill(taskModel: TaskModel?, type: Int): List<ITaskBill> {
        return RemoteOrderSendBill(taskModel, type).billBuild()
    }
}