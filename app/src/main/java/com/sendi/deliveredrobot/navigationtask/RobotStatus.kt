package com.sendi.deliveredrobot.navigationtask

import androidx.lifecycle.MutableLiveData
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.RobotCommand.STOP_BUTTON_UNPRESSED
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.navigationtask.task.ArrayPointExplanTask
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import geometry_msgs.Pose2D

/**
 *   @author: heky
 *   @date: 2021/8/19 11:20
 *   @describe: 机器人状态
 */
object RobotStatus {
    var SERIAL_NUMBER = "" //序列号
    const val CALL_LIFT_AND_MOVE_TIMES = 10 //重试电梯次数阈值
    const val RETRY_DOCK_MAX_TIMES = 3
    const val LOW_POWER_VALUE = 15 // 低电量阈值
    const val SHUT_DOWN_VALUE = 5 // 低电量阈值
    var callLiftAndMoveTimes = 0       //重试电梯次数
    var callingLift = false //正在呼叫电梯
    var inLiftFlow = false //正在电梯流程内（start: callLift, end: outLift)
    var mqttConnected = false //mqtt连接状态
    var retryDockTimes = 0      //重试自主充电次数
    var batteryStateNumber : MutableLiveData<Boolean> = MutableLiveData(false)//用于观察是否连接充电器（非适配器）来设置位置

    var bootLocation: QueryPointEntity? = null //开机点
    var originalLocation: QueryPointEntity? = null//原始点，默认为充电桩停靠点
    var currentLocation: QueryPointEntity? = null //记录当前所在楼层
    var liftCurrentLocation: QueryPointEntity? = null //记录电梯所在楼层
    var expectLocation: QueryPointEntity? = null //想去的楼层

    // 适配器状态 1-接入电源线 0-电源线拔出
    val adapterState: MutableLiveData<Byte> = MutableLiveData(-1)
    // 仓门状态
    val doorState = arrayListOf<Int>()
    //广告屏
    val mPresentation : MutableLiveData<Int?> = MutableLiveData()

    //电量
    val batteryPower: MutableLiveData<Float> = MutableLiveData(-1f)

    //电量供应状态
    val batterySupplyStatus: MutableLiveData<Byte> = MutableLiveData(-1)
    var chargeStatus: MutableLiveData<Boolean> = MutableLiveData(false) //是否充电中
//    val RobotStat : MutableLiveData<Int> = MutableLiveData()
    var selfChecking = 0 //0-正在自检，1-非自检
    var lowPowerBacking = false //低电量自动回充
    var docking = false //自主回充状态
    var targetAxis: QueryPointEntity? = null //当前切换的锚点
    var outOfLift = true //是否在电梯内
    var twoSamePlace = false //双送物任务同地点
    var autoCruise = false //自动巡航
    var liftState = true //电梯可用状态
    var ready : MutableLiveData<Int> = MutableLiveData<Int>()
    val stopButtonPressed = MutableLiveData(STOP_BUTTON_UNPRESSED) //急停按钮是否按下
    var manageStatus: Int = -1 //状态机状态
    var needDelay = false //需要一定的延时
    var currentStatus = TYPE_IDLE //机器人当前状态
    var previousStatus = TYPE_IDLE
    var sendFailType = -1 //送物失败类型
    var chassisVersionName = "" //底盘版本名字
    val versionStatusModel = MutableLiveData<VersionStatusModel>()//机器人版本状态
    val tenancy = MutableLiveData<ResponseTenancyModel>() //使用期限
    var odomPose: Pose2D? = null //里程计
    val PassWordToSetting : MutableLiveData<Boolean> = MutableLiveData<Boolean>()//监听密码是否输入正确
    var robotConfig : MutableLiveData<RobotConfig>? = MutableLiveData<RobotConfig>()//X8机器人配置
    var gatekeeper : MutableLiveData<Gatekeeper>?  = MutableLiveData<Gatekeeper>()//X8门岗配置
    var routeConfig : MutableLiveData<RouteConfig>? = MutableLiveData<RouteConfig>()//讲解路线配置
    var explainConfig : MutableLiveData<ExplainConfig>? = MutableLiveData<ExplainConfig>()//讲解配置
    var advertisingConfig : MutableLiveData<AdvertisingConfig>? = MutableLiveData<AdvertisingConfig>()
    var newUpdata : MutableLiveData<Int> = MutableLiveData<Int>()
    var onTouch : MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var speakNumber : MutableLiveData<String> = MutableLiveData();//记录智能讲解中断的之前朗读的文字个数
    var speakContinue : MutableLiveData<Int>? = MutableLiveData<Int>();//记录智能讲解朗读的内容
    var identifyFace : MutableLiveData<Int>? = MutableLiveData()//观察百度语音是否朗读完毕，之后进行人脸识别
    var sdScreenStatus : MutableLiveData<Int>? = MutableLiveData() // 0:空闲 1:测温 2:讲解
    var selectRoutMapItem : MutableLiveData<Int>? = MutableLiveData()//选择的item
    var SecondModel : MutableLiveData<SecondModel?>? = MutableLiveData()//讲解模式存储的副屏中的显示数据
    var targetName : MutableLiveData<String?>? = MutableLiveData()
    var progress : MutableLiveData<Int> = MutableLiveData()//文字朗读进度
    var ArrayPointExplan : MutableLiveData<Int> = MutableLiveData()//记录是否到点
    var explanationTaskFinish : MutableLiveData<Int> = MutableLiveData()//是否完成任务

    fun setStatus(status: Int){
        previousStatus = currentStatus
        currentStatus = status
    }

    /**
     * 任务模式 -1 - 空闲、10-客房送物、11-引领、20-跑腿帮取、21-跑腿帮送
     */
    val mode: Int
        get() {
            return when (currentStatus) {
                TYPE_GUIDE-> 11
                TYPE_SEND -> 10
                TYPE_REMOTE_ORDER_SEND -> 21
                TYPE_REMOTE_ORDER_TAKE -> 20
                else -> -1
            }
        }
}