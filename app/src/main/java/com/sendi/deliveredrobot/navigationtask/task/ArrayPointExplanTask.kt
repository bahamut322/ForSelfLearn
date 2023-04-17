package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.MyApplication.Companion.instance
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.Order
import com.sendi.deliveredrobot.view.widget.TaskNext

/**
 * @Author Swn
 * @describe 到点讲解
 * @Data 2023-04-13 08:51
 */
class ArrayPointExplanTask(var status: Int = 1, taskModel: TaskModel) : AbstractTask(taskModel) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        taskDto.apply {
            status = status
            mileage = RobotMileageHelper.robotMileage()
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.FinishGuideTask
    }

    override suspend fun execute() {
        RobotStatus.ArrayPointExplan.postValue(1)
        LogUtil.i("TODO 到达讲解点")
        TaskNext.setOnChangeListener {
            if (TaskNext.getToDo() == "1"){
                LogUtil.i("TODO 到达讲解点${TaskNext.getToDo()}")
                BillManager.currentBill()?.executeNextTask()
                TaskNext.setToDo("0")
                RobotStatus.ArrayPointExplan.postValue(0)
            }
        }
    }
}
