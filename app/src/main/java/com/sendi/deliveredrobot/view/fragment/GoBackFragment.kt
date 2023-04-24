package com.sendi.deliveredrobot.view.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import chassis_msgs.SafeState
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentGoBackBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.LowPowerDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.properties.Delegates

class GoBackFragment : Fragment() {
    private lateinit var binding: FragmentGoBackBinding
    private var timer: Timer? = null
    private var timer2:Timer? = null
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var gifGoBack:RequestBuilder<Drawable>
    private lateinit var mainScope: CoroutineScope
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })

    private var state by Delegates.observable(0){
            _, _, newValue ->
        when (newValue) {
            0 -> {
                IdleGateDataHelper.reportIdleGateCount()
                binding.groupGuideButton.apply {
                    visibility = View.GONE
                }
                binding.groupSendButton.apply {
                    visibility = View.GONE
                }
                binding.groupState3.apply {
                    visibility = View.GONE
                }
//                binding.groupState2.apply {
//                    visibility = View.VISIBLE
//                }
                binding.imageViewGoBack.apply {
                    isEnabled = true
                }
//                if (oldValue == 1) {
//                    //继续
//                    timer?.cancel()
//                    mainScope.launch {
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                    }
//                }
                mainScope.launch {
                    withContext(Dispatchers.Default) {
                        virtualTaskExecute(5, "GoBackFragment语音前")
                        if (CommonHelper.atChargePointFloor()) {
                            //如果是在充电桩的楼层
                            if (BillManager.currentBill()?.taskId()?.startsWith("D") == true || BillManager.currentBill()?.taskId()?.startsWith(
                                    "wx"
                                ) == true
                            ) {
//                                AudioMngHelper(MyApplication.instance!!).setVoice100(
//                                    (basicSettingViewModel?.basicConfig?.sendVolumeLobby ?: 60) / 2
//                                )
                            }
                            if (BillManager.currentBill()?.taskId()?.startsWith("G") == true) {
//                                AudioMngHelper(MyApplication.instance!!).setVoice100(
//                                    (basicSettingViewModel?.basicConfig?.guideVolumeLobby ?: 40) / 2
//                                )
                            }
                            timer2?.cancel()
                            timer2?.purge()
                            timer2 = Timer()
                            timer2Schedule()
                        }
                    }
                }
                binding.imageViewGoBack.isEnabled = true
                binding.textViewBack.isEnabled = false
                seconds.value = 30
                LogUtil.i("current:state2")
            }
            1 -> {
                IdleGateDataHelper.reportIdleGateCount(0)
//                resetButton()
                timer?.cancel()
                timer2?.cancel()
                timer2?.purge()
                if (RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED) {
                    SpeakHelper.speak(getString(R.string.can_i_help_you))
                    timer = Timer()
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            mainScope.launch {
                                withContext(Dispatchers.Main) {
                                    seconds.value = seconds.value?.minus(1)
                                }
                            }
                        }
                    }, Date(), 1000)
                }
                LogUtil.i("current:state3")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gifGoBack =  Glide.with(this).asDrawable().load(R.drawable.img_goback)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_go_back, container, false)
    }

    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        seconds = MutableLiveData(30)
//        binding.bottomAlarmTextViewGoBack.apply {
//            bottomAlarmText1 = resources.getString(R.string.go_back)
//        }
//        Universal.Model = "返回"
//        binding.imageViewGoBack.apply {
//            isEnabled = false
//            gifGoBack.into(this)
//            setOnClickListener {
//                isEnabled = false
//                RobotStatus.stopButtonPressed.value = RobotCommand.STOP_BUTTON_DEFAULT
//                mainScope.launch {
//                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)){
//                        state = 1
//                    }else{
//                        ToastUtil.show("暂停失败，请重试")
//                        isEnabled = true
//                    }
//                }
//            }
//        }
//        binding.buttonGuide.apply {
//            isEnabled = false
//            setOnClickListener {
//                isEnabled = false
//                if (seconds.value!! > 0) {
//                    timer?.cancel()
//                    if ((RobotStatus.batteryPower.value!! * 100).toInt() < RobotStatus.LOW_POWER_VALUE) {
//                        LowPowerDialog(requireContext(), lowPowerDialogListener = object :
//                            LowPowerDialog.LowPowerDialogListener {
//                            override fun timeUp(dialog: LowPowerDialog) {
//                                mainScope.launch {
//                                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                                        timer?.cancel()
//                                        state = 0
//                                        dialog.dismiss()
//                                    }else{
//                                        isEnabled = true
//                                        ToastUtil.show("继续失败，请重试")
//                                    }
//                                }
//                            }
//
//                            override fun buttonPress(dialog: LowPowerDialog) {
//                                mainScope.launch {
//                                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                                        timer?.cancel()
//                                        state = 0
//                                        dialog.dismiss()
//                                    }else{
//                                        isEnabled = true
//                                        ToastUtil.show("继续失败，请重试")
//                                    }
//                                }
//                            }
//                        }).show()
//                        return@setOnClickListener
//                    }
//                    when (basicSettingViewModel?.basicConfig?.guideModeVerifyPassword) {
//                        0 -> {
//                            findNavController().navigate(
//                                R.id.inputRoomNumberFragment,
//                                Bundle().apply {
//                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GO_BACKING_GUIDE)
//                                },
//                            )
//                        }
////                        1 -> {
////                            findNavController().navigate(
////                                R.id.inputPasswordFragment,
////                                Bundle().apply {
////                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GO_BACKING_GUIDE)
////                                },
////                            )
////                        }
//                    }
//
//                }
//            }
//        }

