package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGuidingBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.util.*


class ReadyingFragment : Fragment() {
    private lateinit var binding: FragmentGuidingBinding
    private val viewModelBasicSetting by viewModels<BasicSettingViewModel>({ requireActivity() })
    private var timer: Timer? = null
    private var timer2:Timer? = null
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var mainScope: CoroutineScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gifGuiding = Glide.with(this).asGif().load(R.drawable.img_goback)
        gifStopGuide = Glide.with(this).asGif().load(R.drawable.img_goback)
    }
    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guiding, container, false)
    }

    private lateinit var gifGuiding:RequestBuilder<GifDrawable>

    private lateinit var gifStopGuide: RequestBuilder<GifDrawable>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        mainScope.launch {
            val bill = BillManager.currentBill()
//            if (bill is GuideTaskBill) {
//                // 设置标志位为true，表示已经进入过该方法
//                Universal.speakInt++
//                var pointName = bill?.endTarget()
//                pointName = pointName?.toList()?.joinToString(" ")
//                binding.RoomName.text = pointName
//                if (Universal.speakInt %2 != 0){
//                    BaiduTTSHelper.getInstance().speak(String.format(getString(R.string.hello_we_are_going_to_please_follow_me_1),pointName))
//                }
//            }
            if (bill is GoToReadyPointBill) {
                Universal.speakInt++
                // 设置标志位为true，表示已经进入过该方法
                var pointName = bill?.endTarget()
                pointName = pointName?.toList()?.joinToString(" ")
                binding.RoomName.text = pointName
                if (Universal.speakInt %2 != 0){
                    BaiduTTSHelper.getInstance().speak(String.format(getString(R.string.hello_we_are_going_to_please_follow_me_1),pointName))
                }
            }
        }

        seconds = MutableLiveData(viewModelBasicSetting.basicConfig.guideWalkPauseTime)
//        binding.bottomAlarmTextViewGuiding.apply {
//            bottomAlarmText1 = getString(R.string.going_to)
//            bottomAlarmText2 = BillManager.currentBill()?.currentTask()?.taskModel?.location?.pointName ?: "000"
//        }
//        gifGuiding.into(binding.imgGuiding)
//        gifStopGuide.into(binding.imgStopGuide)
//        binding.motionLayoutGuiding.apply {
//            setTransitionListener(object : MotionLayout.TransitionListener {
//                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
//                }
//
//                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
//                }
//
//                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
//                    when (p1) {
//                        R.id.state1 -> {
//                            timer?.cancel()
//                            LogUtil.i("current:state1")
//                        }
//                        R.id.state2 -> {
//                            binding.buttonStartGuide.isEnabled = false
//                            binding.buttonStopGuide.isEnabled = false
//                            binding.buttonChangeDestination.isEnabled = false
//                            //TODO 判断是state3->state2，此处用了取巧的方式，后期需修改为合理的方式
////                            if (p0.toString().startsWith("state2->state3")) {
////                                //继续
////                                timer?.cancel()
//////                                if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_UNPRESSED){
////                                mainScope.launch {
////                                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
////                                }
//////                                }
////                            }
//                            mainScope.launch {
//                                withContext(Dispatchers.Default){
//                                    virtualTaskExecute(5,"Guide语音前")
//                                    if (CommonHelper.atChargePointFloor()) {
//                                        //如果是在充电桩的楼层
//                                        AudioMngHelper(requireContext()).setVoice100(viewModelBasicSetting.basicConfig.guideVolumeLobby / 2)
//                                        timer2?.cancel()
//                                        timer2?.purge()
//                                        timer2 = Timer()
//                                        timer2Schedule()
//                                    }else{
//                                        AudioMngHelper(requireContext()).setVoice100((viewModelBasicSetting.basicConfig.guideVolume?: 40) / 2)
//                                    }
//                                }
//                            }
////                            binding.imgGuiding.isEnabled = true
//                            seconds.value = viewModelBasicSetting.basicConfig.guideWalkPauseTime
//                            LogUtil.i("current:state2")
//                        }
////                        R.id.state3 -> {
////                            resetButton()
////                            timer?.cancel()
////                            timer2?.cancel()
////                            timer2?.purge()
////                            if (RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED) {
////                                SpeakHelper.speak(getString(R.string.what_is_up))
////                                //暂停
////                                timer = Timer()
////                                timer?.schedule(object : TimerTask() {
////                                    override fun run() {
////                                        mainScope.launch {
////                                            withContext(Dispatchers.Main){
////                                                seconds.value = seconds.value?.minus(1)
////                                            }
////                                        }
////                                    }
////                                }, Date(), 1000)
////                            }
////                            LogUtil.i("current:state3")
////                        }
//                    }
//
//                }
//
//                override fun onTransitionTrigger(
//                    p0: MotionLayout?,
//                    p1: Int,
//                    p2: Boolean,
//                    p3: Float
//                ) {
//                }
//            })
//            transitionToState(R.id.state2)
//        }
//        binding.buttonStartGuide.apply {
//            isClickable = true
//            setOnClickListener {
//                if (seconds.value!! > 0) {
//                    isEnabled = false
//                    mainScope.launch {
//                        if(ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)){
//                            timer?.cancel()
//                            binding.motionLayoutGuiding.transitionToState(R.id.state2)
//                        }else{
//                            isEnabled = true
//                            ToastUtil.show("继续失败，请重试")
//                        }
//                    }
//                }
//            }
//        }

