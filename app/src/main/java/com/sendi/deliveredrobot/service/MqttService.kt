package com.sendi.deliveredrobot.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.handler.MqttMessageHandler
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.utils.LogUtil
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*


/**
 * @describe    与云平台交互的MQTT
 * Github       https://github.com/wildma
 */
class MqttService : Service() {
    private var mMqttConnectOptions: MqttConnectOptions? = null
    private var timeStampReplyGateConfig: Long? = null
    private var timeStampRobotConfigSql: Long? = null
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        init()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    fun response(message: String) {
        val topic = RESPONSE_TOPIC
        val qos = 2
        val retained = false
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient?.publish(
                topic, message.toByteArray(),
                qos, retained
            )
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        val serverURI = BuildConfig.MQTT_HOST //服务器地址（协议+地址+端口号）
        mqttAndroidClient = MqttAndroidClient(this, serverURI, RobotStatus.SERIAL_NUMBER)
        mqttAndroidClient?.setCallback(mqttCallback) //设置监听订阅消息的回调
        mMqttConnectOptions = MqttConnectOptions()
        mMqttConnectOptions?.isCleanSession = true //设置是否清除缓存
        mMqttConnectOptions?.connectionTimeout = 10 //设置超时时间，单位：秒
        mMqttConnectOptions?.keepAliveInterval = 20 //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions?.userName = USERNAME //设置用户名
        mMqttConnectOptions?.password = PASSWORD.toCharArray() //设置密码

        // last will message
        var doConnect = true
        val message = "{\"terminal_uid\":\"$CLIENTSIDE\"}"
        val topic = PUBLISH_TOPIC
        val qos = 2
        val retained = false
        // 最后的遗嘱
        try {
            mMqttConnectOptions?.setWill(
                topic,
                message.toByteArray(),
                qos,
                retained
            )
        } catch (e: Exception) {
            LogUtil.i("MQTT:Exception Occurred$e")
            doConnect = false
            iMqttActionListener.onFailure(null, e)
        }
        if (doConnect) {
            doClientConnection()
        }
    }

    /**
     *  val jsonObject = JSONObject()//实例话JsonObject()
    //刚刚开机的时候发送一次时间搓
    jsonObject.put("type", "queryConfigTime")
    jsonObject.put("robotTimeStamp", Universal.timeStampRobotConfigSql)
    jsonObject.put("gateTimeStamp", Universal.timeStampReplyGateConfig)
    //发送Mqtt
    CloudMqttService.publish(JSONObject.toJSONString(jsonObject), true)连接MQTT服务器
     */
    private fun doClientConnection() {
        if (!mqttAndroidClient!!.isConnected && isConnectIsNormal) {
            try {
                mqttAndroidClient?.connect(mMqttConnectOptions, null, iMqttActionListener)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }/*没有可用网络的时候，延迟3秒再尝试重连*/

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNormal: Boolean
        get() {
            val connectivityManager = this.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                LogUtil.i("MQTT:当前网络名称：$name")
                true
            } else {
                LogUtil.i("MQTT:没有可用网络")
                /*没有可用网络的时候，延迟3秒再尝试重连*/Handler().postDelayed(
                    { doClientConnection() },
                    3000
                )
                false
            }
        }

    //MQTT是否连接成功的监听
    private val iMqttActionListener: IMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(arg0: IMqttToken) {
            UpdateReturn().method()
            Thread { UpdateReturn().assignment() }.start()
            LogUtil.i("MQTT:连接成功 ")
            try {
                mqttAndroidClient?.subscribe(
                    "$RESPONSE_TOPIC/${RobotStatus.SERIAL_NUMBER}",
//                    "$RESPONSE_TOPIC/#",
                    2
                ) //订阅主题，参数：主题、服务质量
                RobotStatus.mqttConnected = true
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        override fun onFailure(arg0: IMqttToken, arg1: Throwable) {
            arg1.printStackTrace()
            LogUtil.i("MQTT:连接失败 ")
            RobotStatus.mqttConnected = false
            doClientConnection() //连接失败，重连（可关闭服务器进行模拟）
        }
    }

    //订阅主题的回调
    private val mqttCallback: MqttCallback = object : MqttCallback {
        @Throws(Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage) {
            if (topic == "${RESPONSE_TOPIC}/${RobotStatus.SERIAL_NUMBER}") {
                MqttMessageHandler.receive(message)
                LogUtil.i("{MQTT:$topic}收到消息:" + String(message.payload))
            }
        }

        override fun deliveryComplete(arg0: IMqttDeliveryToken) {
//            LogUtil.i("MQTT:发送完成：${arg0.message}")
        }

        override fun connectionLost(arg0: Throwable) {
            LogUtil.i("MQTT:连接断开 ")
            doClientConnection() //连接断开，重连
        }
    }

    override fun onDestroy() {
        try {
            mqttAndroidClient?.disconnect() //断开连接
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mqttAndroidClient: MqttAndroidClient? = null
        const val  PUBLISH_TOPIC = "/sendi/robot/server" //发布主题
        const val  RESPONSE_TOPIC = "" //响应主题
//        const val PUBLISH_TOPIC_DELIVER = "/sendi/robot/delivery/server/android" //发布主题
//        const val RESPONSE_TOPIC_DELIVER = "/sendi/robot/delivery/client/android" //响应主题


        //        const val HOST = "tcp://103.215.45.135:1883" //测试环境服务器地址（协议+地址+端口号）
//        const val HOST = "tcp://103.215.44.45:1883" //正式环境服务器地址（协议+地址+端口号）
        const val USERNAME = "robot" //用户名
        const val PASSWORD = "sendi123" //密码

        @SuppressLint("HardwareIds")
        @RequiresApi(api = 26)
        val CLIENTSIDE: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示

        /**
         * 开启服务
         */
        fun startService(mContext: Context) {
            mContext.startService(Intent(mContext, MqttService::class.java))
        }

        /**
         * 关闭服务
         */
        fun stopService(mContext: Context) {
            mContext.stopService(Intent(mContext, MqttService::class.java))
        }

        /**
         * 发布 （模拟其他客户端发布消息）
         *
         * @param message 消息
         * @param needPrintLog 是否打印日志
         */
        fun publish(message: String, needPrintLog: Boolean = true, qos: Int = 2) {
            val topic = PUBLISH_TOPIC
            val retained = false
            try {
                if (mqttAndroidClient?.isConnected == false) return
                //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                mqttAndroidClient?.publish(
                    "$topic/${RobotStatus.SERIAL_NUMBER}", message.toByteArray(),
                    qos, retained
                )
                if (!needPrintLog) return
                LogUtil.i(
                    "MQTT：开始发送：{topic:${topic}/${RobotStatus.SERIAL_NUMBER}},message:${
                        String(
                            message.toByteArray()
                        )
                    }"
                )
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

}