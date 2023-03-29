package com.sendi.deliveredrobot.view.fragment

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
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.BIN_MARK
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentTakeObjectBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.PhoneConfirmModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.DoubleSameSendTaskBillOne
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class TakeObjectFragment : Fragment() {
    companion object {
        const val BIN_NOT_OPEN = 0x11
        const val BIN_OPENED = 0x22
        const val BIN_CLOSED = 0x33
    }

    private lateinit var binding: FragmentTakeObjectBinding
    private var binStatus = BIN_NOT_OPEN
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val mainScope:CoroutineScope = MainScope()
    private var controllingDoor = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_object, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val timer = Timer()
        val seconds = MutableLiveData(basicSettingViewModel?.basicConfig?.sendWaitTakeObjectTime!!)
        val binMark = arguments?.getInt(BIN_MARK)
        if (viewModelBin1.binMarkBin1 == binMark) {
            with(viewModelBin1) {
                Glide.with(binding.imageViewTakeObject).asGif().load(R.raw.take_from_bin_1_not_open)
                    .into(binding.imageViewTakeObject)
                binding.textViewPackageDescription.apply {
                    text = resources.getString(R.string.your_package_is_in_bin_1)
                }
                binding.textViewPlaceName.apply {
                    text = getCurrentSelectedName()
                }
            }

        } else if (viewModelBin2.binMarkBin2 == binMark) {
            with(viewModelBin2) {
                Glide.with(binding.imageViewTakeObject).asGif().load(R.raw.take_from_bin_2_not_open)
                    .into(binding.imageViewTakeObject)
                binding.textViewPackageDescription.apply {
                    text = resources.getString(R.string.your_package_is_in_bin_2)
                }
                binding.textViewPlaceName.apply {
                    text = getCurrentSelectedName()
                }
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
                            mainScope.launch(Dispatchers.Main) {
                                Glide.with(binding.imageViewTakeObject).asGif()
                                    .load(R.raw.take_from_bin_1_opened)
                                    .into(binding.imageViewTakeObject)
                                binding.textViewPackageDescription.apply {
                                    text = resources.getString(R.string.bin1_opened_2)
                                }
                            }

                            //结束拨号
                            viewModelBin1.previousTaskFinished = true
                            CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin1.place.value?:"").toString())
                        }
                        DoorState.DOOR_TWO -> {
                            mainScope.launch(Dispatchers.Main) {
                                Glide.with(binding.imageViewTakeObject).asGif()
                                    .load(R.raw.take_from_bin_2_opened)
                                    .into(binding.imageViewTakeObject)
                                binding.textViewPackageDescription.apply {
                                    text = resources.getString(R.string.bin2_opened_2)
                                }
                            }
                            viewModelBin2.previousTaskFinished = true
                            CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin2.place.value?:"").toString())
                        }
                    }
                    binding.textViewBinControlCommit.apply {
                        text = resources.getString(R.string.close_bin)
                    }
                    mainScope.launch(Dispatchers.Main) {
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
//                    TaskQueues.executeNextTask()
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
                    mainScope.launch(Dispatchers.Main) {
                        seconds.value = basicSettingViewModel?.basicConfig?.sendTakeObjectTime
                    }
                    controllingDoor = false
                }
                DoorState.STATE_HALF_OPEN -> {
                    setButtonEnable(true)
                    controllingDoor = false
                }
            }
        }
        binding.textViewBinControlCommit.apply {
            setButtonEnable(true)
            setOnClickListener {
                setButtonEnable(false)
                when (binStatus) {
                    BIN_NOT_OPEN -> {
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            val state = ROSHelper.controlBin(
                                cmd = RobotCommand.CMD_CHECK,
                                door = DoorState.DOOR_ONE
                            )
                            if (state.toByte() != DoorState.STATE_OPENED) {
                                ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE)
                            }
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            val state = ROSHelper.controlBin(
                                cmd = RobotCommand.CMD_CHECK,
                                door = DoorState.DOOR_TWO
                            )
                            if (state.toByte() != DoorState.STATE_OPENED) {
                                ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
                            }
                        }
                    }
                    BIN_OPENED -> {
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            val state = ROSHelper.controlBin(
                                cmd = RobotCommand.CMD_CHECK,
                                door = DoorState.DOOR_ONE
                            )
                            if (state.toByte() != DoorState.STATE_CLOSED) {
                                timer.cancel()
                                ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                            }
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            val state = ROSHelper.controlBin(
                                cmd = RobotCommand.CMD_CHECK,
                                door = DoorState.DOOR_TWO
                            )
                            if (state.toByte() != DoorState.STATE_CLOSED) {
                                timer.cancel()
                                ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                            }
                        }
                    }
                }
            }
        }

        timerSchedule(timer, seconds)
        seconds.observe(viewLifecycleOwner) {
            if (it < 1) {
//                timer.cancel()
                setButtonEnable(false)
                when (binStatus) {
                    BIN_NOT_OPEN -> {
                        if (viewModelBin1.binMarkBin1 == binMark) {
//                            mainScope.launch {
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin1.place.value?:"").toString())
//                                TaskQueues.executeNextTask()
                            BillManager.currentBill()?.executeNextTask()
//                            }
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
//                            mainScope.launch {
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = viewModelBin2.place.value?:"").toString())
//                                TaskQueues.executeNextTask()
                            BillManager.currentBill()?.executeNextTask()
//                            }
                        }
                        // 超时则直接下一步
                        timer.cancel()
                        return@observe
                    }
                    BIN_OPENED -> {
//                        RobotStatus.takingObject = true
                        if (viewModelBin1.binMarkBin1 == binMark) {
                            timer.cancel()
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                        } else if (viewModelBin2.binMarkBin2 == binMark) {
                            timer.cancel()
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                        }
                        return@observe
                    }
                }
            }
            binding.textViewTips.apply {
                val str = when (binStatus) {
                    BIN_NOT_OPEN -> resources.getString(R.string.please_take_your_object_in_seconds)
                    BIN_OPENED -> resources.getString(R.string.please_take_your_object_in_seconds_opened)
                    else -> resources.getString(R.string.please_take_your_object_in_seconds_opened)
                }
                text = CommonHelper.getTipsSpan(
                    2,
                    "${it}s",
                    String.format(
                        str,
                        it
                    ),
                    ContextCompat.getColor(context, R.color.color_4D6FBE)
                )
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
                        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                        if(controllingDoor) return@launch
                        seconds.value = seconds.value?.minus(1)
                }
            }
        }, Date(), 1000)
    }

    private fun setButtonEnable(enable: Boolean){
        mainScope.launch(Dispatchers.Main) {
            binding.textViewBinControlCommit.apply {
                isEnabled = enable
                isClickable = enable
            }
        }
    }
}