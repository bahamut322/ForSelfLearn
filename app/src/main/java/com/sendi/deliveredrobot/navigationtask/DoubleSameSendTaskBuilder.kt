package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel
import java.util.*

/**
 * @author heky
 * @date 2022-08-30
 * @description 双仓相同
 */
class DoubleSameSendTaskBuilder(private val taskModel: TaskModel?) {

    fun buildTaskList(): List<ITaskBill>{
        val date = Date()
        val tempList = LinkedList<ITaskBill>()
        tempList.addAll(listOf(DoubleSameSendTaskBillOne(taskModel = taskModel, date = date), DoubleSameSendTaskBillTwo(taskModel = taskModel, date = date)))
        return tempList
    }
}