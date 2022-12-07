package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

object SendTaskBillFactory: ITaskBillFactory {
    private const val TYPE_SINGLE = 1
    private const val TYPE_DOUBLE = 2

    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        val location = taskModel?.location
        val location2 = taskModel?.location2
        val type = when{
            location != null && location2 != null -> TYPE_DOUBLE
            else -> TYPE_SINGLE
        }
        return when (type) {
            TYPE_SINGLE -> SingleSendTaskBill(taskModel).billBuild()
            TYPE_DOUBLE -> {
                val pointId1 = location?.pointId
                val pointId2 = location2?.pointId
                val tempBill = if (pointId1 == pointId2) {
//                    DoubleSameSendTaskBill(taskModel)
                    DoubleSameSendTaskBuilder(taskModel).buildTaskList()
                }else{
//                    DoubleDifferentSendTaskBillOne(taskModel)
                    DoubleDifferentSendTaskBuilder(taskModel).buildTaskList()
                }
                return tempBill
            }
            else -> SingleSendTaskBill(taskModel).billBuild()
        }
    }
}