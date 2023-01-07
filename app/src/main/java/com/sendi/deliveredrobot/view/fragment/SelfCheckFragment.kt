package com.sendi.deliveredrobot.view.fragment

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import chassis_msgs.SafeState
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.BaseFragment.TAG
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentSelfCheckBinding
import com.sendi.deliveredrobot.entity.ReplyGateConfig
import com.sendi.deliveredrobot.entity.RobotConfigSql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.helpers.CheckSelfHelper.OnCheckChangeListener
import com.sendi.deliveredrobot.model.QueryElevatorListModel
import com.sendi.deliveredrobot.model.QueryFloorListModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.SubMapName
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.DateUtil
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.inputfilter.DownloadUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import okhttp3.internal.toHexString
import org.litepal.LitePal
import org.litepal.LitePal.deleteAll
import sensor_msgs.BatteryState
import java.io.File
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [SelfCheckFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelfCheckFragment : Fragment() {
    private lateinit var binding: FragmentSelfCheckBinding
    private val basicSettingViewModel by viewModels<BasicSettingViewModel>({ requireActivity() })
    private lateinit var mView: View


    /** 充电点检测评分检测阈值 ,暂时写死检测分数为170*/
    private val scoreMin: Int = 100

    private var controller: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSelfCheck()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogUtil.i("系统回收SelfCheckFragment onSaveInstanceState")
    }


    @SuppressLint("SuspiciousIndentation")
    fun initSelfCheck() {
        MainScope().launch {
            withContext(Dispatchers.Default) {
                val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                val queryChargePoint = dao.queryChargePoint()
//                val queryChargeBootPoint = dao.queryChargeBootPoint()
                val queryChargeBootPoint = dao.queryChargePoint()
                RobotStatus.bootLocation = queryChargeBootPoint
                RobotStatus.originalLocation = queryChargePoint
                RobotStatus.currentLocation = RobotStatus.originalLocation
                // 从数据库获取basic
                // _config，并缓存
                val basicConfig = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                    .queryBasicConfig()
                basicSettingViewModel.basicConfig = basicConfig

                //-------------------自检---------------------
                var mStartProgressBar = 0
                var resCheck = 0;
                activity?.let {
                    resCheck = CheckSelfHelper.checkHardware(
                        90,
                        it,
                        object : OnCheckChangeListener {
                            override fun onCheckProgress(progress: Int) {
                                LogUtil.i("progress:" + progress)
                                MainScope().launch(Dispatchers.Main) {
                                    setAnimation(
                                        binding.selfCheckPb,
                                        mStartProgressBar,
                                        progress * 10
                                    )
                                }
                                mStartProgressBar = progress * 10
                            }
                        }
                    )
                }
                // ================================初始化状态机====================================
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
                LogUtil.i("初始化状态机")
                if (resCheck != 0x0f) {
                    withContext(Dispatchers.Main) {
                        var errorCode = ""
                        if (BuildConfig.IS_DEBUG) {
                            if (resCheck and 0x01 != 0x01){
                                errorCode = (if("" == errorCode) "" else "$errorCode,") +"镭射异常"
                            }
                            if (resCheck and 0x02 != 0x02){
                                errorCode = (if("" == errorCode) "" else "$errorCode,") +"电量异常"
                            }
                            if (resCheck and 0x04 != 0x04){
                                errorCode = (if("" == errorCode) "" else "$errorCode,") +"急停异常"
                            }
                        }else{
                            errorCode = resCheck.toHexString();
                        }
                        DialogHelper.selfCheckDialog("启动异常", "请尝试重启", errorCode, false, false, null).show()
                    }
                } else {
                    //硬件自检通过
//                    //初始化机器人序列号
//                    //设置底盘时间
                    //查询地图名字
                    RobotStatus.SERIAL_NUMBER = ROSHelper.getSerialNumber()
                    LogUtil.i("SERIAL_NUMBER:${RobotStatus.SERIAL_NUMBER}")
//                    ROSHelper.updateTime()
                    //缓存电梯指令
                    LiftCommand.getInstance(requireContext()).apply {
                        RobotCommand.LIFT_RELEASE_CONTROL_DOOR = this.liftDoorRelease.toInt()
                        RobotCommand.LIFT_OPEN_CONTROL_DOOR = this.liftDoorOpen.toInt()
                        if (liftControlTime != null) {
                            RobotCommand.LIFT_CONTROL_TIME = this.liftControlTime.toInt()
                        }
                    }
                    // 楼层
//                    CloudMqttService.publish(QueryFloorListModel().toString())
                    // 电梯
//                    CloudMqttService.publish(QueryElevatorListModel().toString())
//                    // ================================初始化云平台MQTT-SERVICE==============================
//                    CloudMqttService.startService(requireActivity())
//                    // ================================初始化上报机器人信息SERVICE=============================
//                    if(BuildConfig.IS_REPORT){
//                        ReportRobotStateService.startService(requireActivity())
//                    }
//                    UploadMapHelper.uploadMap()
                    if (RobotStatus.bootLocation != null) {
//                        设置地图
//                        ROSHelper.setNavigationMap(
//                            labelMapName = RobotStatus.bootLocation!!.subPath!!,
//                            pathMapName = RobotStatus.bootLocation!!.routePath!!
//                        )
                        LogUtil.d("SelfCheck"+"开始设置默认充电桩的点")
                        var setPoseRes = ROSHelper.setPoseClient(RobotStatus.bootLocation!!)
                        LogUtil.d("SelfCheck"+"设置默认充电桩的点完成")
                        if (RobotStatus.adapterState.value == SafeState.STATE_IS_TRIGGING) {
                            //适配器已接入
                            if (RobotStatus.batterySupplyStatus.value == BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
                                //充电中
                                LogUtil.i("手动充电中")
                                withContext(Dispatchers.Main) {
                                    val chargingDialog = DialogHelper.initChargingDialog(this@SelfCheckFragment)
                                    chargingDialog.show()
                                    RobotStatus.batterySupplyStatus.observe(this@SelfCheckFragment) {
                                        if (RobotStatus.batterySupplyStatus.value != BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
                                            LogUtil.i("已取消手动充电")
                                            chargingDialog.dismiss()
                                            if (RobotStatus.adapterState.value == SafeState.STATE_IS_TRIGGING) {
                                                //请拔出电源线
                                                DialogHelper.pullOutAdapterDialog.show()
                                            }
                                        }
                                    }

                                    RobotStatus.adapterState.observe(this@SelfCheckFragment) {
                                        if (RobotStatus.adapterState.value != SafeState.STATE_IS_TRIGGING) {
                                            LogUtil.i("已拔出电源线")
                                            DialogHelper.pullOutAdapterDialog.dismiss()
                                            getScore(0)
                                        }
                                    }

                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    //请拔出电源线
                                    DialogHelper.pullOutAdapterDialog.show()
                                    RobotStatus.adapterState.observe(this@SelfCheckFragment) {
                                        if (RobotStatus.adapterState.value != SafeState.STATE_IS_TRIGGING) {
                                            LogUtil.i("已拔出电源线")
                                            DialogHelper.pullOutAdapterDialog.dismiss()
                                            getScore(0)
                                        }
                                    }
                                }
                            }
                        } else if (RobotStatus.adapterState.value == SafeState.STATE_IS_NOT_TRIGGING) {
                            //适配器已拔除
                            if (RobotStatus.batterySupplyStatus.value == BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
                                //充电中
                                LogUtil.i("自动充电中");
                                withContext(Dispatchers.Main) {
                                    //判断当前电量是否小于最低电,小于则去充电
                                    if ((RobotStatus.batteryPower.value!! * 100).toInt() < RobotStatus.LOW_POWER_VALUE)
                                    {
                                        val chargingDialog = DialogHelper.initChargingDialog(this@SelfCheckFragment)
                                        chargingDialog.show()
                                        RobotStatus.batterySupplyStatus.observe(this@SelfCheckFragment) {
                                            if (RobotStatus.batterySupplyStatus.value != BatteryState.POWER_SUPPLY_STATUS_CHARGING) {
                                                LogUtil.i("已取消自动充电")
                                                chargingDialog.dismiss()
                                                findNavController().popBackStack()
                                                controller!!.navigate(R.id.action_selfCheckFragment_to_updateConfigurationFragment)
                                            }
                                        }
                                        RobotStatus.batteryPower.observe(this@SelfCheckFragment) {
                                            if ((RobotStatus.batteryPower.value!! * 100).toInt() >= RobotStatus.LOW_POWER_VALUE) {
                                                LogUtil.i("电量已超过最小阈值")
                                                chargingDialog.dismiss()
                                                findNavController().popBackStack()
                                                controller!!.navigate(R.id.action_selfCheckFragment_to_updateConfigurationFragment)
                                            }
                                        }
//                                        findNavController().navigate(R.id.action_selfCheckFragment_to_chargeFragment)
                                    }else{
                                        findNavController().popBackStack()
                                        controller!!.navigate(R.id.action_selfCheckFragment_to_updateConfigurationFragment)
                                    }
                                }
                            } else {
                                getScore(0)
                            }
                        } else {
                            getScore(0)
                        }
                    } else {
                        LogUtil.i("数据异常未设置默认充电桩")
                        findNavController().popBackStack()
                        //默认充电桩点未设置
//                        findNavController().navigate(R.id.inputPasswordFragment,
//                            Bundle().apply {
//                                putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_SETTING)
//                            }
//                        )
                    }
                }
            }
        }
    }

    /**
     * @param type 0:充电状态 未使用
     */
    private fun getScore(type: Int) {
        //匹配机器人起点的分数

//        var score: Int = -1
//        if (RobotStatus.bootLocation != null) {
//            LogUtil.d("SelfCheck 开始获取分数")
//            score = ROSHelper.checkPoseClient(RobotStatus.bootLocation!!)
//            LogUtil.d("SelfCheck 获取到分数:$score")
//        }
//        if (score > scoreMin) {
        controller!!.navigate(R.id.action_selfCheckFragment_to_updateConfigurationFragment)

//        } else {
//            MainScope().launch {
//                withContext(Dispatchers.Main) {
//                    DialogHelper.selfCheckDialog("定位检测异常", "请按下急停，将机器人推到充电桩的位置进行对接", "", true, true,
//                        object : DialogHelper.DialogListener {
//                            override fun cancel() {
//                                findNavController().popBackStack()
//                                findNavController().navigate(R.id.homeFragment)
//                            }
//
//                            override fun confirm() {
//                                MainScope().launch {
//                                    withContext(Dispatchers.Default) {
//                                        getScore(0)
//                                    }
//                                }
//                            }
//                        }).show()
//                }
//            }
//        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_self_check, container, false)
        binding = DataBindingUtil.bind(mView)!!
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)

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

    fun setAnimation(view: ProgressBar, mStartProgressBar: Int, mProgressBar: Int) {
        val animator: ValueAnimator =
            ValueAnimator.ofInt(mStartProgressBar, mProgressBar).setDuration(2000)
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
                view.setProgress(valueAnimator.getAnimatedValue() as Int)
            }
        })
        animator.start()
    }
}