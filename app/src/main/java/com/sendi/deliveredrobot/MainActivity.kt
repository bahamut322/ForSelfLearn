package com.sendi.deliveredrobot

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.hacknife.wifimanager.*
import com.sendi.deliveredrobot.databinding.ActivityMainBinding
import com.sendi.deliveredrobot.entity.AdvertisingConfigDB
import com.sendi.deliveredrobot.entity.BasicSetting
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.handler.TopicHandler
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.model.DefaultModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.newUpdata
import com.sendi.deliveredrobot.navigationtask.RobotStatus.sdScreenStatus
import com.sendi.deliveredrobot.receiver.NavigationReceiver
import com.sendi.deliveredrobot.receiver.SendTaskFinishReceiver
import com.sendi.deliveredrobot.receiver.SimNetStatusReceiver
import com.sendi.deliveredrobot.receiver.TimeChangeReceiver
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.*
import com.sendi.deliveredrobot.viewmodel.DateViewModel
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.LitePal.findAll
import org.litepal.LitePal.findFirst
import java.text.SimpleDateFormat
import java.util.*


class
MainActivity : BaseActivity(), OnWifiChangeListener, OnWifiConnectListener,
    OnWifiStateChangeListener {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private val dateViewModel by viewModels<DateViewModel>()
    private val fileNames =
        arrayOf("advdefault.jpg", "explandefault.jpg", "guidedefault.jpg", "usherdefault.jpg","default_explain.gif","businessdefault.jpg")
    @SuppressLint("SimpleDateFormat")
    private val sdf2 = SimpleDateFormat("HH:mm")
    private lateinit var navigationReceiver: NavigationReceiver
    private lateinit var timeChangeReceiver: TimeChangeReceiver
    private lateinit var simNetStatusReceiver: SimNetStatusReceiver
    private lateinit var sendTaskFinishReceiver: SendTaskFinishReceiver
    private var screenland : MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            screenland.value = false
            Log.e(TAG,"\n 当前屏幕为横屏")
        } else {
            screenland.value = true
            Log.e(TAG,"\n 当前屏幕为竖屏")
        }
        super.onConfigurationChanged(newConfig)
        Log.e("TAG", "onConfigurationChanged")
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        //创建LitePal数据库
        LitePal.getDatabase()
        val basicSettings = findAll(BasicSetting::class.java)
        val basicSetting = BasicSetting()
        if (basicSettings.size == 0) {
            basicSetting.id = 1
            basicSetting.save()
        }
        val handler = Handler(Looper.getMainLooper())
        screenland.observe(this) {
            if (it) {
                handler.postDelayed({
                    Log.d(TAG, "onCreate: Pmu")
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }, 4000) // 延迟
            }
        }


        pushImage(fileNames)
        AppUtils.checkPermission(this, 0)
        //检查日志
        FileUtil.checkAndDeleteLogFilesCache()
        //初始化ToastUtil
        ToastUtil.initial(this)
        //隐藏状态栏按钮
        statusBarDisable(!BuildConfig.IS_DEBUG, this)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        //双屏异显的方法
        ShowPresentationByDisplaymanager()
        with(binding.textViewTime) {
            val date = Date()
            text = sdf2.format(date)
        }
        //-----------------初始化全局Dialog-----------------
        DialogHelper.initDialog(this)
        //-----------------全屏+隐藏虚拟导航栏、状态栏-----------------
        val window: Window = this.window
        NavigationBarUtil.hideNavigationBar(window)
        //-----------------设置页面跳转receiver-------------------
        navController = Navigation.findNavController(this, R.id.fragmentContainerView)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            LogUtil.i("mainactivity onDestinationChangedListener")
            BaseVoiceRecorder.getInstance()?.removeCallback()
        }
        navigationReceiver = NavigationReceiver()
        navigationReceiver.navController = navController
        registerReceiver(navigationReceiver, IntentFilter(ACTION_NAVIGATE))
        RobotStatus.PassWordToSetting.postValue(false);
        //-----------------设置状态栏状态receiver--------------------
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_TICK) //每分钟变化
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED) //设置了系统时区
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED) //设置了系统时间
        timeChangeReceiver = TimeChangeReceiver()
        timeChangeReceiver.dateViewModel = dateViewModel
        dateViewModel.date.observe(this) {
            with(binding.textViewTime) {
                text = it
            }
        }
        registerReceiver(timeChangeReceiver, intentFilter)
        //-----------------设置送物任务结束receiver------------------
        sendTaskFinishReceiver = SendTaskFinishReceiver()
        registerReceiver(sendTaskFinishReceiver, IntentFilter(ACTION_SEND_TASK_FINISH))
        //===========添加移动网络监听receiver===============
        val intentFilter2 = IntentFilter()
        intentFilter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION) //监听移动网络连接状态
        intentFilter2.addAction(ACTION_SIM_STATE_CHANGED)
        simNetStatusReceiver = SimNetStatusReceiver()
        simNetStatusReceiver.binding = binding
        registerReceiver(simNetStatusReceiver, intentFilter2)
        //==========添加wifi状态监听================
        WifiManager.create(MyApplication.instance!!).apply {
            setOnWifiChangeListener(this@MainActivity)
            setOnWifiConnectListener(this@MainActivity)
            setOnWifiStateChangeListener(this@MainActivity)
        }
        //-------------------TopicHandler--------------
        TopicHandler.create(navController)
        MainScope().launch {
            withContext(Dispatchers.Default) {
                if (BillManager.billList().isEmpty()) {
                    val queryPoint =
                        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                            .queryChargePoint()
                    RobotStatus.originalLocation = queryPoint
                    RobotStatus.currentLocation = RobotStatus.originalLocation
//                    LogUtil.d("当前所在楼层" + RobotStatus.currentLocation!!.floorName)
                }
            }
        }
        screenRenew()
        initBatteryStatusListener()
        RobotStatus.tenancy.observe(this) {
            binding.textViewRobotName.apply {
                val robotName = it.robotName
                visibility = if (TextUtils.isEmpty(robotName)) {
                    View.GONE
                } else {
                    text = robotName
                    View.VISIBLE
                }
            }
        }
        RobotStatus.robotConfig?.observe(this){
            if (it != null) {
                WakeupWordHelper.wakeupWord = it.wakeUpWord?:""
            }
        }
        WakeupWordHelper.wakeupWord = QuerySql.robotConfig().wakeUpWord
    }

    override fun onBackPressed() {
        //重写onBackPressed是为了禁用返回键
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(navigationReceiver)
        unregisterReceiver(timeChangeReceiver)
        unregisterReceiver(simNetStatusReceiver)
    }

    /**
     * @describe 电量监听
     */
    @SuppressLint("SetTextI18n")
    private fun initBatteryStatusListener() {
        RobotStatus.batteryPower.observe(this) {
            val batteryPower = (it * 100).toInt()
            binding.textViewPowerStatus.text = "$batteryPower%"
            when (RobotStatus.chargeStatus.value) {
                true -> {
                    binding.imageViewFlash.visibility = View.VISIBLE
                    with(binding.imageViewPowerStatus) {
                        when (batteryPower) {
                            in 0 until 20 -> setBackgroundResource(R.drawable.ic_battery_4)
                            in 21 until 40 -> setBackgroundResource(R.drawable.ic_battery_5)
                            in 41 until 60 -> setBackgroundResource(R.drawable.ic_battery_6)
                            in 61 until 80 -> setBackgroundResource(R.drawable.ic_battery_7)
                            in 81 until 100 -> setBackgroundResource(R.drawable.ic_battery_8)
                            100 -> {
                                setBackgroundResource(R.drawable.ic_battery_9)
                                binding.imageViewFlash.visibility = View.GONE
                            }
                        }
                    }
                }

                false -> {
                    binding.imageViewFlash.visibility = View.GONE
                    with(binding.imageViewPowerStatus) {
                        when (batteryPower) {
                            in 0 until 10 -> setBackgroundResource(R.drawable.ic_battery_0)
                            in 10 until 20 -> setBackgroundResource(R.drawable.ic_battery_1)
                            in 20 until 80 -> setBackgroundResource(R.drawable.ic_battery_2)
                            in 80..100 -> setBackgroundResource(R.drawable.ic_battery_3)
                        }
                    }
                }

                else -> {}
            }
        }
        RobotStatus.chargeStatus.observe(this) {
            val batteryPower = (RobotStatus.batteryPower.value!! * 100).toInt()
            when (it) {
                true -> {
                    binding.imageViewFlash.visibility = View.VISIBLE
                    with(binding.imageViewPowerStatus) {
                        when (batteryPower) {
                            in 0 until 20 -> setBackgroundResource(R.drawable.ic_battery_4)
                            in 21 until 40 -> setBackgroundResource(R.drawable.ic_battery_5)
                            in 41 until 60 -> setBackgroundResource(R.drawable.ic_battery_6)
                            in 61 until 80 -> setBackgroundResource(R.drawable.ic_battery_7)
                            in 81 until 100 -> setBackgroundResource(R.drawable.ic_battery_8)
                            100 -> {
                                setBackgroundResource(R.drawable.ic_battery_9)
                                binding.imageViewFlash.visibility = View.GONE
                            }
                        }
                    }
                }

                false -> {
                    binding.imageViewFlash.visibility = View.GONE
                    with(binding.imageViewPowerStatus) {
                        when (batteryPower) {
                            in 0 until 10 -> setBackgroundResource(R.drawable.ic_battery_0)
                            in 10 until 20 -> setBackgroundResource(R.drawable.ic_battery_1)
                            in 20 until 80 -> setBackgroundResource(R.drawable.ic_battery_2)
                            in 80..100 -> setBackgroundResource(R.drawable.ic_battery_3)
                        }
                    }
                }
            }

        }
        RobotStatus.batteryPower.value = 0f
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //处理软键盘弹出时虚拟导航栏不会自动收回
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onWifiChanged(wifis: MutableList<IWifi>?) {
        if (wifis != null && wifis.size > 0 && wifis[0].isConnected) {
            val connectWifi = wifis[0]
            with(binding.imageViewWifiStatus) {
                visibility = View.VISIBLE
                when {
                    connectWifi.level() <= -100 -> this.setBackgroundResource(com.sendi.deliveredrobot.R.drawable.ic_wifi_level_0)
                    connectWifi.level() in -99..-88 || connectWifi.level() in -87..-66 || connectWifi.level() in -65 until 55 -> setBackgroundResource(
                        R.drawable.ic_wifi_level_1
                    )

                    connectWifi.level() >= -55 -> setBackgroundResource(R.drawable.ic_wifi_level_2)
                }
            }
        } else {
            with(binding.imageViewWifiStatus) {
                visibility = View.GONE
            }
        }
    }

    override fun onConnectChanged(status: Boolean) {
        binding.imageViewWifiStatus.visibility =
            when (status) {
                true -> View.VISIBLE
                false -> View.GONE
            }
    }

    override fun onStateChanged(state: State?) {
        binding.imageViewWifiStatus.visibility =
            when (state) {
                State.ENABLING, State.ENABLED -> View.VISIBLE
                State.DISABLING, State.DISABLED, State.UNKNOWN -> View.GONE
                else -> View.GONE
            }
    }

    @SuppressLint("PrivateApi")
    private fun statusBarDisable(disable: Boolean, mContext: Context) {
        try {
            // 隐藏导航栏相关的按钮
            val disableNavigation = (0x00010000
                    or 0x00400000
                    or 0x00020000
                    or 0x00040000
                    or 0x00080000
                    or 0x00100000
                    or 0x00200000 or 0x01000000
                    or 0x00800000)
            val disableNone = 0x00000000
            //获得ServiceManager类
            val serviceManager = Class.forName("android.os.ServiceManager")

            //获得ServiceManager的getService方法
            val getService = serviceManager.getMethod("getService", String::class.java)

            //调用getService获取RemoteService
            val oRemoteService = getService.invoke(null, "statusbar")

            //获得IStatusBarService.Stub类
            val cStub = Class.forName("com.android.internal.statusbar.IStatusBarService\$Stub")
            //获得asInterface方法
            val asInterface = cStub.getMethod("asInterface", IBinder::class.java)
            //调用asInterface方法获取IStatusBarService对象
            val oIStatusBarService = asInterface.invoke(null, oRemoteService)
            //获得disable()方法
            val disableMethod = oIStatusBarService.javaClass.getMethod(
                "disable",
                Int::class.javaPrimitiveType,
                IBinder::class.java,
                String::class.java
            )
            //调用disable()方法
            if (disable) {
                disableMethod.invoke(
                    oIStatusBarService,
                    disableNavigation,
                    Binder(),
                    mContext.packageName
                )
            } else {
                disableMethod.invoke(
                    oIStatusBarService,
                    disableNone,
                    Binder(),
                    mContext.packageName
                )
            }

            // 关闭截图按钮 rk3288 的源码加了这个字段判断截图键
            Settings.System.putInt(contentResolver, "screenshot_button_show", 0)
            // 系统按键音 0关闭，1开启
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 0)
            //            Class<?> systemProperties = Class
//                    .forName("android.os.SystemProperties");
//            Method getProperties = systemProperties.getMethod("set", java.lang.String.class, java.lang.String.class);
//            getProperties.invoke(systemProperties, "ro.config.systembar.voiceicon", "false");
            // rk3288 的源码加了这个字段判断音量键，但是 ro. 开头的是只读属性。。。所以改不了
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface MyTouchListener {
        fun onTouchEvent(event: MotionEvent?)
    }

    // 保存MyTouchListener接口的列表
    private val myTouchListeners = ArrayList<MyTouchListener>()

    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     * @param listener
     */
    fun registerMyTouchListener(listener: MyTouchListener) {
        myTouchListeners.add(listener)
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     * @param listener
     */
    fun unRegisterMyTouchListener(listener: MyTouchListener) {
        myTouchListeners.remove(listener)
    }

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (listener in myTouchListeners) {
            listener.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED"

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: ComponentActivity
    }


    private fun screenRenew() {
        var doubleScreen = 0
        //监听观察者更新副屏内容
        newUpdata.observe(this) {
            if (newUpdata.value == 1) {
                newUpdata.postValue(0)
                if (doubleScreen == 0) {
                    sdScreenStatus!!.postValue(0)
                } else if (doubleScreen == 1) {
                    sdScreenStatus!!.postValue(1)
                } else if (doubleScreen == 2) {
                    sdScreenStatus!!.postValue(2)
                }else if (doubleScreen == 4){
                    sdScreenStatus!!.postValue(4)
                }
            }
            if (newUpdata.value ==3) {
                default(Universal.advDefault,true)
            }
        }
        sdScreenStatus!!.observe(this) {
            advertisingConfigDB = findFirst(AdvertisingConfigDB::class.java) //查询副屏第一条数据
            if (sdScreenStatus!!.value == 0 && mPresentation != null ) {
                doubleScreen = sdScreenStatus!!.value!!
                if( advertisingConfigDB != null && advertisingConfigDB.type !=0) {
                    layoutThis(
                        advertisingConfigDB.picPlayTime,
                        Universal.advertisement,
                        advertisingConfigDB.type,
                        advertisingConfigDB.textPosition,
                        advertisingConfigDB.fontLayout,
                        advertisingConfigDB.fontContent,
                        advertisingConfigDB.fontBackGround,
                        advertisingConfigDB.fontColor,
                        advertisingConfigDB.fontSize,
                        advertisingConfigDB.picType,
                        advertisingConfigDB.videolayout,
                        advertisingConfigDB.videoAudio,
                        true
                    )
                }else{
                    default(Universal.advDefault,true)
                }
            }
            if (sdScreenStatus!!.value == 1 && mPresentation != null) {
                doubleScreen = sdScreenStatus!!.value!!
                if ( Universal.bigScreenType !=0) {
                    layoutThis(
                        Universal.picPlayTime,
                        Universal.Secondary,
                        Universal.bigScreenType,
                        Universal.textPosition,
                        Universal.fontLayout,
                        Universal.fontContent,
                        Universal.fontBackGround,
                        Universal.fontColor,
                        Universal.fontSize,
                        Universal.picTypeNum,
                        Universal.TempVideoLayout,
                        Universal.AllvideoAudio,
                        false
                    )
                }else{
                    default(Universal.usherDefault,false)
                }
            }
            if (sdScreenStatus!!.value == 2 && mPresentation != null) {
                doubleScreen = sdScreenStatus!!.value!!
                if (RobotStatus.SecondModel!!.value?.type !=0) {
                    layoutThis(
                        RobotStatus.SecondModel!!.value?.picPlayTime!!,
                        RobotStatus.SecondModel!!.value?.file,
                        RobotStatus.SecondModel!!.value?.type!!,
                        RobotStatus.SecondModel!!.value?.textPosition!!,
                        RobotStatus.SecondModel!!.value?.fontLayout!!,
                        RobotStatus.SecondModel!!.value?.fontContent,
                        RobotStatus.SecondModel!!.value?.fontBackGround,
                        RobotStatus.SecondModel!!.value?.fontColor,
                        RobotStatus.SecondModel!!.value?.fontSize!!,
                        RobotStatus.SecondModel!!.value?.picType!!,
                        RobotStatus.SecondModel!!.value?.videolayout!!,
                        RobotStatus.SecondModel!!.value?.videoAudio!!
                        ,false
                    )
                }else{
                    default(Universal.explainDefault,false)
                }
            }

            if (sdScreenStatus!!.value == 4 && mPresentation != null) {
                doubleScreen = sdScreenStatus!!.value!!
                if (RobotStatus.businessBigModel!!.value?.type !=0) {
                    layoutThis(
                        RobotStatus.businessBigModel!!.value?.picPlayTime!!,
                        RobotStatus.businessBigModel!!.value?.file,
                        RobotStatus.businessBigModel!!.value?.type!!,
                        RobotStatus.businessBigModel!!.value?.textPosition!!,
                        RobotStatus.businessBigModel!!.value?.fontLayout!!,
                        RobotStatus.businessBigModel!!.value?.fontContent,
                        RobotStatus.businessBigModel!!.value?.fontBackGround,
                        RobotStatus.businessBigModel!!.value?.fontColor,
                        RobotStatus.businessBigModel!!.value?.fontSize!!,
                        RobotStatus.businessBigModel!!.value?.picType!!,
                        RobotStatus.businessBigModel!!.value?.videolayout!!,
                        RobotStatus.businessBigModel!!.value?.videoAudio!!,
                        false
                    )
                }else{
                    default(Universal.businessDefault,false)
                }
            }

            if (sdScreenStatus!!.value == 3 && mPresentation != null) {
                doubleScreen = sdScreenStatus!!.value!!
                if (RobotStatus.businessBigModel!!.value?.type !=0) {
                    layoutThis(
                        RobotStatus.businessBigModel!!.value?.picPlayTime!!,
                        RobotStatus.businessBigModel!!.value?.file,
                        RobotStatus.businessBigModel!!.value?.type!!,
                        RobotStatus.businessBigModel!!.value?.textPosition!!,
                        RobotStatus.businessBigModel!!.value?.fontLayout!!,
                        RobotStatus.businessBigModel!!.value?.fontContent,
                        RobotStatus.businessBigModel!!.value?.fontBackGround,
                        RobotStatus.businessBigModel!!.value?.fontColor,
                        RobotStatus.businessBigModel!!.value?.fontSize!!,
                        RobotStatus.businessBigModel!!.value?.picType!!,
                        RobotStatus.businessBigModel!!.value?.videolayout!!,
                        RobotStatus.businessBigModel!!.value?.videoAudio!!,
                        false
                    )
                }else{
                    default(Universal.guideDefault,false)
                }
            }

        }
        RobotStatus.SecondModel!!.observe(this) {
            if (mPresentation != null) {
                layoutThis(
                    it?.picPlayTime ?: 30,
                    it?.file ?: "",
                    it?.type ?: 0,
                    it?.textPosition ?: 0,
                    it?.fontLayout?: 0,
                    it?.fontContent,
                    it?.fontBackGround,
                    it?.fontColor,
                    it?.fontSize?: 0,
                    it?.picType?: 0,
                    it?.videolayout?: 0,
                    it?.videoAudio?: 0,
                    false
                )
            }
        }
        RobotStatus.businessBigModel!!.observe(this) {
            if (mPresentation != null) {
                layoutThis(
                    it?.picPlayTime ?: 30,
                    it?.file ?: "",
                    it?.type ?: 0,
                    it?.textPosition ?: 0,
                    it?.fontLayout?: 0,
                    it?.fontContent,
                    it?.fontBackGround,
                    it?.fontColor,
                    it?.fontSize?: 0,
                    it?.picType?: 0,
                    it?.videolayout?: 0,
                    it?.videoAudio?: 0,
                    false
                )
            }
        }
    }
    //默认+下载时大屏幕的样式
    private fun default( picFile : String,boolean: Boolean){
        val defaultModel = DefaultModel(file = picFile, picPlayTime = 4,type = 1, textPosition = 0, fontLayout = 0, fontContent = "", fontBackGround = (R.color.white).toString(), fontColor = (R.color.white).toString(), fontSize = 1, picType = 1, videolayout = 0, videoAudio = 0)
        layoutThis(
            defaultModel.picPlayTime!!,
            defaultModel.file,
            defaultModel.type!!,
            defaultModel.textPosition!!,
            defaultModel.fontLayout!!,
            defaultModel.fontContent,
            defaultModel.fontBackGround,
            defaultModel.fontColor,
            defaultModel.fontSize!!,
            defaultModel.picType!!,
            defaultModel.videolayout!!,
            defaultModel.videoAudio!!,
            boolean
        )
    }
}