package com.sendi.deliveredrobot.view.fragment

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
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentInputRoomNumberBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.CustomIndicatorTextView
import com.sendi.deliveredrobot.view.widget.CustomKeyBoardView
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.GuidePlaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class InputRoomNumberFragment : Fragment() {
    private lateinit var binding: FragmentInputRoomNumberBinding
    private val viewModelGuide: GuidePlaceViewModel? by viewModels({ requireActivity() })
    private val viewModelBasicSetting:BasicSettingViewModel? by viewModels({ requireActivity() })
    private lateinit var timer: Timer
    private lateinit var seconds: MutableLiveData<Int>
    private var inputFull = false
    private var firstInput = true
    private var fromType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input_room_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
        MainScope().launch {
            if (CommonHelper.atChargePointFloor()) {
                //如果是在充电桩的楼层
                AudioMngHelper(requireContext()).setVoice100((viewModelBasicSetting?.basicConfig?.guideVolumeLobby?: 60) / 2)
            }else{
                AudioMngHelper(requireContext()).setVoice100((viewModelBasicSetting?.basicConfig?.guideVolume ?: 40) / 2)
            }
            when (fromType) {
                InputPasswordFromType.HOME_GUIDE,
                InputPasswordFromType.GO_BACKING_GUIDE,
                InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                    SpeakHelper.speak(getString(R.string.xiao_di_serve_you_please_input_room_number))
                }
            }
        }
        binding = DataBindingUtil.bind(view)!!
        timer = Timer()
        seconds = MutableLiveData(30)
        binding.textViewCommit.apply {
            isEnabled = false
            setOnClickListener {
                isEnabled = false
                var text = binding.customIndicatorTextView.getText()
                if(text.startsWith("0")){
                    text = text.substring(1)
                }
                MainScope().launch(Dispatchers.Default) {
                    val point =
                        DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
                            .queryPoint(text)
                    if (point == null) {
                        ToastUtil.show(
                            String.format(
                                getString(R.string.room_number_mismatch),
                                text
                            )
                        )
                        isEnabled = true
                        return@launch
                    }
                    BillManager.currentBill()?.earlyFinish()
                    val bill = GuideTaskBillFactory.createBill(TaskModel(location = point))
                    when (fromType) {
                        InputPasswordFromType.HOME_GUIDE,InputPasswordFromType.GO_BACKING_GUIDE -> {
                            BillManager.addAllAtIndex(bill)
                        }
                         InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                            BillManager.addAllAtIndex(bill, 1)
                        }
                       }
                    viewModelGuide?.clearGuidePlace()
//                    if (RobotStatus.autoCruise) {
//                        TaskQueue.autoCruiseQueue.clear()
//                        TaskQueue.autoCruiseQueue.addAll(TaskQueue.queue)
//                    }
                    timer.cancel()
                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
                }
            }
        }
        binding.customIndicatorTextView.apply {
            setTextLength(4)
            isEnabled = false
            isClickable = false
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                    inputFull = true
                }

                override fun notFull() {
                    inputFull = false
                }

                override fun hasText(text: String) {
                    with(binding.textViewCommit) {
                        isEnabled = true
                        isClickable = true
                        setTextColor(Color.WHITE)
                    }
                }

                override fun empty() {
                    with(binding.textViewCommit) {
                        isEnabled = false
                        isClickable = false
                        setTextColor(ContextCompat.getColor(context, R.color.color_A0BAEF))
                    }
                }
            })
        }

        binding.customKeyBoardView.apply {
            setKeyBoardListener(object : CustomKeyBoardView.KeyBoardCallback() {
                override fun onPushText(char: Char) {
                    if (!inputFull) {
                        binding.customIndicatorTextView.addText(char)
                        if(firstInput){
                            firstInput = false
                            SpeakHelper.speak(char.toString())
                        }else{
                            SpeakHelper.speakWithoutStop(char.toString())
                        }

                    }
                }

                override fun onRemoveText() {
                    binding.customIndicatorTextView.removeText()
                }

                override fun onClearText() {
                    binding.customIndicatorTextView.clearText()
                }

            })
        }
        binding.tvRoomNumber.apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.chooseGuidePlaceFragment,
                    Bundle().apply {
                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, fromType)
                    },
                )
                timer.cancel()
            }
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
                InputPasswordFromType.GUIDING_CHANGE_POINT, InputPasswordFromType.GO_BACKING_GUIDE -> {
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
                InputPasswordFromType.GUIDING_CHANGE_POINT, InputPasswordFromType.GO_BACKING_GUIDE -> {
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
                        InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                            findNavController().popBackStack(R.id.guidingFragment, false)
                            MainScope().launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.GO_BACKING_GUIDE -> {
                            findNavController().popBackStack(R.id.goBackFragment, false)
                            MainScope().launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.HOME_GUIDE -> {
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
                MainScope().launch {
                    withContext(Dispatchers.Main){
                        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@withContext
                        seconds.value = seconds.value?.minus(1)
                    }
                }
            }
        }, Date(), 1000)
    }
}