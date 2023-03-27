package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.MyResultModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.launch

/**
 * @describe:引领中
 */
class GuidingTask(
    taskModel: TaskModel?, private val navigateId:Int
) : AbstractTask(taskModel) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.GuidingTask
    }

    override suspend fun execute() {
        if (taskModel!!.location == null) {
            ToastUtil.show(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            LogUtil.e(MyApplication.instance!!.getString(R.string.db_query_point_is_null))
            return
        }
        MyApplication.instance!!.sendBroadcast(Intent().apply {
            action = ACTION_NAVIGATE
            putExtra(NAVIGATE_ID, navigateId)
        })
        //step1设置速度
        ROSHelper.setSpeed("${basicSettingViewModel.value.basicConfig.guideSpeed}")
        ROSHelper.navigateTo(taskModel!!.location!!)
        DialogHelper.loadingDialog.dismiss()
//        //如果有讲解内容
//        if (taskModel!!.walkSpeak != null) {
//            //截取文字加入队列
//            RobotStatus.ready.postValue(1)
////            getLength(taskModel!!.walkSpeak)
//            RobotStatus.speakNumber.postValue(taskModel!!.walkSpeak)
//            //观察讲解是否结束
//            mainScope.launch {
//                    if (RobotStatus.speakContinue!!.value != 3) {
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                    } else {
//                        //TODO
//                        arrayToDo()
//                    }
//                }
//            }
//        //如果有mp3音屏
//        if (taskModel!!.walkMp3 != null) {
//            MediaPlayerHelper.play(taskModel!!.walkMp3 )
//            //mp3播放监听
//            MediaPlayerHelper.setOnProgressListener { currentPosition: Int, totalDuration: Int ->
//                LogUtil.i("currentPosition : $currentPosition totalDuration : $totalDuration")
//               mainScope.launch {
//                    if (currentPosition != totalDuration) {
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                    } else {
//                        //TODO
//                        arrayToDo()
//                    }
//                }
//            }
//        }
//    }
//    fun arrayToDo() {
//        mainScope.launch {
//            if (taskModel!!.arraySpeak != null) {
//                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                RobotStatus.ready.postValue(2)
//                if (RobotStatus.speakContinue!!.value == 3) {
//                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                }
//            } else if (taskModel!!.arrayMp3 != null) {
//                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                MediaPlayerHelper.play(taskModel!!.arrayMp3)
//                MediaPlayerHelper.setOnProgressListener { currentPosition: Int, totalDuration: Int ->
//                    mainScope.launch {
//                        if (currentPosition != totalDuration) {
//                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                        }else{
//                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                        }
//                    }
//                }
//            }
//        }
    }
}