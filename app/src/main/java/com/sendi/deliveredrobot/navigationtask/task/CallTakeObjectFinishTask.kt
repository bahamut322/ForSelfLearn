package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-07
 * @description 呼叫取物结束
 */
class CallTakeObjectFinishTask(taskModel: TaskModel, type:Int, var exceptioned:Boolean = false) : AbstractTask(taskModel) {

    override suspend fun beforeReportData(taskDto: TaskDto) {
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                // 1号仓
                taskDto.status = when(viewModelBin1.value.previousRemoteOrderSendFinished){
                    true -> 1
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                // 2号仓
                taskDto.status = when(viewModelBin2.value.previousRemoteOrderSendFinished){
                    true -> 1
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        result
                    }
                }
            }
        }
        taskDto.apply {
            mileage = RobotMileageHelper.robotMileage()
        }
    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallTakeObjectFinishTask
    }

    override suspend fun execute() {
        taskModel?.bill?.executeNextTask()
    }
}