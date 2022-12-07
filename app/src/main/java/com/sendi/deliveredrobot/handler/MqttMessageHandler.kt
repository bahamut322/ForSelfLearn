package com.sendi.deliveredrobot.handler

import androidx.lifecycle.ViewModelLazy
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sendi.deliveredrobot.ElevatorObject
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.RemoteOrderHelper
import com.sendi.deliveredrobot.model.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.concurrent.thread

/**
 *   @author: heky
 *   @date: 2021/8/18 12:01
 *   @describe: MQTT消息处理
 */
object MqttMessageHandler {
    private val dao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private val mainScope = MainScope()
    private val basicSettingViewModel = ViewModelLazy(
        BasicSettingViewModel::class,
        { MainActivity.instance.viewModelStore },
        { MainActivity.instance.defaultViewModelProviderFactory }
    )
    private val gson = Gson()
    private val floorNameSet = HashSet<String>()

    /**
     * @describe 接收消息
     */
    fun receive(mqttMessage: MqttMessage) {
        synchronized(MqttMessageHandler::class.java) {
            val message = String(mqttMessage.payload)
            val jsonObject = JsonParser.parseString(message) as JsonObject
            if (!jsonObject.has("type")) return
            when (jsonObject.get("type").asString) {
                "callElevatorState" -> {
                    try {
                        when (jsonObject.get("state").asInt) {
                            0 -> {
                                RobotStatus.liftState = false
                            }
                            1 -> {
                                RobotStatus.liftState = true
                            }
                            else -> {

                            }
                        }
                    } catch (_: Exception) {

                    }
                }
//                "callElevatorCurrentFloor" -> {
//                    try {
//                        val currentFloorIndex = jsonObject.get("currentFloorIndex").asInt
//                        LiftHelper.liftReach(currentFloorIndex)
//                    }catch (_: java.lang.Exception){
//
//                    }
//                }
                "sendVersionInfo" -> {
                    //版本更新信息
                    val flag: Boolean
                    val size: Int
                    val path: String
                    val version: String
                    try {
                        flag = jsonObject.get("flag").asBoolean
                        size = jsonObject.get("size").asInt
                        path = jsonObject.get("path").asString
                        version = jsonObject.get("version").asString
                    } finally {
                        mainScope.launch {
                            withContext(Dispatchers.Main) {
                                RobotStatus.versionStatusModel.value = VersionStatusModel(
                                    flag = flag,
                                    size = size,
                                    path = path,
                                    version = version
                                )
                            }
                        }
                    }
                }
                "sendRobotTenancy" -> {
                    val useType: Int
                    val days: Int
                    val deadline: String
                    val robotName: String
                    try {
                        // 下发机器人租期信息
                        useType = jsonObject.get("useType").asInt
                        days = jsonObject.get("days").asInt
                        deadline = when (useType) {
                            0 -> MyApplication.instance!!.resources.getString(R.string.forever)
                            else -> jsonObject.get("deadline").asString
                        }
                        robotName = jsonObject.get("robotName").asString
                    } finally {
                        mainScope.launch {
                            withContext(Dispatchers.Main) {
                                RobotStatus.tenancy.value =
                                    ResponseTenancyModel(useType, days, deadline, robotName)
                            }
                        }
                    }
                }
                "sendAppletTask" -> {
                    // 小程序下发任务
                    ToastUtil.show("收到远程任务..")
                    LogUtil.i("收到远程任务..")
                    RemoteOrderHelper.receiveRemoteOrder(jsonObject)
                }
                "resetVerificationCode" -> {
                    // 重置密码
                    ToastUtil.show("远程重置密码")
                    LogUtil.i("远程重置密码")
                    basicSettingViewModel.value.basicConfig.verifyPassword = "00000"
                    thread {
                        dao.updateBasicConfig(basicSettingViewModel.value.basicConfig)
                    }
                    CloudMqttService.publish(ResetVerificationCodeAckModel().toString())
                }
                "replyFloorList" -> {
                    val replyFloorListModel =
                        gson.fromJson(message, ReplyFloorListModel::class.java)
                    // 楼层名字set，用于展示
                    floorNameSet.clear()
                    replyFloorListModel.floorList?.map { floorModel -> floorNameSet.add(floorModel.floor) }
                    ElevatorObject.originFloorModel = replyFloorListModel
                    ElevatorObject.floorNameArray = floorNameSet.toTypedArray()
                    ElevatorObject.elevatorNameArray = replyFloorListModel.elevatorList
                }
                "replyElevatorList" -> {
                    val replyElevatorListModel =
                        gson.fromJson(message, ReplyElevatorListModel::class.java)
                    handleReplyElevatorListModel(replyElevatorListModel)
                    ElevatorObject.originElevatorModel = replyElevatorListModel
                }
                "replyElevatorArrive" -> {
                    try {
                        val currentFloorName = jsonObject.get("currentFloorName").asString
                        val elevator = jsonObject.get("elevator").asString
                        LiftHelper.liftReach(currentFloorName, elevator)
                    } catch (_: java.lang.Exception) {

                    }
                }
                else -> {}
            }
        }
    }

    private fun handleReplyElevatorListModel(replyElevatorListModel: ReplyElevatorListModel){
        for (elevatorModel in replyElevatorListModel.elevatorList) {
            val floorList = elevatorModel.floorList.filter { floorModel -> "开门" != floorModel.name && "关门" != floorModel.name}
            val floorNameSet = HashSet<String>()
            val transferSet = HashSet<String>()
            for (floorModel in floorList) {
                floorNameSet.add(floorModel.name)
                if (floorModel.transfer == 1) {
                    transferSet.add(floorModel.name)
                }
            }
            elevatorModel.apply {
                floorNameList = floorNameSet.toTypedArray()
                transferList = transferSet.toTypedArray()
            }
        }
    }
}