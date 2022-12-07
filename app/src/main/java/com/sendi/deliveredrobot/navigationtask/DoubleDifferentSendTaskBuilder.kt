package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel
import java.util.*

class DoubleDifferentSendTaskBuilder(private val taskModel: TaskModel?) {

    fun buildTaskList(): List<ITaskBill>{
        val date = Date()
        val tempList = LinkedList<ITaskBill>()
        tempList.addAll(listOf(DoubleDifferentSendTaskBillOne(taskModel = taskModel, date = date), DoubleDifferentSendTaskBillTwo(taskModel = taskModel, date = date)))
        return tempList
    }
}