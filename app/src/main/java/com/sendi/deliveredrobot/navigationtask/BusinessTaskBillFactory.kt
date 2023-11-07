package com.sendi.deliveredrobot.navigationtask

import com.sendi.deliveredrobot.model.TaskModel

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理任务清淡工厂实现类
 */
object BusinessTaskBillFactory : ITaskBillFactory {
    override fun createBill(taskModel: TaskModel?): List<ITaskBill> {
        return BusinessTaskBill(taskModel).billBuild()
    }
}