//        binding.buttonStopGuide.apply {
//            isClickable = true
//            setOnClickListener {
//                if (seconds.value!! > 0) {
//                    isEnabled = false
//                    timer?.cancel()
//                    DialogHelper.loadingDialog.show()
//                    mainScope.launch {
//                        BillManager.currentBill()?.earlyFinish()
//                        ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
//                    }
//                }
//            }
//        }
//        binding.buttonChangeDestination.apply {
//            isClickable = true
//            setOnClickListener {
//                if (seconds.value!! > 0) {
//                    isEnabled = false
//                    timer?.cancel()
//                    when (viewModelBasicSetting.basicConfig.guideModeVerifyPassword) {
//                        0 -> {
//                            findNavController().navigate(
//                                R.id.inputRoomNumberFragment,
//                                Bundle().apply {
//                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GUIDING_CHANGE_POINT)
//                                }
//                            )
//                        }
////                        1 -> {
////                            findNavController().navigate(
////                                R.id.inputPasswordFragment,
////                                Bundle().apply {
////                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.GUIDING_CHANGE_POINT)
////                                }
////                            )
////                        }
//                    }
//                }
//            }
//
//        }
//        binding.imgGuiding.apply {
//            isEnabled = false
//            setOnClickListener {
//                RobotStatus.stopButtonPressed.value = RobotCommand.STOP_BUTTON_DEFAULT
//                isEnabled = false
//                mainScope.launch {
////                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_PAUSE)
////                    TaskQueues.queue.add(0, PauseGuideTask())
////                    TaskQueues.executeNextTask()
//                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)) {
//                        binding.motionLayoutGuiding.transitionToState(R.id.state3)
//                    }else{
//                        ToastUtil.show("暂停失败，请重试")
//                        isEnabled = true
//                    }
//                }
//            }
//        }
        seconds.observe(viewLifecycleOwner) {
            if (it < 1) {
                timer?.cancel()
                mainScope.launch {
                    if(ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)){
                        timer?.cancel()
//                        binding.motionLayoutGuiding.transitionToState(R.id.state2)
                    }else{
                        ToastUtil.show("继续失败，请重试")
                    }
                }
            }
//            binding.textTime.text = CommonHelper.getTimeSpan(it, 1.39f)
        }

    }

    /**
     * @describe 重置按钮可否点击状态
     */
    private fun resetButton() {
//        binding.buttonChangeDestination.isEnabled = true
//        binding.buttonStartGuide.isEnabled = true
//        binding.buttonStopGuide.isEnabled = true
    }

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
                    if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED)return@launch
                    SpeakHelper.speakWithoutStop(welcome1)
//                    delay(4000)
                    SpeakHelper.speakWithoutStop(welcome2)
//                    delay(6000)
                    SpeakHelper.speakWithoutStop(welcome3)
                }
            }
        }, Date(), 25000)
    }
}