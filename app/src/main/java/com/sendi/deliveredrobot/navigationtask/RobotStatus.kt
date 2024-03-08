package com.sendi.deliveredrobot.navigationtask

import android.app.Dialog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.RobotCommand.STOP_BUTTON_UNPRESSED
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
import geometry_msgs.Pose2D
import java.util.Date

/**
 *   @author: heky
 *   @date: 2021/8/19 11:20
 *   @describe: 机器人状态
 */
object RobotStatus {
    var SERIAL_NUMBER = "" //序列号
    const val CALL_LIFT_AND_MOVE_TIMES = 10 //重试电梯次数阈值
    const val RETRY_DOCK_MAX_TIMES = 3
    const val LOW_POWER_VALUE = 10 // 低电量阈值
    const val SHUT_DOWN_VALUE = 5 // 低电量阈值
    var callLiftAndMoveTimes = 0       //重试电梯次数
    var callingLift = false //正在呼叫电梯
    var inLiftFlow = false //正在电梯流程内（start: callLift, end: outLift)
    var mqttConnected = false //mqtt连接状态
    var retryDockTimes = 0      //重试自主充电次数
    var batteryStateNumber : MutableLiveData<Boolean> = MutableLiveData(false)//用于观察是否连接充电器（非适配器）来设置位置
    val sysTimeStamp : MutableLiveData<Long> = MutableLiveData(Date().time)//如果怕获取不到后台的时间，可以加上Date().time先设置系统时间进去
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
    var autoCruise = false //自动巡航
    var liftState = true //电梯可用状态
    var ready : MutableLiveData<Int> = MutableLiveData<Int>()
    val stopButtonPressed = MutableLiveData(STOP_BUTTON_UNPRESSED) //急停按钮是否按下
    var manageStatus: Int = -1 //状态机状态
    var needDelay = false //需要一定的延时
    var currentStatus = TYPE_IDLE //机器人当前状态
    var previousStatus = TYPE_IDLE
    var chassisVersionName = "" //底盘版本名字
    val versionStatusModel = MutableLiveData<VersionStatusModel>()//机器人版本状态
    val tenancy = MutableLiveData<ResponseTenancyModel>() //使用期限
    var odomPose: Pose2D? = null //里程计
    val PassWordToSetting : MutableLiveData<Boolean> = MutableLiveData<Boolean>()//监听密码是否输入正确
    var robotConfig : MutableLiveData<RobotConfig>? = MutableLiveData<RobotConfig>()//X8机器人配置
    var shoppingConfigList : MutableLiveData<ShoppingGuideConfing>? = MutableLiveData<ShoppingGuideConfing>()//导购配置
    var routeConfig : MutableLiveData<RouteConfig>? = MutableLiveData<RouteConfig>()//讲解路线配置
    var explainConfig : MutableLiveData<ExplainConfig>? = MutableLiveData<ExplainConfig>()//讲解配置
    var advertisingConfig : MutableLiveData<AdvertisingConfig>? = MutableLiveData<AdvertisingConfig>()
    var newUpdata = MutableLiveData<Int?>()//1:配置下载完成 ；2：数据存储到数据库，不代表配置下载完成 3:下载配置中提醒副屏幕变更成默认图片
    var onTouch : MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    var speakNumber : MutableLiveData<String> = MutableLiveData("")//记录智能讲解中断的之前朗读的文字个数
    var speakContinue : MutableLiveData<Int>? = MutableLiveData<Int>()//记录智能讲解朗读的内容
    var identifyFace : MutableLiveData<Int>? = MutableLiveData()//观察百度语音是否朗读完毕，之后进行人脸识别
    var sdScreenStatus: Int? = 0 // 0:空闲 1:测温 2:讲解 3:引领 4:导购 5:迎宾 6:轻应用
    var selectRoutMapItem : MutableLiveData<Int>? = MutableLiveData(-1)//选择的item
    var pointItem : MutableLiveData<Int>? = MutableLiveData(-1)//选择item中的列表的索引
    var targetName : MutableLiveData<String?>? = MutableLiveData()
    var progress : MutableLiveData<Int> = MutableLiveData(0)//文字朗读进度
    var ArrayPointExplan : MutableLiveData<Int> = MutableLiveData()//记录是否到点
    var explanationTaskFinish : MutableLiveData<Int> = MutableLiveData()//是否完成任务
    var ttsIsPlaying = false //百度语音播放状态
        set(value) {
//            when (value) {
//                true -> DialogHelper.loadingDialog.show()
//                false -> DialogHelper.loadingDialog.dismiss()
//            }
            Log.i("SpeakHelper", "ttsIsPlaying: $value")
            BaseVoiceRecorder.ttsIsPlaying = value
            field = value
        }

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
                TYPE_WELCOME -> 12
                else -> -1
            }
        }
}