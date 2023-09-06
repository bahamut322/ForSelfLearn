package com.sendi.deliveredrobot.helpers

import android.annotation.SuppressLint
import android.util.Log
import chassis_msgs.*
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.handler.TopicHandler
import com.sendi.deliveredrobot.model.LiftControlLoraModel
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.PointCompat
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecuteFloat
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.ros.ClientManager
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.constant.Constant
import com.sendi.deliveredrobot.ros.constant.RosResultEnum
import com.sendi.deliveredrobot.ros.dto.Client
import com.sendi.deliveredrobot.ros.dto.RosResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import geometry_msgs.Pose2D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import map_msgs.Multi_axisResponse
import map_msgs.Set_poseResponse
import navigation_base_msgs.*
import org.ros.rosjava_geometry.Quaternion
import org.ros.rosjava_geometry.Vector3
import rosapi.GetParamResponse
import rosapi.HasParamResponse
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

object ROSHelper {
    private val mutex = Mutex()
    @SuppressLint("SimpleDateFormat")
    private val sdf1 = SimpleDateFormat("MM/dd/yyyy")
    @SuppressLint("SimpleDateFormat")
    private val sdf2 = SimpleDateFormat("HH:mm:ss")
    /**
     * @describe 控制导航状态
     * 1、终止     2、暂停    3、继续
     */
    suspend fun manageRobot(status: Int): Boolean {
        var response: ManageResponse? = null
        DialogHelper.loadingDialog.show()
        withContext(Dispatchers.Default){
            val param = HashMap<String, Any>()
            param[Constant.CMD] = status
            val rosResult =
                ClientManager.sendClientMsg(Client(ClientConstant.MANAGE, param))
            if (rosResult.isFlag) {
                if (rosResult.response == null) return@withContext false
                response = rosResult.response as ManageResponse
                val msg = when (response?.result) {
                    1 -> {
                        if(status == 2){
                            BaiduTTSHelper.getInstance().pause()
                        }
                        else if (status == 3 && !Universal.speakIng){
                            BaiduTTSHelper.getInstance().resume()
                        }
                        "控制导航状态成功"
                    }
                    -2 -> {
                        LogUtil.i("State machine error")
                        "控制导航状态:导航异常-不符合状态机"
                    }
                    else -> {
                        LogUtil.e("Other mistakes, result = $rosResult")
                        "控制导航状态:导航异常-未知错误"
                    }
                }
                LogUtil.i(msg)
            } else {
                return@withContext false
            }
        }
        DialogHelper.loadingDialog.dismiss()
        return response?.result == 1
    }

    /**
     * @describe 设置地图
     */
    fun setNavigationMap(labelMapName: String, pathMapName: String): Boolean {
        val clientParam = HashMap<String, Any>()
        clientParam["label_map_name"] = labelMapName
        clientParam["path_map_name"] = pathMapName
        val setLocationMapClient = Client(ClientConstant.SET_NAVIGATION_MAP, clientParam)
        val rosResultSetNavigationMap = ClientManager.sendClientMsg(setLocationMapClient)
        if (rosResultSetNavigationMap.isFlag) {
            val response = rosResultSetNavigationMap.response as SetMoveMapResponse
            if (response.result == 1) {
                LogUtil.i("设置地图成功")
            } else {
                LogUtil.i("设置地图失败")
                ToastUtil.show(MyApplication.instance!!.getString(R.string.set_map_fail))
            }
            return response.result == 1
        }
        return false
    }

    /**
     * @describe 设置速度
     */
    fun setSpeed(speed: String) {
        val clientParamSpeed = HashMap<String, Any>()
        clientParamSpeed["name"] = ClientConstant.SET_SPEED
        clientParamSpeed["value"] = speed
        val speedClient = Client(ClientConstant.SET_PARAM, clientParamSpeed)
        val rosResultSpeed = ClientManager.sendClientMsg(speedClient)
        if (rosResultSpeed.isFlag) {
            LogUtil.i("设置速度成功")
        }
    }

    /**
     * @describe 导航到
     */
    fun navigateTo(location: QueryPointEntity): Int{
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = 0.0
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientMoveTo = Client(ClientConstant.MOVE_TO, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(clientMoveTo)
        var result = -1
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as MoveToResponse
            val msg = when (response.result) {
                1 -> {
                    "导航成功"
                }
                -2 -> {
                    "导航异常-状态错误"
                }
                -3 -> {
                    "导航异常-已经运行"
                }
                -24 -> {
                    "导航异常-看不到地图对应的标签"
                }
                -34 -> {
                    sendFakeSafeTopic()
                    "导航异常-急停被按下"
                }
                else -> {
                    "导航异常-其他错误"
                }
            }
            result = response.result
            LogUtil.i(msg)
        }
        return result
    }

