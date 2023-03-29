package com.sendi.deliveredrobot.topic

import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.navigationtask.task.DispatchWaitResumeTask
import com.sendi.deliveredrobot.ros.ClientManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.constant.Constant
import com.sendi.deliveredrobot.ros.dto.Client
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import geometry_msgs.Quaternion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nav_msgs.Odometry
import navigation_base_msgs.MoveToResponse
import rosapi.HasParamResponse
import java.util.concurrent.TimeUnit

object SchedulingChangeGoalTopic {
    private val mainScope = MainScope()
    private val mutex = Mutex()

    fun handle(rosResult: RosResult<*>?) {
        mainScope.launch(Dispatchers.Default) {
            mutex.withLock {
                // 调度-调度点接收处理下发
                // 一.解析消息
                val response = rosResult?.response as Odometry
                // 一.解析消息
                val x: Double = response.pose.pose.position.x
                val y: Double = response.pose.pose.position.y
                val z = 0.0
                val position = JSONObject().apply {
                    this[Constant.X] = x
                    this[Constant.Y] = y
                    this[Constant.Z] = z
                }
                val orientation: Quaternion =
                    response.pose.pose.orientation
                val targetPose = JSONObject().apply {
                    this[Constant.POSITION] = position
                    this[Constant.ORIENTATION] = orientation
                }
                val clientPara = HashMap<String, Any>().apply {
                    this[Constant.TARGET_POSE] = targetPose
                    this[Constant.DOCK_DIRECTION] = 1
                }
                val client = Client(ClientConstant.SCH_CHANGE_GOAL, clientPara)
                val clientResponse =
                    ClientManager.sendClientMsg(client).response as MoveToResponse
                if (clientResponse.result == 1) {
                    //                                         -2: "State machine error"
                    //                                         -3: "Running exists"
                    //                                        -24: "Not label"
                    var isHas = false
                    while (!isHas) {
                        val para = HashMap<String, Any>()
                        para["name"] = ClientConstant.RESUME_OLD_GOAL_FLAG
                        val hasParamService =
                            Client(ClientConstant.GET_PARAM, para)
                        val rosResult2 = ClientManager.sendClientMsg(hasParamService)
                        val clientResponse2 = rosResult2.response as HasParamResponse
                        isHas = clientResponse2.exists
                        // 设置参数
                        val paraMap = HashMap<String, Any>().apply {
                            this[ClientConstant.CHANGE_COAL_RESPONSE] = true
                        }
                        Client(ClientConstant.SET_PARAM, paraMap).let {
                            ClientManager.sendClientMsg(it)
                        }
                        try {
                            TimeUnit.SECONDS.sleep(2)
                        } catch (e: InterruptedException) {
                            LogUtil.e("SchedulingSub -> sleep(2) -> isHas:$isHas")
                        }
                    }
//                    TaskQueues.addFirst(DispatchWaitResumeTask())
//                    withContext(Dispatchers.Default) {
//                        TaskQueues.executeNextTask()
//                    }
                }
            }
        }
    }
}