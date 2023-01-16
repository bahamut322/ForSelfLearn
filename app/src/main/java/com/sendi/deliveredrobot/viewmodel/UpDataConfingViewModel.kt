package com.sendi.deliveredrobot.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.BaseFragment
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MapConfig
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.view.inputfilter.DownloadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import java.io.File
import java.util.*

class UpDataConfingViewModel : ViewModel() {
    lateinit var debugDao: DebugDao
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    lateinit var dao: DeliveredRobotDao
    var fileName: Array<String>? = null//副屏内容
    var sleepName: Array<String>? = null//熄屏内容
    var fileNamepassc: Int = 0
    var sleepNamepassc: Int = 0
    var mHandler: Handler? = null
    var pics: String? = ""//图片名字
    var videoFile: String? = ""//视频名字
    var sleepContentName: String? = ""//待机图片名字
    private var pointIdList: ArrayList<Int>? = ArrayList()
    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null

    /**
     *创建文件
     */
    fun createFolder() {
        //文件夹目录(存放待机图片/视频...)
        val fileStandby = File(Universal.Standby)
        //文件目录(存放正常的副屏轮播)
        val fileSecondary = File(Universal.Secondary)
        //判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!fileStandby.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            fileStandby.mkdirs()
        } else if (!fileSecondary.exists()) {
            fileSecondary.mkdirs()
        }
    }

    /**
     * 删除文件
     */
    fun deleteFiles(file:File): Boolean {
        return try {
            if (file.isDirectory) { //判断是否是文件夹
                val files = file.listFiles() //遍历文件夹里面的所有的
                for (i in files!!.indices) {
                    Log.e("SelfCheckFragment", "更新原有文件>>>>>> " + files[i].toString())
                    deleteFiles(files[i]) //删除
                }
            } else {
                file.delete()
            }
            System.gc() //系统回收垃圾
            true
        } catch (e: Exception) {
            Log.e("SelfCheckFragment", "更新报错！！！: $e")
            false
        }
    }


    fun time() {
        debugDao = DataBaseDeliveredRobotMap.getDatabase(
            Objects.requireNonNull(
                MyApplication.instance
            )!!
        ).getDebug()
        dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        val replyGateConfigData: List<ReplyGateConfig> =
            LitePal.findAll(ReplyGateConfig::class.java)
        for (replyGateConfigDatas in replyGateConfigData) {
            timeStampReplyGateConfig = replyGateConfigDatas.timeStamp
        }
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            timeStampRobotConfigSql = robotConfigDatas.timeStamp
        }
        val timeGetTime = Date().time //时间戳
        if (timeStampRobotConfigSql == null) {
            timeStampRobotConfigSql = timeGetTime
        }
        if (timeStampReplyGateConfig == null) {
            timeStampReplyGateConfig = timeGetTime
        }
        //查询地图名字
        MainScope().launch {
            withContext(Dispatchers.Default) {
//                listData = debugDao.queryTargetPointMap()!!
                val mMapResult = mapTargetPointServiceImpl.mapsName
                val listData = mMapResult.data["mapsName"] as List<*>
                val mapList: ArrayList<String> = ArrayList()
                for (i in listData.indices) {
                    mapList.add(listData[i].toString())
                }
                val jsonObject = JSONObject()//实例话JsonObject()
                jsonObject["type"] = "queryConfigTime"
                jsonObject["robotTimeStamp"] = timeStampRobotConfigSql
                jsonObject["gateTimeStamp"] = timeStampReplyGateConfig
                jsonObject["maps"] = mapList
                //发送Mqtt
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)
            }
        }
    }

    fun assignment() {
        //机器人基础配置
        debugDao = DataBaseDeliveredRobotMap.getDatabase(
            Objects.requireNonNull(
                MyApplication.instance
            )!!
        ).getDebug()
        dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            Universal.audioType = robotConfigDatas.audioType
            Universal.wakeUpWord = robotConfigDatas.wakeUpWord
            Universal.sleep = robotConfigDatas.sleep
            Universal.sleepTime = robotConfigDatas.sleepTime
            Universal.wakeUpType = robotConfigDatas.wakeUpList
            Universal.sleepType = robotConfigDatas.sleepType
            Universal.picType = robotConfigDatas.picType
            Universal.timeStampRobotConfigSql = robotConfigDatas.timeStamp
            Universal.mapName = robotConfigDatas.mapName
            Universal.password = robotConfigDatas.password
            if (robotConfigDatas.mapName != "") {
                val mapId = debugDao.selectMapId(robotConfigDatas.mapName)
                Log.d(ContentValues.TAG, "地图ID： $mapId")
                //设置总图
                dao.updateMapConfig(MapConfig(1, mapId, null))
                //查询充电桩
                val pointId = dao.queryChargePointList()
                pointId?.map {
                    pointIdList?.add(it.pointId!!)
                }
                //设置充电桩；默认查询到的第一个数据
                dao.updateMapConfig(MapConfig(1, mapId, pointIdList?.get(0)))
            }
            //设置默认密码
            if (robotConfigDatas.password == "") {
                Universal.password = "8888"
            }
        }
        //门岗配置
        val replyGateConfigData: List<ReplyGateConfig> =
            LitePal.findAll(ReplyGateConfig::class.java)
        for (replyGateConfigDatas in replyGateConfigData) {
            Universal.picTypeNum = replyGateConfigDatas.picType
            Universal.picPlayTime = replyGateConfigDatas.picPlayTime
            Universal.videoAudio = replyGateConfigDatas.videoAudio
            Universal.fontContent = replyGateConfigDatas.fontContent
            Universal.fontColor = replyGateConfigDatas.fontColor
            Universal.fontSize = replyGateConfigDatas.fontSize
            Universal.fontLayout = replyGateConfigDatas.fontLayout
            Universal.fontBackGround = replyGateConfigDatas.fontBackGround
            Universal.tipsTemperatureInfo = replyGateConfigDatas.tipsTemperatureInfo
            Universal.tipsTemperatureWarn = replyGateConfigDatas.tipsTemperatureWarn
            Universal.tipsMaskWarn = replyGateConfigDatas.tipsMaskWarn
            Universal.timeStampReplyGateConfig = replyGateConfigDatas.timeStamp
            Universal.bigScreenType = replyGateConfigDatas.bigScreenType
            Universal.textPosition = replyGateConfigDatas.textPosition
            Universal.TemperatureMax = replyGateConfigDatas.temperatureThreshold
        }
    }

    //查询文件目录中文件数量
    fun fileSize(fileName: String): Int {
        val file: File = File(fileName)
        val files = file.listFiles()
        return files.size
    }


}

