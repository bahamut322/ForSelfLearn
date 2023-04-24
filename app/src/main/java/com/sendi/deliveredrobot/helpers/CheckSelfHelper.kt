package com.sendi.deliveredrobot.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.usb.UsbDevice
import android.media.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import chassis_msgs.DoorState
import com.infisense.iruvc.usb.UVCCamera
import com.infisense.iruvc.utils.SynchronizedBitmap
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.camera.IRUVC
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.ros.ClientManager
import com.sendi.deliveredrobot.ros.DispatchService
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.constant.Constant.CMD
import com.sendi.deliveredrobot.ros.dto.Client
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.service.DeliverMqttService
import com.sendi.deliveredrobot.service.MqttService
import com.sendi.deliveredrobot.service.ReportRobotStateService
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.*
import sendi_sensor_msgs.InfraredManageResponse
import java.util.*


/**
 *   @author: heky
 *   @date: 2021/7/12 16:44
 *   @describe: 自检
 */
object CheckSelfHelper {
    // 红外线状态
    private val infraredComplete = MutableLiveData(false)

    // 电量检测状态
    val powerCheckComplete = MutableLiveData(false)

    /** 镭射检测 */
    val laserCheckComplete = MutableLiveData(false)
    /**红外摄像头(测温)*/
    @SuppressLint("StaticFieldLeak")
    var p2camera: IRUVC? = null
    private val syncimage = SynchronizedBitmap()

//    // 急停按钮状态(默认-1)
//    private var stopButtonStatus = -1

    // 自测通过
    private var checkSelfComplete = false

    // 自测倒计时
    private var countdownComplete = false


    interface OnCheckChangeListener {
        //接口中的抽象函数,并携带数据
        fun onCheckProgress(progress: Int)
    }


    suspend fun checkHardware(time: Int, owner: LifecycleOwner, mOnCheckChangeListener:OnCheckChangeListener):Int {
        checkSelfComplete = false
        countdownComplete = false
        // 倒计时观察
        val seconds = MutableLiveData(time)
        var progress = 0
        var tempFlag = 384//转为二进制之后，对应自检
        var initRosTopic = false
        val preTopicList = listOf(
            ClientConstant.SCHEDULING_PAGE,
            ClientConstant.SAFE_STATE_TOPIC,
            ClientConstant.LASER_SCAN,
            ClientConstant.BATTERY_STATE,
            ClientConstant.NAVIGATION_STATE_TOPIC,
            ClientConstant.SCHEDULING_CHANGE_GOAL,
            ClientConstant.VOICE_PROMPT_TOPIC,
//            ClientConstant.DOOR_STATE,
            ClientConstant.DOCK_STATE,
            ClientConstant.NEAR_INDOOR_LIFT,
            ClientConstant.CHASSIS_MSGS_TOPIC,
            ClientConstant.LORA_RECEIVE,
            ClientConstant.ROBOT_MILEAGE
        )

        LogUtil.i("初始化Ros")
        DispatchService.initRosBridge("com.sendi.deliveredrobot.ros", MyApplication.instance)
        if(!ROSHelper.hasParam("/navigation_base/vaild")){
            delay(10000L)
        }else{
            var result: Boolean
            do {
                result = ROSHelper.getParam("/navigation_base/vaild") == "1"
                delay(100L)
            }while (!result)
        }
        //初始化机器人序列号
        RobotStatus.SERIAL_NUMBER = ROSHelper.getSerialNumber()
        LogUtil.i("SERIAL_NUMBER:${RobotStatus.SERIAL_NUMBER}")
        //设置底盘时间
        ROSHelper.updateTime()
        // ================================初始化云平台MQTT-SERVICE==============================
        CloudMqttService.startService(MainActivity.instance)
        DeliverMqttService.startService(MainActivity.instance)
        MqttService.startService(MainActivity.instance)
        // ================================初始化上报机器人信息SERVICE=============================
        if(BuildConfig.IS_REPORT){
            ReportRobotStateService.startService(MainActivity.instance)
        }
        withContext(Dispatchers.Main){
            seconds.observe(owner) { it ->
                if (it < 0) {
                    countdownComplete = true
                }
                val state = ROSHelper.checkStopButton()
                if(state == RobotCommand.STOP_BUTTON_PRESSED) {
                    RobotStatus.currentStatus = TYPE_EXCEPTION
                    return@observe
                }else{
                    if(RobotStatus.chargeStatus.value != true){
                        RobotStatus.currentStatus = TYPE_IDLE
                    }
                }
                it.minus(1)
            }
        }
        while (!checkSelfComplete && !countdownComplete) {
            LogUtil.i("=========LASER_SCAN===while")
            // ======================================订阅topic======================================
            /**
             * 代码初始化完成之后，会将所有topic添加到SubManager中
             */
            if(!initRosTopic){
                LogUtil.i("订阅topic")
//                delay(10000L)
                initRosTopic = DispatchService.subInitTopic(preTopicList)
            }

            if (initRosTopic) {
                val state = ROSHelper.checkStopButton()
                withContext(Dispatchers.Main) {
                    //自检
                    RobotStatus.stopButtonPressed.value = state
                }
                if(RobotStatus.stopButtonPressed.value == 1){
                    //急停已被按下
                    if (!DialogHelper.stopDialog.isShowing){
                        DialogHelper.stopDialog.show()
                    }
                    continue
                }
            }

            if(laserCheckComplete.value!! && powerCheckComplete.value!! &&
                RobotStatus.stopButtonPressed.value == 0){
                checkSelfComplete = true
            }
            LogUtil.i("=========LASER_SCAN===${laserCheckComplete.value}")
            if(laserCheckComplete.value!! && tempFlag and 0x01 == 0){
                tempFlag = tempFlag or 0x01
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("镭射检测通过")
            }
            if(powerCheckComplete.value!! && tempFlag and 0x02 == 0){
                tempFlag = tempFlag or 0x02
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("电量检测通过")
            }
            if(RobotStatus.stopButtonPressed.value == 0 && tempFlag and 0x04 == 0){
                tempFlag = tempFlag or 0x04
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("急停按钮检测通过")
            }
            if(RobotStatus.mPresentation.value == 1 && tempFlag and 0x08 == 0){
                tempFlag = tempFlag or 0x08
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                RobotStatus.newUpdata.postValue(1)
                LogUtil.i("副屏检测通过")
            }
            if (checkCameraHardware(MyApplication.context) && tempFlag and 0x10 == 0){
                tempFlag = tempFlag or 0x10
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("摄像头检测通过")
            }
            if (isMicrophoneAvailable() && tempFlag and 0x20 == 0){
                tempFlag = tempFlag or 0x20
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("麦克风检测通过")
            }
            if (isSpeakerAvailable() && tempFlag and 0x40 == 0){
                tempFlag = tempFlag or 0x40
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("扬声器检测通过")
            }
            if (temp() && tempFlag and 0x80 == 0){
                tempFlag = tempFlag or 0x80
                progress++
                //关闭红外
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("红外(测温)检测通过")
                if (p2camera != null) {
                    p2camera!!.unregisterUSB()
                    p2camera!!.stop()
                }
                syncimage.valid = false
            }
            if (checkInfrared() && tempFlag and 0x100 == 0){
                tempFlag = tempFlag or 0x100
                progress++
                mOnCheckChangeListener.onCheckProgress(progress)
                LogUtil.i("红外(机器人导航)检测通过")
            }
            delay(1000L)

            withContext(Dispatchers.Main) {
                seconds.value = seconds.value!!.minus(1)
            }
        }

        if (checkSelfComplete) {
            LogUtil.i("自检完成")
        }
        if (countdownComplete) {
            LogUtil.i("自检失败")
        }
        return tempFlag
    }