    private fun sendFakeSafeTopic() {
        val rosResult = RosResult<SafeState>().apply {
            this.url = ClientConstant.SAFE_STATE_TOPIC
            this.isFlag = true
            val safeStateJson = """
                           {"header": {"stamp": {"secs": 1677739073, "nsecs": 721510500}, "frame_id": "", "seq": 1048}, "safe_type": 1, "safe_state": 1}
                        """.trimIndent()
            //                        val gson = Gson()
            //                        val safeState = gson.fromJson(safeStateJson, SafeState::class.java)

            val safeState = JSONObject.parseObject(safeStateJson, SafeState::class.java)
            this.response = safeState
        }

        TopicHandler.binaryObserver.receivedMessage(rosResult)
    }

    /**
     * @describe 扫描电梯
     */
    fun scanLift(currentAxis: QueryPointEntity?, targetAxis: QueryPointEntity?): Boolean {
        if(currentAxis == null || targetAxis == null) return false
        val clientPara = HashMap<String, Any>()
        // currentAxis
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = currentAxis.x
        position[Constant.Y] = currentAxis.y
        position[Constant.Z] = currentAxis.w
        targetPose[Constant.POSITION] = position
        // targetAxis
        val targetPose2 = JSONObject()
        val position2 = JSONObject()
        position2[Constant.X] = targetAxis.x
        position2[Constant.Y] = targetAxis.y
        position2[Constant.Z] = targetAxis.w
        targetPose2[Constant.POSITION] = position2
        // clientPara
        clientPara["current_axis"] = targetPose
        clientPara["target_axis"] = targetPose2
        val scanLiftClient = Client(ClientConstant.CHECK_LIFT_VAILD, clientPara)
        val rosResult = ClientManager.sendClientMsg(scanLiftClient)
        if (rosResult.isFlag) {
            val response = rosResult.response as Multi_axisResponse
            return response.result == 1
        }
        return false
    }

    /**
     * @describe 切换锚点
     */
    fun setMultiAxis(currentAxis: QueryPointEntity, targetAxis: QueryPointEntity): Boolean {
        val clientPara = HashMap<String, Any>()
        // currentAxis
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = currentAxis.x
        position[Constant.Y] = currentAxis.y
        position[Constant.Z] = currentAxis.w
        targetPose[Constant.POSITION] = position
        // targetAxis
        val targetPose2 = JSONObject()
        val position2 = JSONObject()
        position2[Constant.X] = targetAxis.x
        position2[Constant.Y] = targetAxis.y
        position2[Constant.Z] = targetAxis.w
        targetPose2[Constant.POSITION] = position2
        // clientPara
        clientPara["current_axis"] = targetPose
        clientPara["target_axis"] = targetPose2
        val setMultiAxisClient = Client(ClientConstant.SET_MULTI_AXIS, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(setMultiAxisClient)
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as Multi_axisResponse
            val msg = when (response.result) {
                1 -> {
                    "切换锚点成功：${response.result}"
                }
                -1 -> {
                    "切换锚点未知错误：${response.result}"
                }
                else -> {
                    "切换锚点未知错误:${response.result}"
                }
            }
            LogUtil.i(msg)
            return response.result == 1
        }
        return false

    }

    /**
     * @describe 进入电梯
     */
    fun enterLift(location: QueryPointEntity?): Int {
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location?.x
        position[Constant.Y] = location?.y
        position[Constant.Z] = 0.0
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location?.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientMoveTo = Client(ClientConstant.GO_TO_LIFT, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(clientMoveTo)
        var result = -1
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as MoveToResponse
            val msg = when (response.result) {
                1 -> {
                    "进入电梯成功"
                }
                -1 -> {
                    "进入电梯:未知错误:-1"
                }
                -2 -> {
                    "进入电梯:状态错误"
                }
                -3 -> {
                    "进入电梯:线程重复错误"
                }
                -34 -> {
                    sendFakeSafeTopic()
                    "导航异常-急停被按下"
                }
                else -> {
                    "进入电梯:未知错误"
                }
            }
            result = response.result
            LogUtil.i(msg)
        }
        return result
    }

