package com.sendi.deliveredrobot.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * @describe 上报机器人状态服务(每5秒）
 * @author heky
 * @date 2021-11-10
 */
class ReportRobotStateService : Service() {
    //1、创建任务每隔5s执行一次
    val timer = Timer()
    val mainScope = MainScope()
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        init()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun init() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                //2、构建实体
                mainScope.launch {
                    val data = generateData()
                    //3、上报
                    withContext(Dispatchers.Default){
                        CloudMqttService.publish(message = data.toString(), needPrintLog = BuildConfig.IS_DEBUG)
                    }
                }
            }
        },Date(), 5000)
    }

    /**
     * @describe 机器人每5s上报位置信息
     */
    private suspend fun generateData():RobotPoseLiveData{
        val robotPoseLiveData = RobotPoseLiveData()
        withContext(Dispatchers.Default){
            val currentTask = when(RobotStatus.inLiftFlow){
                true -> RobotStatus.expectLocation
                false -> RobotStatus.currentLocation
            }
            with(robotPoseLiveData){
                type = "livePoseData"
                val pose = ROSHelper.getPose()
                val poseArray = floatArrayOf(pose?.x?.toFloat()?:-1f,pose?.y?.toFloat()?:-1f,pose?.theta?.toFloat()?:-1f)
                robotPose = poseArray
                val tempArrayList = ArrayList<Float>()
                if (ROSHelper.getNowLaser()) {
                    for (floats in RosPointArrUtil.updateMap) {
                        floats.map {
                            tempArrayList.add(it)
                        }
                    }
                }
                updateMap = tempArrayList.toFloatArray()
                taskStatus = when(RobotStatus.currentStatus){
                    TYPE_CHARGING -> RobotStageEnum.CHARGING.code
                    TYPE_GUIDE, TYPE_SEND, TYPE_REMOTE_ORDER_SEND, TYPE_REMOTE_ORDER_TAKE -> RobotStageEnum.TASKING.code
                    TYPE_GO_BACK -> RobotStageEnum.BACKING.code
                    TYPE_IDLE -> RobotStageEnum.IDLE.code
                    TYPE_EXCEPTION -> RobotStageEnum.ERROR.code
                    else -> RobotStageEnum.ERROR.code
                }
                power = (RobotStatus.batteryPower.value?.times(100))?.toInt()?:0
                floor = currentTask?.floorName?:""
                time = System.currentTimeMillis()
                targetStage = BillManager.currentBill()?.firstPeek()?.configEnum()?.code?:TaskStageEnum.Idle.code
                target = BillManager.currentBill()?.currentTask()?.taskModel?.location?.pointName ?: ""
                endTarget = BillManager.currentBill()?.endTarget()
                nextTarget = BillManager.nextEndTarget()
                mode = RobotStatus.mode
                remain = BillManager.remainTaskCount()
            }
        }
        return robotPoseLiveData
    }

    companion object{
        /**
         * 开启服务
         */
        fun startService(mContext: Context) {
            mContext.startService(Intent(mContext, ReportRobotStateService::class.java))
        }
    }
}