//        binding.buttonSend.apply {
//            isEnabled = false
//            setOnClickListener {
//                isEnabled = false
//                if (seconds.value!! > 0) {
//                    timer?.cancel()
//                    if ((RobotStatus.batteryPower.value!! * 100).toInt() < RobotStatus.LOW_POWER_VALUE) {
//                        LowPowerDialog(requireContext(), lowPowerDialogListener = object :
//                            LowPowerDialog.LowPowerDialogListener {
//                            override fun timeUp(dialog: LowPowerDialog) {
//                                mainScope.launch {
//                                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                                        timer?.cancel()
//                                        state = 0
//                                        dialog.dismiss()
//                                    }else{
//                                        isEnabled = true
//                                        ToastUtil.show("继续失败，请重试")
//                                    }
//                                }
//                            }
//
//                            override fun buttonPress(dialog: LowPowerDialog) {
//                                mainScope.launch {
//                                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                                        timer?.cancel()
//                                        state = 0
//                                        dialog.dismiss()
//                                    }else{
//                                        isEnabled = true
//                                        ToastUtil.show("继续失败，请重试")
//                                    }
//                                }
//                            }
//                        }).show()
//                        return@setOnClickListener
//                    }
//                    when (basicSettingViewModel?.basicConfig?.sendModeVerifyPassword) {
//                        0 -> {
//                            findNavController().navigate(
//                                R.id.putObjectFragment,
//                                Bundle().apply {
//                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GO_BACKING_SEND)
//                                },
//                            )
//                        }
////                        1 -> {
////                            findNavController().navigate(
//////                                R.id.inputPasswordFragment,
////                                Bundle().apply {
////                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GO_BACKING_SEND)
////                                },
////                            )
////                        }
//                    }
//                }
//            }
//        }
//        binding.textViewBack.apply {
//            isEnabled = false
//            setOnClickListener {
//                isEnabled = false
//                mainScope.launch {
//                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                        timer?.cancel()
//                        state = 0
//                    }else{
//                        isEnabled = true
//                        ToastUtil.show("继续失败，请重试")
//                    }
//                }
//
//            }
//        }
//        state = 0
//        seconds.observe(viewLifecycleOwner) {
//            if (it < 1) {
//                mainScope.launch {
//                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
//                        timer?.cancel()
//                        state = 0
//                    }else{
//                        ToastUtil.show("继续失败，请重试")
//                    }
//                }
//            }
//            binding.textViewSecond.text = CommonHelper.getTimeSpan(it, 1.33f)
//        }
//        SafeStateTopic.setSafeStateListener {
//                safeState ->
//            when (safeState.safeState) {
//                SafeState.STATE_IS_TRIGGING -> {
//                    //按下
//                    if (state == 0) {
//                        //如果当前不在暂停状态
//                        if (RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_CONTINUE) {
//                            mainScope.launch {
//                                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
//                            }
//                        }
//                    } else if (state == 1) {
//                        timer?.cancel()
//                    }
//                    timer2?.cancel()
//                    timer2?.purge()
//
//                }
//                SafeState.STATE_IS_NOT_TRIGGING -> {
//                    //抬起
//                    if (state == 1) {
//                        //如果当前不在导航状态
//                        timer = Timer()
//                        timer?.schedule(object : TimerTask() {
//                            override fun run() {
//                                mainScope.launch {
//                                    withContext(Dispatchers.Main) {
//                                        seconds.value = seconds.value?.minus(1)
//                                    }
//                                }
//                            }
//                        }, Date(), 1000)
//                    } else {
//                        if (RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_PAUSE) {
//                            mainScope.launch {
//                                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
//                            }
//                        }
//                    }
//                    mainScope.launch {
//                        if (CommonHelper.atChargePointFloor()){
////                                if(binding.motionLayoutGoBack.currentState != R.id.state3){
//                            if(state == 0){
//                                if(BillManager.currentBill()?.taskId()?.startsWith("D") == true || BillManager.currentBill()?.taskId()?.startsWith("wx") == true){
////                                    AudioMngHelper(MyApplication.instance!!).setVoice100(
////                                        (basicSettingViewModel?.basicConfig?.sendVolumeLobby?: 60) / 2)
//                                }
//                                if(BillManager.currentBill()?.taskId()?.startsWith("G") == true){
////                                    AudioMngHelper(MyApplication.instance!!).setVoice100(
////                                        (basicSettingViewModel?.basicConfig?.guideVolumeLobby?:40) / 2)
//                                }
//                                timer2 = Timer()
//                                timer2Schedule()
//                            }
//                        }
//                    }
//                }
//                SafeState.TYPE_MOTOR_CURRENT -> {
//                    when (safeState.safeState) {
//                        SafeState.STATE_IS_TRIGGING -> {
//                            LogUtil.i("进入堵转状态")
//                            if (RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_CONTINUE) {
//                                mainScope.launch(Dispatchers.Default) {
//                                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)) {
//                                        withContext(Dispatchers.Main){
////                                            DialogHelper.getChassisExceptionDialog().show()
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        SafeState.STATE_IS_NOT_TRIGGING -> {
//                            LogUtil.i("退出堵转状态")
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * @describe 重置按钮点击状态
     */
//    private fun resetButton() {
//        binding.groupState3.apply {
//            visibility = View.VISIBLE
//        }
//        binding.groupState2.apply {
//            visibility = View.GONE
//        }
//        binding.imageViewGoBack.apply {
//            isEnabled = false
//        }
//        binding.textViewBack.apply {
//            isEnabled = true
//        }
//        when (basicSettingViewModel?.basicConfig?.guideModeOpen) {
//            0 -> {
//                binding.groupGuideButton.visibility = View.GONE
//                binding.buttonGuide.isEnabled = false
//            }
//            1 -> {
//                binding.groupGuideButton.visibility = View.VISIBLE
//                when (basicSettingViewModel?.basicConfig?.guideModeVerifyPassword) {
//                    0 -> {
//                        binding.imageViewLockGuide.visibility = View.GONE
//                    }
//                    1 -> {
//                        binding.imageViewLockGuide.visibility = View.VISIBLE
//                    }
//                }
//                binding.buttonGuide.isEnabled = true
//            }
//        }
//        when (basicSettingViewModel?.basicConfig?.sendModeOpen) {
//            0 -> {
//                binding.groupSendButton.visibility = View.GONE
//                binding.buttonSend.isEnabled = false
//            }
//            1 -> {
//                binding.groupSendButton.visibility = View.VISIBLE
//                when (basicSettingViewModel?.basicConfig?.sendModeVerifyPassword) {
//                    0 -> {
//                        binding.imageViewLockSend.visibility = View.GONE
//                    }
//                    1 -> {
//                        binding.imageViewLockSend.visibility = View.VISIBLE
//                    }
//                }
//                binding.buttonSend.isEnabled = true
//            }
//        }
//
//
//    }

    /**
     * @describe 第二个定时器（用于循环播报语音）
     */
    private fun timer2Schedule() {
        val welcome1 = getString(R.string.welcome_i_am_xiao_di)
        val welcome2 = getString(R.string.i_am_going_to_work_if_you_need_my_serve)
        val welcome3 = getString(R.string.i_hope_to_serve_you)
        timer2!!.schedule(object : TimerTask() {
            override fun run() {
                mainScope.launch(Dispatchers.IO) {
                    if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                    SpeakHelper.speakWithoutStop(welcome1)
                    SpeakHelper.speakWithoutStop(welcome2)
                    SpeakHelper.speakWithoutStop(welcome3)
                }
            }
        }, Date(), 25000)
    }
}