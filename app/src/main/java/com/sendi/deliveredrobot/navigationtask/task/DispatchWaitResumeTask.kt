package com.sendi.deliveredrobot.navigationtask.task

import android.content.Intent
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.POP_BACK_STACK
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.AbstractTask
import com.sendi.deliveredrobot.ros.ClientManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.dto.Client
import com.sendi.deliveredrobot.service.TaskStageEnum
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import rosapi.GetParamResponse
import java.lang.Boolean
import java.util.*
import kotlin.Any
import kotlin.String
import kotlin.apply

/**
 *   @author: heky
 *   @date: 2021/7/22 18:04
 *   @describe: 调度完成等待恢复
 */
class DispatchWaitResumeTask : AbstractTask() {

    override fun configEnum(): TaskStageEnum {
        return TaskStageEnum.DispatchWaitResumeTask
    }

    override suspend fun execute() {
        //TODO 调度中 -> 等待调度恢复中
        //一定时间内 进行轮询操作检查 RESUME_OLD_GOAL_FLAG
        val para = HashMap<String, Any>()
        para["name"] = ClientConstant.RESUME_OLD_GOAL_FLAG
        //给底盘设置RESUME_OLD_GOAL_FLAG
        val hasParamClient = Client(ClientConstant.HAS_PARAM, para)
        ClientManager.sendClientMsg(hasParamClient)
        //询问底盘flag
        var seconds = 60
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                seconds--
                val getParamClient = Client(ClientConstant.SET_PARAM, para)
                val rosResult = ClientManager.sendClientMsg(getParamClient)
                val response =
                    JSONObject.parseObject(
                        rosResult.response.toString(),
                        GetParamResponse::class.java
                    )
                val flag = Boolean.parseBoolean(response.value)
                if (flag || seconds < 1) {
                    timer.cancel()
                    MyApplication.instance?.sendBroadcast(Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, POP_BACK_STACK)
                    })
                    MainScope().launch {
//                        TaskQueues.executeNextTask()
                        BillManager.currentBill()?.executeNextTask()
                    }
                }
            }
        }, Date(), 1000)

    }

}