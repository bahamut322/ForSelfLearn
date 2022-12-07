package com.sendi.deliveredrobot.view.fragment

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
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.BIN_MARK
import com.sendi.deliveredrobot.databinding.FragmentInputTakeObjectCodeBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.PhoneConfirmModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.DoubleSameSendTaskBillOne
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.CustomIndicatorTextView
import com.sendi.deliveredrobot.view.widget.CustomKeyBoardView
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class InputTakeObjectCodeFragment : Fragment() {
    companion object {
        const val BIN_NOT_OPEN = 0x11
        const val BIN_OPENED = 0x22
        const val BIN_CLOSED = 0x33
    }

    private var binStatus = BIN_NOT_OPEN
    lateinit var binding: FragmentInputTakeObjectCodeBinding
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val mainScope = MainScope()
    private var controllingDoor = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_take_object_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val binMark = arguments?.getInt(BIN_MARK)
        val timer = Timer()
        val seconds = MutableLiveData(basicSettingViewModel?.basicConfig?.sendWaitTakeObjectTime!!)
        var name = ""
        if (viewModelBin1.binMarkBin1 == binMark) {
            with(viewModelBin1) {
                name = getCurrentSelectedName()
            }
        } else if (viewModelBin2.binMarkBin2 == binMark) {
            with(viewModelBin2) {
                name = getCurrentSelectedName()
            }
        }
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
                    binStatus = BIN_OPENED
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            //结束拨号
                            viewModelBin1.previousTaskFinished = true
                            CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin1.place.value?:"").toString())
                        }
                        DoorState.DOOR_TWO -> {
                            viewModelBin2.previousTaskFinished = true
                            CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin2.place.value?:"").toString())
                        }
                    }
                    mainScope.launch(Dispatchers.Main) {
                        binding.textViewCommit.apply {
                            text = resources.getString(R.string.close_bin)
                        }
                        seconds.value = basicSettingViewModel?.basicConfig?.sendTakeObjectTime
                    }
                    controllingDoor = false
                    setButtonEnable(true)
                }
                DoorState.STATE_CLOSED -> {
                    binStatus = BIN_CLOSED
//                    if (RobotStatus.twoSamePlace) {
//                        RobotStatus.twoSamePlace = false
//                    } else {
//                        SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.i_continue_work))
//                    }
                    if(BillManager.currentBill() !is DoubleSameSendTaskBillOne){
                        SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.i_continue_work))
                    }
                    timer.cancel()
//                    TaskQueue.executeNextTask()
                    BillManager.currentBill()?.executeNextTask()
                }
                DoorState.STATE_OPENING -> {
                    controllingDoor = true
                }
                DoorState.STATE_CLOSING -> {
                    controllingDoor = true
                }
                DoorState.STATE_OPEN_FAILED -> {
                    setButtonEnable(true)
                    controllingDoor = false
                }
                DoorState.STATE_CLOSE_FAILED -> {
                    setButtonEnable(true)
                    controllingDoor = false
                }
                DoorState.STATE_HALF_OPEN -> {
                    setButtonEnable(true)
                    controllingDoor = false
                }
            }
        }
        binding.customIndicatorTextView.apply {
            setTextLength(length = name.length)
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
        binding.textViewMessage.apply {
            val bin = when (binMark) {
                viewModelBin1.binMarkBin1 -> getString(R.string.bin_1)
                viewModelBin2.binMarkBin2 -> getString(R.string.bin_2)
                else -> ""
            }
            text = CommonHelper.getTipsSpan(
                0,
                String.format(getString(R.string.master_name),name),
                String.format(getString(R.string.master_your_object_is_in),name,bin),
                ContextCompat.getColor(context, R.color.color_FF8282)
            )
        }
        binding.textViewCommit.apply {
            isClickable = true
            setOnClickListener {
                setButtonEnable(false)
                when (binStatus) {
                    BIN_NOT_OPEN -> {
                        if (binding.customIndicatorTextView.getText() != name) {
                            ToastUtil.show(getString(R.string.take_object_code_error))
                            setButtonEnable(true)
                            return@setOnClickListener
                        }
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE)
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
                        }
                    }
                    BIN_OPENED -> {
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                        }
                    }
                }
            }
        }
        timerSchedule(timer, seconds)
        seconds.observe(viewLifecycleOwner) {
            if (it < 1) {
                timer.cancel()
                when (binStatus) {
                    BIN_NOT_OPEN -> {
                        // 超时则直接下一步
                        mainScope.launch {
//                            TaskQueue.executeNextTask()
                            BillManager.currentBill()?.executeNextTask()
                        }
                        return@observe
                    }
                    BIN_OPENED -> {
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                        }
                        return@observe
                    }
                }
            }
            binding.textViewSeconds.apply {
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
                mainScope.launch {
                    withContext(Dispatchers.Main){
                        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@withContext
                        if(controllingDoor) return@withContext
                        seconds.value = seconds.value?.minus(1)
                    }
                }
            }
        }, Date(), 1000)
    }

    private fun setButtonEnable(enable: Boolean){
        mainScope.launch(Dispatchers.Main) {
            binding.textViewCommit.apply {
                isEnabled = enable
                isClickable = enable
            }
        }
    }
}