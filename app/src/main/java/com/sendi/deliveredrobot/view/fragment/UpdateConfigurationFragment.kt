package com.sendi.deliveredrobot.view.fragment;

import android.annotation.SuppressLint
import android.app.Notification
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.databinding.FragmentUpdateconfigBinding
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.inputfilter.DownloadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import java.io.File
import java.util.*


class UpdateConfigurationFragment : Fragment() {

    private lateinit var binding: FragmentUpdateconfigBinding
    private lateinit var mView: View
    var FileName: Array<String>? = null//副屏内容
    var sleepName: Array<String>? = null//熄屏内容
    var passc: Int = 0
    var pics: String? = ""//图片名字
    var videoFile: String? = ""//视频名字
    var sleepContentName: String? = ""//待机图片名字
    private var controller: NavController? = null
    var timeStampReplyGateConfig: Long? = null
    var timeStampRobotConfigSql: Long? = null
    var mHandler: Handler? = null

    lateinit var debugDao: DebugDao
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_updateconfig, container, false)
        binding = DataBindingUtil.bind(mView)!!
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        time()
        //收到新配置
        obtain()
        //这里必须过几秒钟后才能赋值，否者可能会将原来数据库中的值赋给变量
        val assignmentThread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(5000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            assignment()
        }, "assignment")
        assignmentThread.start()

        val thread = Thread({
            val threadName = Thread.currentThread().name
            println(threadName + "线程开始执行")
            try {
                Thread.sleep(15000)
                //15000
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            println(threadName + "任务执行完毕")
            if (pics == "" || videoFile == "" || sleepContentName == "") {
                controller!!.navigate(R.id.action_updateConfigurationFragment_to_homeFragment)
            }
            if (pics != "" || videoFile != "" || sleepContentName != "") {
                if (pics != "") {
                    Log.d(TAG, "名字： " + pics)
                    FileName = pics?.split(",")?.toTypedArray()
                } else if (videoFile != "") {
                    FileName = videoFile?.split(",")?.toTypedArray()
                } else if (sleepContentName != "") {
                    sleepName = sleepContentName?.split(",")?.toTypedArray()
                }
                //副屏
                if (FileName != null) {
                    for (i in FileName!!) {
                        Looper.prepare()
                        DownloadUtil.getInstance().download(
                            "http://172.168.201.34:9055/management_res/" + i,
                            Universal.Secondary,
                            object : DownloadUtil.OnDownloadListener {
                                override fun onDownloadSuccess(path: String) {
                                    Log.e("TAG", "已保存：$path ")
                                }

                                @SuppressLint("LongLogTag")
                                override fun onDownloading(progress: Int) {
                                    if (progress == 100) {
                                        passc++
                                        Log.d(TAG + "print passc", passc.toString())
                                        if (sleepName == null) {
                                            if (passc == FileName!!.size) {
                                                mHandler = @SuppressLint("HandlerLeak")
                                                object : Handler() {
                                                    override fun handleMessage(msg: Message) {
                                                        // process incoming messages here
                                                        controller!!.navigate(R.id.action_updateConfigurationFragment_to_homeFragment)
                                                    }
                                                }
                                            }
                                        } else {
                                            if (passc == FileName!!.size + sleepName!!.size) {
                                                mHandler = @SuppressLint("HandlerLeak")
                                                object : Handler() {
                                                    override fun handleMessage(msg: Message) {
                                                        // process incoming messages here
                                                        controller!!.navigate(R.id.action_updateConfigurationFragment_to_homeFragment)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onDownloadFailed() {
                                    Log.d(BaseFragment.TAG, "下载失败: ")

                                }
                            })
                        Looper.loop()
                    }
                }

                //熄屏
                if (sleepName != null) {
                    for (i in sleepName!!) {
                        Looper.prepare()
                        DownloadUtil.getInstance().download(
                            "http://172.168.201.34:9055/management_res" + i,
                            Universal.Standby,
                            object : DownloadUtil.OnDownloadListener {
                                override fun onDownloadSuccess(path: String) {
                                    Log.e("TAG", "已保存：$path ")
                                }

                                @SuppressLint("LongLogTag")
                                override fun onDownloading(progress: Int) {
                                    if (progress == 100) {
                                        passc++
                                        Log.d(TAG + "print passc", passc.toString())
                                        if (FileName == null) {
                                            if (passc == sleepName!!.size) {
                                                mHandler = @SuppressLint("HandlerLeak")
                                                object : Handler() {
                                                    override fun handleMessage(msg: Message) {
                                                        // process incoming messages here
                                                        controller!!.navigate(R.id.action_updateConfigurationFragment_to_homeFragment)
                                                    }
                                                }
                                            }
                                        } else {
                                            if (passc == sleepName!!.size + FileName!!.size) {
                                                mHandler = @SuppressLint("HandlerLeak")
                                                object : Handler() {
                                                    override fun handleMessage(msg: Message) {
                                                        // process incoming messages here
                                                        controller!!.navigate(R.id.action_updateConfigurationFragment_to_homeFragment)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onDownloadFailed() {
                                    Log.d(TAG, "下载失败: ")

                                }
                            })
                        Looper.prepare()
                    }
                }

            }
        }, "work")

        thread.start()

        binding.bootIv.apply {
            Glide.with(this).asGif().load(R.raw.selfcheck_animation).into(this)
        }
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i("SelfCheckFragment onStart")
    }

    override fun onStop() {
        super.onStop()
        RobotStatus.selfChecking = 1
        LogUtil.i("SelfCheckFragment onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i("SelfCheckFragment onDestroy")
    }

    /**
     *创建文件
     */
    private fun createFolder() {
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
    private fun deleteFiles(file: File): Boolean {
        return try {
            if (file.isDirectory) { //判断是否是文件夹
                val files = file.listFiles() //遍历文件夹里面的所有的
                for (i in files.indices) {
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


    private fun time() {
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
                debugDao = DataBaseDeliveredRobotMap.getDatabase(
                    Objects.requireNonNull(
                        MyApplication.instance
                    )!!
                ).getDebug()
//                listData = debugDao.queryTargetPointMap()!!
                val mMapResult = mapTargetPointServiceImpl.mapsName
//                val listData = mMapResult.data["mapsName"] as List<*>
                val jsonObject = JSONObject()//实例话JsonObject()
                jsonObject.put("type", "queryConfigTime")
                jsonObject.put("robotTimeStamp", timeStampRobotConfigSql)
                jsonObject.put("gateTimeStamp", timeStampReplyGateConfig)
                jsonObject.put("maps", mMapResult.data["mapsName"] as List<*> )
                //发送Mqtt
                CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)
            }
        }
    }

    private fun obtain() {
        //机器人基础配置
        RobotStatus.robotConfig.observe(viewLifecycleOwner) {
            LitePal.deleteAll(RobotConfigSql::class.java)
            //提交数据到数据库
            deleteFiles(File(Universal.Standby))
            //创建文件的方法
            createFolder()
            val robotConfigSql = RobotConfigSql()
            robotConfigSql.audioType = it.audioType!!
            robotConfigSql.wakeUpWord = it.wakeUpWord
            robotConfigSql.sleep = it.sleep!!
            robotConfigSql.sleepTime = it.sleepTime!!
            robotConfigSql.wakeUpType = it.wakeUpType!!
            robotConfigSql.sleepType = it.sleepType!!
            robotConfigSql.picType = it.picType!!
            robotConfigSql.mapName = it.mapName
            robotConfigSql.timeStamp = it.timeStamp!!
            robotConfigSql.save()
            time()
        }
        //门岗配置
        RobotStatus.gatekeeper.observe(viewLifecycleOwner) {
            LitePal.deleteAll(ReplyGateConfig::class.java)
            //提交到数据库
            deleteFiles(File(Universal.Secondary))
            //创建文件的方法
            createFolder()
            val replyGateConfig = ReplyGateConfig()
            replyGateConfig.temperatureThreshold = it.temperatureThreshold!!
            replyGateConfig.picPlayType = it.picPlayType!!
            replyGateConfig.picPlayTime = it.picPlayTime!!
            replyGateConfig.videoAudio = it.videoAudio!!
            replyGateConfig.fontContent = it.fontContent
            replyGateConfig.fontColor = it.fontColor
            replyGateConfig.fontSize = it.fontSize!!
            replyGateConfig.fontBackGround = it.fontBackGround
            replyGateConfig.tipsTemperatureInfo = it.tipsTemperatureInfo
            replyGateConfig.tipsTemperatureWarn = it.tipsTemperatureWarn
            replyGateConfig.tipsMaskWarn = it.tipsMaskWarn
            replyGateConfig.timeStamp = it.timeStamp!!
            replyGateConfig.picType = it.picType!!
            replyGateConfig.fontLayout = it.fontLayout!!
            replyGateConfig.bigScreenType = it.bigScreenType!!
            replyGateConfig.textPosition = it.textPosition!!
            pics = it.pics
            videoFile = it.videos
            replyGateConfig.save()
            time()
        }

    }

    private fun assignment() {
        val robotConfigData: List<RobotConfigSql> = LitePal.findAll(RobotConfigSql::class.java)
        for (robotConfigDatas in robotConfigData) {
            Universal.audioType = robotConfigDatas.audioType
            Universal.wakeUpWord = robotConfigDatas.wakeUpWord
            Universal.sleep = robotConfigDatas.sleep
            Universal.sleepTime = robotConfigDatas.sleepTime
            Universal.wakeUpType = robotConfigDatas.wakeUpType
            Universal.sleepType = robotConfigDatas.sleepType
            Universal.picType = robotConfigDatas.picType
            Universal.timeStampRobotConfigSql = robotConfigDatas.timeStamp
            Universal.mapName = robotConfigDatas.mapName
        }

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
        }
    }

}