    /**
     * @describe 红外自检
     */
    private fun checkInfrared(): Boolean {
        val response:InfraredManageResponse
        val result:Int
        // ------------------------------------------------------------------------
        // 1.检查激光雷达数据 -> RobotStatus.scanAvaliable.get()
        // 2.检查红外摄像头 -> InfraredManageClient
        // ======================================调用服务======================================
        // 1.构建参数，无参调用可不传
        val clientPara = HashMap<String, Any>()
        clientPara[CMD] = 2
        val client = Client(ClientConstant.INFRARED_MANAGE, clientPara)
        // 2.调用ClientManager方法，接收返回值
        val rosResult = ClientManager.sendClientMsg(client)
        if(rosResult.response == null) return false
        try {
            response = rosResult.response as InfraredManageResponse
            result = response.result

        }catch (e:java.lang.Exception){
            return false
        }
        return result == 1

    }

    /**
     * @describe 检测仓门状态
     */
    private fun checkDoor() {
        // 检测仓门是否关闭
        var state =  ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK, door = DoorState.DOOR_ONE)
        RobotStatus.doorState.add(state)
        if(state != 2){
            LogUtil.i("仓门1未关闭,开机执行关闭")
            //仓门未关闭
            ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_ONE)
        }
        state =  ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK, door = DoorState.DOOR_TWO)
        RobotStatus.doorState.add(state)
        if(state != 2){
            LogUtil.i("仓门2未关闭,开机执行关闭")
            //仓门未关闭,执行关闭仓门
            ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_TWO)
        }
    }
    /**
     * 检测摄像头
     */
    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    open fun checkCameraHardware(context: Context): Boolean {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 检测设备是否有摄像头
            var camera: Camera? = null
            try {
                camera = Camera.open()
                // 打开摄像头
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (camera != null) {
                camera.release()
                // 释放摄像头
                return true
            }
        }
        return false
    }
    fun temp(): Boolean {
        if (p2camera == null) {
            p2camera = IRUVC(Universal.cameraHeight, Universal.cameraWidth, MyApplication.context, syncimage)
            p2camera?.registerUSB()
        }
        return !(p2camera!!.uvcCamera == null || !p2camera!!.uvcCamera.getOpenStatus())

    }
    /**
     * 麦克风检测
     */
    open fun isMicrophoneAvailable(): Boolean {
        var audioRecord: AudioRecord? = null
        return try {
            val bufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                false
            } else true
        } catch (e: java.lang.Exception) {
            false
        } finally {
            audioRecord?.release()
        }
    }
    /**
     * 扬声器检测
     */
    open fun isSpeakerAvailable(): Boolean {
        var audioTrack: AudioTrack? = null
        return try {
            val bufferSize = AudioTrack.getMinBufferSize(
                44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack(
                AudioManager.STREAM_VOICE_CALL, 44100, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM
            )
            audioTrack.play()
            true
        } catch (e: java.lang.Exception) {
            false
        } finally {
            audioTrack?.release()
        }
    }

}