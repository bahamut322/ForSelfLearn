package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

/**
 * @author heky
 * @date 2022-08-23
 * @description 引领任务清单工厂实现类
 */
object GoBackTaskBillFactory: ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return GoBackTaskBill(taskModel).billBuild()
    }
}