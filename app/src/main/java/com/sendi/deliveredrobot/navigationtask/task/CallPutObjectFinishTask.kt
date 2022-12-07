package com.sendi.deliveredrobot.navigationtask.task

import com.sendi.deliveredrobot.helpers.RobotMileageHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum

/**
 * @author heky
 * @date 2022-06-07
 * @description 呼叫放物结束
 */
class CallPutObjectFinishTask(taskModel: TaskModel, type: Int, var exceptioned:Boolean = false) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.CallPutObjectFinishTask
    }

    override suspend fun beforeReportData(taskDto: TaskDto) {
        when (taskModel?.location?.binMark) {
            viewModelBin1.value.binMarkBin1 -> {
                // 1号仓
                taskDto.status = when(viewModelBin1.value.previousRemoteOrderPutFinished){
                    true -> 1
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        taskDto.apply {
                            mileage = RobotMileageHelper.robotMileage()
                        }
                        result
                    }
                }
            }
            viewModelBin2.value.binMarkBin2 -> {
                // 2号仓
                taskDto.status = when(viewModelBin2.value.previousRemoteOrderPutFinished){
                    true -> 1
                    false -> {
                        val result = when(exceptioned){
                            true -> -1
                            false -> 0
                        }
                        taskDto.apply {
                            mileage = RobotMileageHelper.robotMileage()
                        }
                        result
                    }
                }
            }
        }
    }

    override suspend fun execute() {
        taskModel?.bill?.executeNextTask()
    }
}