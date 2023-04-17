package com.sendi.deliveredrobot.handler

import android.annotation.SuppressLint
import androidx.navigation.NavController
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.ros.observable.BinaryObserver
import com.sendi.deliveredrobot.ros.observable.Subject
import com.sendi.deliveredrobot.topic.*
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import navigation_base_msgs.State

/**
 * @describe topic处理
 */
@SuppressLint("StaticFieldLeak")
object TopicHandler {
    lateinit var navController: NavController
    private val mainScope = MainScope()
    lateinit var binaryObserver: BinaryObserver
//    private val mutex = Mutex()

    fun create(navController: NavController) {
        this.navController = navController
        // 1.客户端注册
        binaryObserver = object : BinaryObserver(Subject.getInstance()) {
            override fun receivedMessage(rosResult: RosResult<*>?) {
                super.receivedMessage(rosResult)
                when (rosResult?.url) {
                    ClientConstant.NAVIGATION_STATE_TOPIC -> {
                        handleNavigationStateTopic(rosResult)
                    }
                    ClientConstant.SAFE_STATE_TOPIC -> {
                        // 急停按钮状态
                        handleSafeStateTopic(rosResult)
                    }
                    ClientConstant.VOICE_PROMPT_TOPIC -> {
                        handleVoicePromptTopic(rosResult, navController)
                    }
                    ClientConstant.BATTERY_STATE -> {
                        // =============== 电池信息上报 =================
                        handleBatteryState(rosResult, navController)
                    }
                    ClientConstant.LASER_SCAN -> {
                        // 激光雷达数据
                        // 取消订阅并移除topic（只需要接收一次topic发布的消息，例如自检时镭射扫描状态上报，接收一次之后可以取消订阅）
                        SubManager.delSub(ClientConstant.LASER_SCAN)
                        mainScope.launch(Dispatchers.Main) {
                            CheckSelfHelper.laserCheckComplete.value = true
                        }
                    }
                    ClientConstant.ROBOT_POSE -> {
                        //路径地图创建路径上发的点
                        handleRoutePoseInfo(rosResult)
                    }
                    ClientConstant.LABEL_LIST -> {
                    }
                    ClientConstant.SCHEDULING_PAGE -> {
                        // 调度-调度页面
                        val response = rosResult.response as State
                        when (response.state) {
                            1 -> {
                                //正在调度
                                //TODO 跳转调度中页面

                            }
                            2 -> {
                                //结束调度
                                //TODO 调度结束，退出调度中页面
                            }
                        }
                    }
                    ClientConstant.SCHEDULING_CHANGE_GOAL -> {
                        handleSchedulingChangeGoal(rosResult)
                    }

                    ClientConstant.DOOR_STATE -> {
                        handleDoorState(rosResult)
                    }

                    ClientConstant.DOCK_STATE -> {
                        //自主充电
                        handleDockState(rosResult)
                    }

                    ClientConstant.SUB_MAP_INFO -> {
                        handleSubMapInfo(rosResult)
                    }
                    ClientConstant.PAUSE_CHECK -> {
                        handlePauseCheck(rosResult)
                    }
                    ClientConstant.GLOBAL_LASER -> {
                        // 重定位显示激光地图 - hard了一批,ma de
                        handleCurrentLaser(rosResult)
                    }
                    ClientConstant.NEAR_INDOOR_LIFT -> {
                        handleNearIndoorLift(rosResult)
                    }
                    ClientConstant.TEMP_OBSTACLE -> {
                        handleTempObstacle(rosResult)
                    }
                    ClientConstant.LORA_RECEIVE -> {
                        handleLoraReceive(rosResult)
                    }
                    ClientConstant.ROBOT_MILEAGE -> {
                        handleRobotMileage(rosResult)
                    }
                    ClientConstant.MAPPING_POSE -> {
                        handleMappingPose(rosResult)
                    }
                }
            }
        }
    }

    /**
     * @describe 实时路径点
     */
    private fun handleRoutePoseInfo(rosResult: RosResult<*>?) {
        RoutePoseInfoTopic.handle(rosResult)
    }

    /**
     * @describe 创建路径和目标点的实时激光点
     */
    private fun handleCurrentLaser(rosResult: RosResult<*>?) {
        CurrentLaserTopic.handle(rosResult)
    }


    /**
     * @describe 实时激光点
     */
    private fun handleSubMapInfo(rosResult: RosResult<*>?) {
        SubMapInfoTopic.handle(rosResult)
    }

    /**
     * @describe 40帧确认
     */
    private fun handlePauseCheck(rosResult: RosResult<*>?) {
        PauseCheckTopic.handle(rosResult)
    }

    /**
     * @describe 挡路
     */
    private fun handleVoicePromptTopic(
        rosResult: RosResult<*>?,
        navController: NavController
    ) {
        VoicePromptTopic.handle(rosResult,navController)
    }

    /**
     * @describe 电池上报
     */
    private fun handleBatteryState(
        rosResult: RosResult<*>?,
        navController: NavController
    ) {
        BatteryStateTopic.handle(rosResult,navController)
    }

    /**
     * @describe 调度
     */
    private fun handleSchedulingChangeGoal(rosResult: RosResult<*>?) {
        SchedulingChangeGoalTopic.handle(rosResult)
    }

    /**
     * @describe 仓门
     */
    private fun handleDoorState(rosResult: RosResult<*>?) {
        DoorStateTopic.handle(rosResult)
    }

    /**
     * @describe 自主充电
     */
    private fun handleDockState(rosResult: RosResult<*>?) {
        DockStateTopic.handle(rosResult)
    }

    /**
     * @describe 导航状态
     */
    private fun handleNavigationStateTopic(rosResult: RosResult<*>?) {
        NavigationStateTopic.handle(rosResult)
    }

    /**
     * @describe 急停按钮等
     */
    private fun handleSafeStateTopic(rosResult: RosResult<*>?) {
        SafeStateTopic.handle(rosResult)
    }

    /**
     * @describe 表示机器人到达电梯里面
     * RosResult类型State
     */
    private fun handleNearIndoorLift(rosResult: RosResult<*>) {
        NearIndoorLiftTopic.handle(rosResult)
    }

    /**
     * @describe 实时 限速区/虚拟墙 路径
     */
    private fun handleTempObstacle(rosResult: RosResult<*>) {
        TempObstacleTopic.handle(rosResult)
    }

    /**
     * @describe lora发送的消息
     */
    private fun handleLoraReceive(rosResult: RosResult<*>) {
        LoraReceiveTopic.handle(rosResult)
    }

    private fun handleRobotMileage(rosResult: RosResult<*>){
        RobotMileageTopic.handle(rosResult)
    }

    private fun handleMappingPose(rosResult: RosResult<*>){
        MappingPoseTopic.handle(rosResult)
    }
}