    /**
     * @describe 出电梯
     */
    fun outLift(location: QueryPointEntity?): Int {
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location?.x
        position[Constant.Y] = location?.y
        position[Constant.Z] = 0.0
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location?.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientMoveTo = Client(ClientConstant.OUT_OF_LIFT, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(clientMoveTo)
        var result = -1
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as MoveToResponse
            val msg = when (response.result) {
                1 -> {
                    "出电梯成功"
                }
                -1 -> {
                    "出电梯:未知错误"
                }
                -2 -> {
                    "出电梯:状态错误"
                }
                -3 -> {
                    "出电梯:线程重复错误"
                }
                -34 -> {
                    sendFakeSafeTopic()
                    "导航异常-急停被按下"
                }
                else -> {
                    "出电梯:未知错误"
                }
            }
            result = response.result
            LogUtil.i(msg)
        }
        return result
    }

    /**
     * @describe 开舱门
     */
    fun controlBin(cmd: Byte, door: Byte): Int {
        val clientPara = HashMap<String, Any>()
        clientPara[Constant.CMD] = cmd
        clientPara["door"] = door
        val doorCommand = Client(ClientConstant.DOOR_COMMAND, clientPara)
        val doorCommandResponse = ClientManager.sendClientMsg(doorCommand)
        if (doorCommandResponse.response == null) return -1
        try {
            val response = doorCommandResponse.response as DoorCommandResponse
            if (doorCommandResponse.isFlag) {
                val msg = when (response.result) {
                    0.toByte() -> {
//                    if (cmd != RobotCommand.CMD_CHECK) {
//                        DialogHelper.loadingDialog.show()
//                    }
                        "控制仓门成功"
                    }
                    1.toByte() -> {
                        "控制仓门失败"
                    }
                    else -> {
                        "控制仓门失败"
                    }
                }
                LogUtil.i(msg)
            }
            return response.state.toInt()
        }catch (e: Exception){
            return -1
        }
    }

    fun checkStopButton(): Int {
        // 检测急停按钮是否在开机前被按下，正常被按下会主动发送，开机前被按下不会发送
        // build para
        val clientPara = java.util.HashMap<String, Any>()
        clientPara[Constant.TYPE] = ChassisStateRequest.EMERGENCY_STOP_STATE
        val client = Client(ClientConstant.CHASSIC_STATE, clientPara)
        // send Msg
        val rosResult = ClientManager.sendClientMsg(client)
        if (rosResult.response == null) return -1
        val response: ChassisStateResponse = rosResult.response as ChassisStateResponse
        return response.state
    }

