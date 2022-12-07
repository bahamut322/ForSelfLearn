package com.sendi.deliveredrobot.view.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.constants.*
import com.sendi.deliveredrobot.databinding.FragmentInputPasswordBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.CustomIndicatorTextView
import com.sendi.deliveredrobot.view.widget.CustomKeyBoardView
import com.sendi.deliveredrobot.view.widget.SendFailDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class InputPasswordFragment : Fragment() {
    private lateinit var binding: FragmentInputPasswordBinding
    private lateinit var timer: Timer
    private lateinit var seconds: MutableLiveData<Int>
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val sendPlaceBin1ViewModel: SendPlaceBin1ViewModel? by viewModels({ requireActivity() })
    private val sendPlaceBin2ViewModel: SendPlaceBin2ViewModel? by viewModels({ requireActivity() })
    private var sendFailDialog: Dialog? = null
    private val mainScope = MainScope()
    private var opening = false
    private var fromType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input_password, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
        when (fromType) {
            InputPasswordFromType.HOME_GUIDE, InputPasswordFromType.GO_BACKING_GUIDE, InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                mainScope.launch {
                    if (CommonHelper.atChargePointFloor()) {
                        //如果是在充电桩的楼层
                        AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.guideVolumeLobby?:60) / 2)
                    }else{
                        AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.guideVolume?: 40) / 2)
                    }
                    SpeakHelper.speak(getString(R.string.welcome_to_use_guide_please_input_password))
                }
            }
            InputPasswordFromType.HOME_SEND, InputPasswordFromType.GO_BACKING_SEND -> {
                mainScope.launch {
                    if (CommonHelper.atChargePointFloor()) {
                        //如果是在充电桩的楼层
                        AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolumeLobby?:60) / 2)
                    }else{
                        AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolume?: 40) / 2)
                    }
                    SpeakHelper.speak(getString(R.string.welcome_to_use_delivery_please_input_password))
                }
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
////        if (arguments?.get(NAVIGATE_TO) == R.id.putObjectFragment) {
////            mainScope.launch {
////                if (CommonHelper.atChargePointFloor()) {
////                    //如果是在充电桩的楼层
////                    AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolumeLobby?:60) / 2)
////                }else{
////                    AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolume?: 40) / 2)
////                }
////                SpeakHelper.speak(getString(R.string.welcome_to_use_delivery_please_input_password))
////            }
////        }
//        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
//        when (fromType) {
//             InputPasswordFromType.HOME_GUIDE, InputPasswordFromType.GO_BACKING_GUIDE, InputPasswordFromType.GUIDING_CHANGE_POINT -> {
//                 mainScope.launch {
//                     if (CommonHelper.atChargePointFloor()) {
//                         //如果是在充电桩的楼层
//                         AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.guideVolumeLobby?:60) / 2)
//                     }else{
//                         AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.guideVolume?: 40) / 2)
//                     }
//                     SpeakHelper.speak(getString(R.string.welcome_to_use_guide_please_input_password))
//                 }
//             }
//             InputPasswordFromType.HOME_SEND, InputPasswordFromType.GO_BACKING_SEND -> {
//                 mainScope.launch {
//                     if (CommonHelper.atChargePointFloor()) {
//                         //如果是在充电桩的楼层
//                         AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolumeLobby?:60) / 2)
//                     }else{
//                         AudioMngHelper(requireContext()).setVoice100((basicSettingViewModel?.basicConfig?.sendVolume?: 40) / 2)
//                     }
//                     SpeakHelper.speak(getString(R.string.welcome_to_use_delivery_please_input_password))
//                 }
//             }
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        timer = Timer()
        seconds = MutableLiveData(60)
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
//                    mainScope.launch(Dispatchers.Main) {
//                        when (RobotStatus.sendFailType) {
//                            SEND_FAIL_TYPE_ONLY_ONE -> {
//                                sendPlaceBin1ViewModel?.previousTaskFinished = true
//                            }
//                            SEND_FAIL_TYPE_ONE_TWO_BOTH -> {
//                                if(door == DoorState.DOOR_ONE){
//                                    sendPlaceBin1ViewModel?.previousTaskFinished = true
//                                    ROSHelper.controlBin(
//                                        RobotCommand.CMD_OPEN,
//                                        DoorState.DOOR_TWO
//                                    )
//                                    return@launch
//                                }else if(door == DoorState.DOOR_TWO){
//                                    sendPlaceBin2ViewModel?.previousTaskFinished = true
//                                }
//                            }
//                            SEND_FAIL_TYPE_ONE_TWO_ONE -> {
//                                sendPlaceBin1ViewModel?.previousTaskFinished = true
//                            }
//                            SEND_FAIL_TYPE_ONLY_TWO -> {
//                                sendPlaceBin2ViewModel?.previousTaskFinished = true
//                            }
//                        }
//                        MyApplication.instance!!.sendBroadcast(Intent().apply {
//                            action = ACTION_NAVIGATE
//                            putExtra(NAVIGATE_ID, R.id.chooseRelocatePlaceFragment)
//                        })
//                    }
                }
                DoorState.STATE_CLOSED -> {
                }
                DoorState.STATE_OPENING -> {
                    opening = true
                    setButtonEnable(false)
                }
                DoorState.STATE_CLOSING -> {

                }
                DoorState.STATE_OPEN_FAILED -> {
                    opening = false
                    setButtonEnable(true)
                }
                DoorState.STATE_CLOSE_FAILED -> {}
                DoorState.STATE_HALF_OPEN -> {
                    opening = false
                    setButtonEnable(true)
                }
            }


        }
        binding.textViewCommit.apply {
            isEnabled = false
            isClickable = false
            setOnClickListener {
                if (binding.customIndicatorTextView.getText() != basicSettingViewModel?.basicConfig?.verifyPassword) {
                    ToastUtil.show(getString(R.string.password_mismatch))
                    return@setOnClickListener
                }
                when (fromType) {
//                    InputPasswordFromType.HOME_SETTING -> {
//                        findNavController().navigate(R.id.action_inputPasswordFragment_to_settingHomeFragment)
//                    }

//                    InputPasswordFromType.NAVIGATE_INTERRUPT_GUIDE -> {}
                    InputPasswordFromType.HOME_GUIDE, InputPasswordFromType.GO_BACKING_GUIDE, InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                        MyApplication.instance!!.sendBroadcast(Intent().apply {
                            action = ACTION_NAVIGATE
                            putExtra(NAVIGATE_ID, R.id.inputRoomNumberFragment)
                            putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                                putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, fromType)
                            })
                        })
                    }
                    InputPasswordFromType.HOME_SEND, InputPasswordFromType.GO_BACKING_SEND -> {
                        MyApplication.instance!!.sendBroadcast(Intent().apply {
                            action = ACTION_NAVIGATE
                            putExtra(NAVIGATE_ID, R.id.putObjectFragment)
                            putExtra(NAVIGATE_BUNDLE,Bundle().apply {
                                putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, fromType)
                            })
                        })
                    }
                }
            }
        }
        binding.customIndicatorTextView.apply {
            setTextLength(basicSettingViewModel?.basicConfig?.verifyPassword?.length?:5)
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                    with(binding.textViewCommit) {
                        isEnabled = true
                        isClickable = true
                        setTextColor(Color.WHITE)
                    }
                }

                override fun notFull() {
                    with(binding.textViewCommit) {
                        isEnabled = false
                        isClickable = true
                        setTextColor(ContextCompat.getColor(context, R.color.color_A0BAEF))
                    }
                }

                override fun hasText(text: String) {
                }

                override fun empty() {
                }
            })
        }
        binding.customKeyBoardView.apply {
            setKeyBoardListener(object : CustomKeyBoardView.KeyBoardCallback() {
                override fun onPushText(char: Char) {
                    binding.customIndicatorTextView.addText(char)
                }

                override fun onRemoveText() {
                    binding.customIndicatorTextView.removeText()
                }

                override fun onClearText() {
                    binding.customIndicatorTextView.clearText()
                }
            })
        }

        binding.textViewHome.apply {
            setOnClickListener {
                timer.cancel()
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })

            }
            visibility = when (fromType) {
                InputPasswordFromType.GUIDING_CHANGE_POINT,
                InputPasswordFromType.GO_BACKING_GUIDE,
                InputPasswordFromType.GO_BACKING_SEND-> {
                    View.GONE
                }
                else -> {
                    View.VISIBLE
                }
            }
        }

        binding.imageViewHome.apply {
            setOnClickListener {
                timer.cancel()
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })
            }
            visibility = when (fromType) {
                InputPasswordFromType.GUIDING_CHANGE_POINT,
                InputPasswordFromType.GO_BACKING_GUIDE,
                InputPasswordFromType.GO_BACKING_SEND-> {
                    View.GONE
                }
                else -> {
                    View.VISIBLE
                }
            }
        }
        timerSchedule(timer, seconds)
        seconds.observe(viewLifecycleOwner) {
            binding.textViewSeconds.apply {
                if (it < 1) {
                    timer.cancel()
                    when (fromType) {
                        InputPasswordFromType.HOME_GUIDE,
                        InputPasswordFromType.HOME_SEND -> {
                            //从首页或创建送物任务中进来
                            MyApplication.instance?.sendBroadcast(Intent().apply {
                                action = ACTION_NAVIGATE
                                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                            })
                        }
                        InputPasswordFromType.GO_BACKING_GUIDE,
                        InputPasswordFromType.GO_BACKING_SEND -> {
                            //从返回中进来
                            findNavController().popBackStack(R.id.goBackFragment, false)
                            mainScope.launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                            findNavController().popBackStack(R.id.guidingFragment, false)
                            mainScope.launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.HOME_SETTING -> {
                            MyApplication.instance?.sendBroadcast(Intent().apply {
                                action = ACTION_NAVIGATE
                                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                            })
                        }
                    }
                }
                text = CommonHelper.getTimeSpan(it, 1.33f)
            }
        }
    }

    private fun timerSchedule(
        timer: Timer,
        seconds: MutableLiveData<Int>
    ) {
        timer.schedule(object : TimerTask() {
            override fun run() {
                mainScope.launch(Dispatchers.Main) {
                    if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                    if (opening) return@launch
                    seconds.value = seconds.value?.minus(1)
                }
            }
        }, Date(), 1000)
    }

    private fun setButtonEnable(enable: Boolean){
        mainScope.launch(Dispatchers.Main) {
            binding.textViewCommit.isEnabled = enable
        }
    }
}