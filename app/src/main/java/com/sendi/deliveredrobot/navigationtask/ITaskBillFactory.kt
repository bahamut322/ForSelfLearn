package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

/**
 * @author heky
 * @datee 2022-08-23
 * @description bill工厂接口
 */
interface ITaskBillFactory {
    fun createBill(taskModel: TaskModel?): List<ITaskBill>
}