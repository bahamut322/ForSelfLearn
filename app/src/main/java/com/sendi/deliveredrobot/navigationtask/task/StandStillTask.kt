package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import android.widget.Toast
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO_HOME
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.TYPE_CHARGING
import com.sendi.deliveredrobot.TYPE_IDLE
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.model.PhoneCallModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 *   @author: heky
 *   @date: 2024-04-09
 *   @describe: 原地不动
 */
class StandStillTask(taskModel: TaskModel, needReportData: Boolean = true) : AbstractTask(taskModel, needReportData) {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.StandStillTask
    }

    override suspend fun execute() {
        RobotStatus.batteryStateNumber.postValue(true)
        RobotStatus.selectRouteMapItemId = -1
        Universal.businessTask = null
            val navigateId  = when (FunctionSkip.selectFunction()) {
                //智能引领
                0 -> {
                    ToastUtil.show("智能引领")
                    LogUtil.i("智能引领")
                    R.id.guidingFragment
                }
                //智能讲解
                1 -> {
                    ToastUtil.show( "智能讲解")
                    LogUtil.i("智能讲解")
                    R.id.ExplanationFragment
                }
                //智能问答
                2 -> {
                    ToastUtil.show("智能问答")
                    LogUtil.i("智能问答")
                    R.id.conversationFragment
                }
                //更多服务
                3 -> {
                    ToastUtil.show("更多服务")
                    LogUtil.i("更多服务")
                    R.id.appContentFragment
                }
                5 ->{
                    if (BuildConfig.DEBUG) {
                        ToastUtil.show("业务办理")
                    }
                    R.id.businessFragment
                }
                //不只有一个选项
                4 -> {
                    NAVIGATE_TO_HOME
                }

                -1 -> {
                    -1
                }
                else -> {
                    -1
                }
            }
            when(navigateId){
                -1 -> {
                    ToastUtil.show("请勾选主页面功能模块")
                    return
                }
                else -> {
                    MyApplication.instance!!.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, navigateId)
                    })
                }
            }
            UpdateReturn().method()
            taskModel?.bill?.executeNextTask()
    }
}