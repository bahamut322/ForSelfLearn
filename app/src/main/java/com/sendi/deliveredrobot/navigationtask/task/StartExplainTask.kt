package com.sendi.deliveredrobot.navigationtask.task

import chassis_msgs.SafeState
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.TYPE_EXPLAN
import com.sendi.deliveredrobot.TYPE_GUIDE
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskDto
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.view.widget.TaskArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @describe 讲解任务开始
 * @author Swn
 * @date 2021/11/15
 */
class StartExplainTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {


    override suspend fun beforeReportData(taskDto: TaskDto) {
        super.beforeReportData(taskDto)
    }

    override fun reportTaskDto() {
        if (Universal.twice) {
            ReportDataHelper.reportTaskStartDto(
                TYPE_EXPLAN, this@StartExplainTask.taskModel ?: TaskModel()
            )
            Universal.twice = false
        }

    }

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.ALLStartTask
    }

    override suspend fun execute() {
        RobotStatus.currentStatus = TYPE_GUIDE
//        withContext(Dispatchers.Main){
//            RobotStatus.targetName?.postValue(taskModel?.location?.pointName?:"")
//
//        }
//        TaskQueues.executeNextTask()
        SafeStateTopic.setSafeStateListener { safeState: SafeState ->
            if (safeState.safeState == SafeState.STATE_IS_TRIGGING) {
                // 按下
                TaskArray.setToDo("3")
                //播报语音音量
                MediaPlayerHelper.getInstance().pause()
                BaiduTTSHelper.getInstance().pause()
                if (RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_CONTINUE) {
                    CoroutineScope(Dispatchers.Default).launch {
                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                    }
                }
            }
            if (safeState.safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                // 抬起
                if (!Universal.speaking && !Universal.process && !Universal.changing && !Universal.finish) {
                    MediaPlayerHelper.getInstance().resume()
                    BaiduTTSHelper.getInstance().resume()
                }
                when (RobotStatus.manageStatus) {
                    RobotCommand.MANAGE_STATUS_STOP -> {
                        if (Universal.explainUnSpeak) {
                            TaskArray.setToDo("5")
                        }
                    }

                    RobotCommand.MANAGE_STATUS_PAUSE -> {
                        if (Universal.explainUnSpeak) {
                            TaskArray.setToDo("5")
                            return@setSafeStateListener
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
                        }
                    }
                }
            }
            null
        }
        taskModel?.bill?.executeNextTask()
    }
}