    //设置默认充电点获取评分
    fun setPoseClient(location: QueryPointEntity): Int {
        val clientPara = java.util.HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = location.w
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w?:0.0)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.POSE] = targetPose
        val clientSetPose = Client(ClientConstant.SET_POSE, clientPara)
        val rosResultSetPose = ClientManager.sendClientMsg(clientSetPose)
        var score = 0
        if (rosResultSetPose.isFlag) {
            val response = rosResultSetPose.response as Set_poseResponse
            score = response.result
        }
        return score
    }

    fun setChargePose(location: QueryPointEntity): Int {
        val clientPara = java.util.HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = location.w
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w?:0.0)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.POSE] = targetPose
        val clientSetPose = Client(ClientConstant.SET_CHARGE_POSE, clientPara)
        Log.d("TAG", "对接充电桩发送日志: $clientSetPose")
        val rosResultSetPose = ClientManager.sendClientMsg(clientSetPose)
        var score = 0
        if (rosResultSetPose.isFlag) {
            val response = rosResultSetPose.response as Multi_axisResponse
            score = response.result
        }
        return score
    }

    //获取默认充电点获取评分
    fun checkPoseClient(location: QueryPointEntity): Int {
        val clientPara = java.util.HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = location.w
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.POSE] = targetPose
        val clientSetPose = Client(ClientConstant.CHECK_POSE, clientPara)
        val rosResultSetPose = ClientManager.sendClientMsg(clientSetPose)
        var score = 0
        if (rosResultSetPose.isFlag) {
            val response = rosResultSetPose.response as Set_poseResponse
            score = response.result
        }
        return score
    }


    /**
     * @describe: 自主充电
     */
    fun controlDock(cmd: Int): Boolean {
        val clientPara = HashMap<String, Any>()
        clientPara[Constant.CMD] = cmd
        val command = Client(ClientConstant.DOCK_COMMAND, clientPara)
        val dockCommandResponse = ClientManager.sendClientMsg(command)
        if (dockCommandResponse.isFlag) {
            val response = dockCommandResponse.response as DockCommandResponse
            val msg = when (response.result) {
                DockCommandResponse.SUCCEED -> {
                    "控制dock成功"
                }
                DockCommandResponse.FAILED -> {
                    "控制dock失败"
                }
                else -> {
                    "控制dock失败"
                }
            }
            LogUtil.i(msg)
            return response.result == DockCommandResponse.SUCCEED
        }
        return false
    }

    /**
     * @describe: 获取序列号
     */
    fun getSerialNumber(): String {
        val para = java.util.HashMap<String, Any>()
        para["name"] = ClientConstant.SERIAL_NUMBER
        val getParamClient = Client(ClientConstant.GET_PARAM, para)
        val rosResult = ClientManager.sendClientMsg(getParamClient)
        if (rosResult.isFlag) {
            val response =
                JSONObject.parseObject(
                    rosResult.response.toString(),
                    GetParamResponse::class.java
                )
            return response.value.replace("\"", "", true)
        }
        return ""
    }

    /**
     * @describe 关机
     */
    fun shutDown() {
        val clientPara = HashMap<String, Any>()
        clientPara[Constant.CMD] = 1
        val shutdownCommand = Client(ClientConstant.LOW_POWER_SHUTDOWN, clientPara)
        val shutdownResponse = ClientManager.sendClientMsg(shutdownCommand)
        if (shutdownResponse.isFlag) {
            val response = shutdownResponse.response as Stm32CommandResponse
            val msg = when (response.result) {
                DockCommandResponse.SUCCEED -> {
                    "关机成功"
                }
                DockCommandResponse.FAILED -> {
                    "关机失败"
                }
                else -> {
                    "关机失败"
                }
            }
            LogUtil.i(msg)
        }
    }

    /**
     * @describe 获取当前锚点
     */
    fun getPose(): Pose2D? {
        val pose2D: Pose2D? = null
        val clientPara = HashMap<String, Any>()
        val getPoseCommand = Client(ClientConstant.GET_POSE, clientPara)
        val getPoseResponse = ClientManager.sendClientMsg(getPoseCommand)
        if (getPoseResponse.isFlag) {
            val response = getPoseResponse.response as TargetPoseResponse
            return response.pose
        }
        return pose2D
    }

    /**
     * @describe 设置电梯内点
     */
    fun sendInDoorPoint(location: QueryPointEntity?): Boolean {
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location?.x
        position[Constant.Y] = location?.y
        position[Constant.Z] = location?.w
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location?.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientReceiveLiftIndoor = Client(ClientConstant.RECV_LIFT_INDOOR, clientPara)
        val rosResultReceiveLiftIndoor = ClientManager.sendClientMsg(clientReceiveLiftIndoor)
        return rosResultReceiveLiftIndoor.isFlag
    }

    /**
     * @describe 封装一个开启线程控制状态机的方法
     */
    suspend fun manageRobotUntilDone(cmd: Int) : Boolean {
        val result = withContext(Dispatchers.Default) {
            mutex.withLock {
                var result: Boolean = manageRobot(cmd)
                var times = 10
                while (!result && times-- > 0) {
                    result = manageRobot(cmd)
                    virtualTaskExecuteFloat(2f, "manageRobot")
                }
                times > 0
//            RobotStatus.manageStatus = cmd
            }
        }
        return result
    }

    fun getParam(name: String): String{
        val para = java.util.HashMap<String, Any>()
        para["name"] = name
        val getParamService =
            Client(ClientConstant.GET_PARAM, para)
        val rosResult = ClientManager.sendClientMsg(getParamService)
        if (!rosResult.isFlag) return "0"
        val clientResponse = rosResult.response as GetParamResponse
        return clientResponse.value.toString()
    }

    /**
     * @describe 充电点、非出梯到电梯外点
     * @param cmd: 1 去充电    2 去充电修复电    3 非进出梯的时候从别的房间到电梯外点
     */
    fun advanceMoveTo(cmd: Int,location: QueryPointEntity): Int{
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = 0.0
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.CMD] = cmd
        clientPara["cmd2"] = 0
        clientPara["cmd3"] = 0
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientAdvanceMoveTo = Client(ClientConstant.ADVAN_MOVE_TO, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(clientAdvanceMoveTo)
        var result = -1
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as AdvanMoveToResponse
            val msg = when (response.result) {
                1 -> {
                    "导航成功"
                }
                -2 -> {
                    "导航异常-状态错误"
                }
                -3 -> {
                    "导航异常-已经运行"
                }
                -24 -> {
                    "导航异常-看不到地图对应的标签"
                }
                -34 -> {
                    sendFakeSafeTopic()
                    "导航异常-急停被按下"
                }
                else -> {
                    "导航异常-其他错误"
                }
            }
            result = response.result
            LogUtil.i(msg)
        }
        return result
    }

    /**
     * @describe 获取实时激光点
     */
    fun getNowLaser(): Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["diretion"] = 1
        clientPara["value"] = 1
        val getNowLaser = Client(ClientConstant.GET_NOW_LASER, clientPara)
        val getNowLaserResponse = ClientManager.sendClientMsg(getNowLaser)
        return getNowLaserResponse.isFlag
    }

    /**
     * @describe 创建限速区
     */
    fun startCreateLimitSpeed():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 3
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if (response.isFlag) {
            SubManager.sub(ClientConstant.TEMP_OBSTACLE)
        }
        return response.isFlag
    }

    /**
     * @describe 重置限速区
     */
    fun resetLimitSpeed():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 4
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 设置限速区参数
     * @param type 0:坡道     1：其他
     */
    fun setLimitSpeedParam(name: String, type:Int = 0, radius:Float = 0.5f, speed: Float = 0.4f, visibleRange:Float = 1f ): Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 6
        clientPara["name"] = name
        clientPara["type"] = type
        clientPara["range"] = radius
        clientPara["speed"] = speed
        clientPara["distance"] = visibleRange
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if (response.isFlag){
            SubManager.unsub(ClientConstant.TEMP_OBSTACLE)
        }
        return response.isFlag
    }

    /**
     * @describe 获取LineNameList
     */
    fun getLimitSpeedLineNameList(laserMapName: String): List<String>?{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 2
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if(response.isFlag){
            val createSlowAreaResponse = response.response as CreateSlowAreaResponse
            return createSlowAreaResponse.list
        }
        return null
    }

    /**
     * @describe 结束创建限速区
     */
    fun endCreateLimitSpeed(lineName:String): Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 5
        clientPara["name"] = lineName
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 保存限速区
     */
    fun saveLimitSpeed(laserMapName: String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 7
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 不保存限速区(返回上一级)
     */
    fun notSaveLimitSpeed():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 7
        clientPara["catalog"] = ""
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 删除单个限速区
     */
    fun deleteLimitSpeed(laserMapName:String, lineName:String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 8
        clientPara["catalog"] = laserMapName
        clientPara["name"] = lineName
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 获取LineInfoList
     */
    fun getLimitSpeedLineInfoList(laserMapName: String, nameList:List<String>): List<LineInfoModel>{
        val lineInfoList = ArrayList<LineInfoModel>()
        val clientPara = HashMap<String, Any>()
        clientPara["catalog"] = laserMapName
        clientPara["command"] = 1
        for (lineName in nameList) {
            clientPara["name"] = lineName
            val response = ClientManager.sendClientMsg(Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara))
            if (response.isFlag) {
                val createSlowAreaResponse = response.response as CreateSlowAreaResponse
                val tempList = ArrayList<PointCompat>()
                for (pose in createSlowAreaResponse.poses) {
                    val pointCompat = PointCompat().apply {
                        x = pose.x
                        y = pose.y
                        theta = pose.theta
                    }
                    tempList.add(pointCompat)
                }
                val lineInfoModel = LineInfoModel(
                    pose = tempList,
                    type = createSlowAreaResponse.type,
                    name = lineName,
                    radius = createSlowAreaResponse.range,
                    speed = createSlowAreaResponse.speed,
                    visibleRange = createSlowAreaResponse.distance,
                    state = createSlowAreaResponse.state
                )
                lineInfoList.add(lineInfoModel)
            }
        }
        return lineInfoList
    }

    /**
     * @describe 编辑限速区
     */
    fun editLineInfoParams(laserMapName:String, lineInfoModel: LineInfoModel):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 9
        clientPara["catalog"] = laserMapName
        clientPara["name"] = lineInfoModel.name
//        clientPara["type"] = lineInfoModel.type?:0
//        clientPara["range"] = lineInfoModel.radius?:0f
//        clientPara["speed"] = lineInfoModel.speed?:0f
//        clientPara["distance"] = lineInfoModel.visibleRange?:0f
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 删除全部限速区
     */
    fun deleteAllLimitSpeed(laserMapName: String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 8
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_SPEED_LIMIT_AREA, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 获取斜坡距离
     */
    fun getSlopeDistance(): Double?{
        val clientPara = HashMap<String, Any>()
        clientPara["range"] = 0.15f
        val getDistance = Client(ClientConstant.GET_DISTANCE, clientPara)
        val response = ClientManager.sendClientMsg(getDistance)
        if (response.isFlag) {
            val obstacleMessageResponse = response.response as ObstacleMessageResponse
            val b = BigDecimal(obstacleMessageResponse.result)
            return b.setScale(3, BigDecimal.ROUND_HALF_UP).toDouble()
        }
        return null
    }

    /**
     * @describe 获取虚拟墙名字列表
     */
    fun getVirtualWallLineNameList(laserMapName: String): List<String>?{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 2
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if(response.isFlag){
            val createVirtualWallResponse = response.response as CreateVirtualWallResponse
            return createVirtualWallResponse.list
        }
        return null
    }

    /**
     * @describe 获取虚拟墙列表
     */
    fun getVirtualWallLineList(laserMapName: String, nameList:List<String>): List<LineInfoModel>{
        val lineInfoList = ArrayList<LineInfoModel>()
        val clientPara = HashMap<String, Any>()
        clientPara["catalog"] = laserMapName
        clientPara["command"] = 1
        for (lineName in nameList) {
            clientPara["name"] = lineName
            val response = ClientManager.sendClientMsg(Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara))
            if (response.isFlag) {
                val createVirtualWallResponse = response.response as CreateVirtualWallResponse
                val tempList = ArrayList<PointCompat>()
                for (pose in createVirtualWallResponse.poses) {
                    val pointCompat = PointCompat().apply {
                        x = pose.x
                        y = pose.y
                        theta = pose.theta
                    }
                    tempList.add(pointCompat)
                }
                val lineInfoModel = LineInfoModel(
                    pose = tempList,
                    name = lineName,
                    state = createVirtualWallResponse.state
                )
                lineInfoList.add(lineInfoModel)
            }
        }
        return lineInfoList
    }

    /**
     * @describe 创建虚拟墙
     */
    fun startCreateVirtualWall():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 3
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if (response.isFlag) {
            SubManager.sub(ClientConstant.TEMP_OBSTACLE)
        }
        return response.isFlag
    }

    /**
     * @describe 结束创建虚拟墙
     */
    fun endCreateVirtualWall(lineName:String): Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 4
        clientPara["name"] = lineName
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        if (response.isFlag){
            SubManager.unsub(ClientConstant.TEMP_OBSTACLE)
        }
        return response.isFlag
    }

    /**
     * @describe 重置虚拟墙
     */
    fun resetVirtualWall():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 4
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 保存虚拟墙
     */
    fun saveVirtualWall(laserMapName: String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 5
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 不保存虚拟墙(返回上一级)
     */
    fun notSaveVirtualWall():Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 5
        clientPara["catalog"] = ""
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 删除单个虚拟墙
     */
    fun deleteVirtualWall(laserMapName:String, lineName:String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 6
        clientPara["catalog"] = laserMapName
        clientPara["name"] = lineName
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 删除全部限速区
     */
    fun deleteAllVirtualWall(laserMapName: String):Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara["command"] = 6
        clientPara["catalog"] = laserMapName
        val startCreate = Client(ClientConstant.CREATE_VIRTUAL_WALL, clientPara)
        val response = ClientManager.sendClientMsg(startCreate)
        return response.isFlag
    }

    /**
     * @describe 查看是否存在参数
     */
    fun hasParam(name: String): Boolean{
        val para = java.util.HashMap<String, Any>()
        para["name"] = name
        val hasParamService =
            Client(ClientConstant.HAS_PARAM, para)
        val rosResult = ClientManager.sendClientMsg(hasParamService)
        if (!rosResult.isFlag) return false
        val clientResponse = rosResult.response as HasParamResponse
        return clientResponse.exists
    }

    /**
     * @describe 恢复出厂 cmd 1：重置所有/2：重置地图文件/3：重置日志文件/4：重置BAG文件
     */
    fun resetFactoryData(reset: Int): ChassisResetResponse? {
        val para = HashMap<String, Any>()
        para["reset"] = reset
        val chassisResetService = Client(ClientConstant.CHASSIS_RESET, para)
        val rosResult = ClientManager.sendClientMsg(chassisResetService)
        if (!rosResult.isFlag) return null
        return rosResult.response as ChassisResetResponse
    }

    /**
     * @describe 获取版本 type 1：底盘版本/2：stm32版本
     */
    fun getVersion(type: Int): VersionGetResponse? {
        val para = java.util.HashMap<String, Any>()
        para[Constant.TYPE] = type
        val versionGetService =
            Client(ClientConstant.VERSION_GET, para)
        val rosResult = ClientManager.sendClientMsg(versionGetService)
        if (!rosResult.isFlag) return null
        return rosResult.response as VersionGetResponse
    }

    /**
     * @describe 发送lora控梯消息
     */
    fun sendLoraLiftMessage(loraModel: LiftControlLoraModel): Int{
        val para = java.util.HashMap<String, Any>()
        para["para"] = "${loraModel}\n"
        val loraSendService =
            Client(ClientConstant.LORA_SEND, para)
        val rosResult = ClientManager.sendClientMsg(loraSendService)
        if (!rosResult.isFlag) return PeripheralsCtrlResponse.RESULT_ERROR
        val peripheralsCtrlResponse: PeripheralsCtrlResponse
        return try {
            peripheralsCtrlResponse = rosResult.response as PeripheralsCtrlResponse
            peripheralsCtrlResponse.result
        }catch (e: Exception){
            PeripheralsCtrlResponse.RESULT_ERROR
        }
    }

    /**
     * @description 设置底盘时间
     */
    fun updateTime(): Boolean{

        val date = Date()
        val para = HashMap<String, Any>()
        para["date"] = sdf1.format(date)
        para["time"] = sdf2.format(date)


        val timeUpdateService =
            Client(ClientConstant.TIME_UPDATE, para)
        val rosResult = ClientManager.sendClientMsg(timeUpdateService)
        return rosResult.isFlag
    }
    /**
     * @description 设置底盘时间（时间戳）
     */
    fun updateCurrent(time : Long ): Boolean{
        // 创建一个Date对象，将时间戳作为参数传递给构造函数
        val  dateTime  =  Date(time)
        val para = HashMap<String, Any>()
        para["date"] = sdf1.format(dateTime)
        para["time"] = sdf2.format(dateTime)
        Log.d("TAG", "设置日期: ${sdf1.format(dateTime)}")
        Log.d("TAG", "设置时间: ${sdf2.format(dateTime)}")
        val timeUpdateService =
            Client(ClientConstant.TIME_UPDATE, para)
        val rosResult = ClientManager.sendClientMsg(timeUpdateService)
        return rosResult.isFlag
    }
    /**
     * @describe 预加载地图
     */

    fun preLoadMap(sourceName: String): Boolean{
        val para = java.util.HashMap<String, Any>()
        para["source_name"] = sourceName
        val preLoadMapService =
            Client(ClientConstant.PRE_LOAD_MAP, para)
        val rosResult = ClientManager.sendClientMsg(preLoadMapService)
        return  rosResult.isFlag
    }

    /**
     * @description 是否电梯外
     */
    fun checkOutOfLift(location: QueryPointEntity): Boolean {
        val clientPara = HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location.x
        position[Constant.Y] = location.y
        position[Constant.Z] = location.w
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location.w!!)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.TARGET_POSE] = targetPose
        clientPara[Constant.DOCK_DIRECTION] = 0
        val clientMoveTo = Client(ClientConstant.CHECK_OUT_OF_LIFT, clientPara)
        val rosResultMoveTo = ClientManager.sendClientMsg(clientMoveTo)
        if (rosResultMoveTo.isFlag) {
            val response = rosResultMoveTo.response as MoveToResponse
            val result = when (response.result) {
                1 -> {
                    true
                }
                0 -> {
                    false
                }
                else -> {
                    false
                }
            }
            return result
        }else{
            return false
        }
    }

    /**
     * @description 判断导航前需不要弹出提示框的服务
     * 1- 设置成功，可以直接导航
     * 非1- 表示客户需要把机器人推到充电桩接触充电片，需要弹出提示
     *
     * target_pose.position x,y,z表示充电桩的x,y,w
     * target_pose.orientation x,y,w表示上一次接触充电片时里程计的x,y,w
     */
    fun judgeBeforeNavigate(labelMapName: String, odomPose:Pose2D?, chargePoint:QueryPointEntity?): Int{
        val clientParam = java.util.HashMap<String, Any>()
        clientParam["label_map_name"] = labelMapName
        clientParam["cruise_map_name"] = ""
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = chargePoint?.x?:0
        position[Constant.Y] = chargePoint?.y?:0
        position[Constant.Z] = chargePoint?.w?:0
        val orientation = JSONObject()
        orientation[Constant.X] = odomPose?.x?:1000
        orientation[Constant.Y] = odomPose?.y?:1000
        orientation[Constant.W] = odomPose?.theta?:0
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        clientParam[Constant.TARGET_POSE] = targetPose
        val clientCalculateChargePose = Client(ClientConstant.CALCULATE_CHARGE_POSE, clientParam)
        val rosResultCalculateChargePose = ClientManager.sendClientMsg(clientCalculateChargePose)
        if(rosResultCalculateChargePose.isFlag){
            val response = rosResultCalculateChargePose.response as CruiseMoveToResponse
            return response.result
        }
        return -1
    }

    /**
     * @description 获取机器人里程计的位置
     */
    fun getOdomPose(): Pose2D?{
        val clientPara = java.util.HashMap<String, Any>()
        val clientGetOdomPose = Client(ClientConstant.GET_ODOM_POSE, clientPara)
        val rosResultGetOdomPose = ClientManager.sendClientMsg(clientGetOdomPose)
        if (rosResultGetOdomPose.isFlag) {
            val response = rosResultGetOdomPose.response as TargetPoseResponse
            return response.pose
        }
        return null
    }

    /**
     * @description 在充电桩检测到充电信号后的机器人位置设置
     */
    fun setCalculateChargePose(location: QueryPointEntity?): Int {
        val clientPara = java.util.HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location?.x?:0
        position[Constant.Y] = location?.y?:0
        position[Constant.Z] = location?.w?:0
        val orientation = JSONObject()
        val quaternion = Quaternion.fromAxisAngle(Vector3.zAxis(), location?.w?:0.0)
        orientation[Constant.X] = quaternion.x
        orientation[Constant.Y] = quaternion.y
        orientation[Constant.Z] = quaternion.z
        orientation[Constant.W] = quaternion.w
        targetPose[Constant.POSITION] = position
        targetPose[Constant.ORIENTATION] = orientation
        // clientPara
        clientPara[Constant.POSE] = targetPose
        val clientSetPose = Client(ClientConstant.SET_CALCULATE_CHARGE_POSE, clientPara)
        val rosResultSetPose = ClientManager.sendClientMsg(clientSetPose)
        var score = 0
        if (rosResultSetPose.isFlag) {
            val response = rosResultSetPose.response as Set_poseResponse
            score = response.result
        }
        return score
    }

    /**
     * @description 检查是否在充电桩附近，判断是否需要后退
     */
    fun checkNearCharge(location: QueryPointEntity?): Int{
        val clientPara = java.util.HashMap<String, Any>()
        // target_pose
        val targetPose = JSONObject()
        val position = JSONObject()
        position[Constant.X] = location?.x?:0
        position[Constant.Y] = location?.y?:0
        position[Constant.Z] = location?.w?:0
        targetPose[Constant.POSITION] = position
        // clientPara
        clientPara[Constant.POSE] = targetPose
        val clientSetPose = Client(ClientConstant.CHECK_POINT, clientPara)
        val rosResultSetPose = ClientManager.sendClientMsg(clientSetPose)
        if (rosResultSetPose.isFlag) {
            val response = rosResultSetPose.response as Set_poseResponse
            return response.result
        }
        return -1
    }

    /**
     * @description 高亮显示子图
     */
    fun highLightSubMap(id: String): Boolean{
        val clientPara = HashMap<String, Any>()
        clientPara[Constant.VALUE] = id.toInt()
        val highLightSubMap = Client(ClientConstant.HIGH_LIGHT_SUB_MAP, clientPara)
        val response = ClientManager.sendClientMsg(highLightSubMap)
        if(response.isFlag){
            val result = RosPointArrUtil.result
            if (result != RosResultEnum.LASER_SUCCESS_RESULT.code) {
                val msg: String = when (result) {
                    -1 -> "未知错误"
                    -2 -> "状态失败"
                    else -> "未定义错误"
                }
                LogUtil.e("高亮显示子图:$msg")
                ToastUtil.show("高亮显示子图:$msg")
            }
            return result == RosResultEnum.LASER_SUCCESS_RESULT.code
        }
        return false
    }

    /**
     * @description 新增标记电梯点服务（base chassisV3.5.1）
     * @param range (double):机器人离电梯内点的距离
     * pose1 (Pose2D):内点坐标
     * pose2 (Pose2D):外点坐标
     * state (int):错误码 -10：传感器异常 -1：失败 1：成功
     */
    fun getLiftPoint(range: Double): CreateOneWayResponse?{
        val clientPara = HashMap<String, Any>()
        clientPara["range"] = range
        val getLiftPose = Client(ClientConstant.GET_LIFT_POINT, clientPara)
        val  rosResult = ClientManager.sendClientMsg(getLiftPose, CreateOneWayResponse::class.java)
        var createOneWayResponse: CreateOneWayResponse? = null
        if (rosResult.isFlag) {
            createOneWayResponse = rosResult.response
        }
        return createOneWayResponse
    }
    fun setDispatchFloorId(id: Int) {
        val clientParamSpeed = HashMap<String, Any>()
        clientParamSpeed["name"] = "/navigation_base/sch_floor_id"
        clientParamSpeed["value"] = "$id"
        val speedClient = Client(ClientConstant.SET_PARAM, clientParamSpeed)
        val rosResultSpeed = ClientManager.sendClientMsg(speedClient)
        if (rosResultSpeed.isFlag) {
            LogUtil.i("设置FloorID成功::id::$id")
        }
    }
}
