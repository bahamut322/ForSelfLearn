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
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.BIN_MARK
import com.sendi.deliveredrobot.PAGE_TYPE
import com.sendi.deliveredrobot.PAGE_TYPE_PUT
import com.sendi.deliveredrobot.PAGE_TYPE_TAKE
import com.sendi.deliveredrobot.databinding.FragmentRemoteOrderCloseDoorBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.*
import java.util.*

/**
 * @author heky
 * @date 2022-06-08
 * @description 小程序下单{放物/取物}页面
 */
class RemoteOrderCloseDoorFragment : Fragment() {
    private lateinit var binding: FragmentRemoteOrderCloseDoorBinding
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val mainScope: CoroutineScope = MainScope()
    private val timer = Timer()
    private var closing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_remote_order_close_door, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val seconds = MutableLiveData(basicSettingViewModel?.basicConfig?.sendTakeObjectTime!!)
        timerSchedule(timer,seconds)
        val pageType = arguments?.getString(PAGE_TYPE)
        val binMark = arguments?.getInt(BIN_MARK)
        if (viewModelBin1.binMarkBin1 == binMark) {
            with(viewModelBin1) {
                Glide.with(binding.imageViewTakeObject).asGif().load(R.raw.take_from_bin_1_opened)
                    .into(binding.imageViewTakeObject)
                binding.textViewPackageDescription.apply {
                    text = resources.getString(R.string.bin1_opened_2)
                }
                binding.textViewPlaceName.apply {
                    // 根据argument判断显示送往的目的地，或者----
                    text = when (pageType) {
                        PAGE_TYPE_TAKE -> {
                            "----"
                        }
                        PAGE_TYPE_PUT -> {
                            "将送往: ${remoteOrderModel?.to?.floorName}F ${remoteOrderModel?.to?.pointName?:""} ${remoteOrderModel?.toName?:""}"
                        }
                        else -> ""
                    }
                }
            }
        } else if (viewModelBin2.binMarkBin2 == binMark) {
            with(viewModelBin2) {
                Glide.with(binding.imageViewTakeObject).asGif().load(R.raw.take_from_bin_2_opened)
                    .into(binding.imageViewTakeObject)
                binding.textViewPackageDescription.apply {
                    text = resources.getString(R.string.bin2_opened_2)
                }
                binding.textViewPlaceName.apply {
                    // 根据argument判断显示送往的目的地，或者----
                    text = when (pageType) {
                        PAGE_TYPE_TAKE -> {
                            "-----"
                        }
                        PAGE_TYPE_PUT -> {
                            "将送往: ${remoteOrderModel?.to?.floorName}F ${remoteOrderModel?.to?.pointName?:""} ${remoteOrderModel?.toName?:""}"
                        }
                        else -> ""
                    }
                }
            }
        }
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
                }
                DoorState.STATE_CLOSED -> {
                    when (pageType) {
                        PAGE_TYPE_TAKE -> {
                            when (door) {
                                DoorState.DOOR_ONE -> {
                                    viewModelBin1.previousRemoteOrderSendFinished = true
                                }
                                DoorState.DOOR_TWO -> {
                                    viewModelBin2.previousRemoteOrderSendFinished = true
                                }
                            }
                        }
                        PAGE_TYPE_PUT -> {
                            when (door) {
                                DoorState.DOOR_ONE -> {
                                    viewModelBin1.previousRemoteOrderPutFinished = true
                                }
                                DoorState.DOOR_TWO -> {
                                    viewModelBin2.previousRemoteOrderPutFinished = true
                                }
                            }
                        }
                    }
                    SpeakHelper.speakWithoutStop(MyApplication.instance!!.getString(R.string.i_am_your_errand))
//                    TaskQueue.executeNextTask()
                    BillManager.currentBill()?.executeNextTask()
                }
                DoorState.STATE_OPENING -> {
                }
                DoorState.STATE_CLOSING -> {
                    closing = true
                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_closing_take_care_hands))
                    setButtonEnable(false)
                }
                DoorState.STATE_OPEN_FAILED -> {
                }
                DoorState.STATE_CLOSE_FAILED -> {
                    closing = false
                    setButtonEnable(true)
                }
                DoorState.STATE_HALF_OPEN -> {
                    closing = false
                    setButtonEnable(true)
                }
            }
        }
        binding.groupTake.apply {
            when (pageType) {
                PAGE_TYPE_TAKE -> {
                    visibility = View.GONE
                }
                PAGE_TYPE_PUT -> {
                    visibility = View.VISIBLE
                }
            }
        }
        binding.view2.apply {
            when (pageType) {
                PAGE_TYPE_TAKE -> {
                    layoutParams = layoutParams.apply {
                        height = PxUtil.dp2px(requireContext(), 310f)
                    }
                }
                PAGE_TYPE_PUT -> {
                    layoutParams = layoutParams.apply {
                        height = PxUtil.dp2px(requireContext(), 410f)
                    }
                }
            }
        }
        binding.textViewBinControlCommit.apply {
            isEnabled = true
            isClickable = true
            setOnClickListener {
                setButtonEnable(false)
//                timer.cancel()
                changeTaskStatus(pageType?:"",binMark?:0)
            }
        }

        seconds.observe(viewLifecycleOwner) {
            if (it < 1) {
                timer.cancel()
                // 根据argument改变对应的小程序任务状态，看是放的还是取的，然后关门
                changeTaskStatus(pageType?:"",binMark?:0)
            }
            binding.textViewTips.apply {
                // 根据argument判断提示的内容
                val str = when (pageType) {
                    PAGE_TYPE_PUT -> resources.getString(R.string.please_put_your_object_in_seconds_opened)
                    PAGE_TYPE_TAKE -> resources.getString(R.string.please_take_your_object_in_seconds_opened)
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
                mainScope.launch {
                    withContext(Dispatchers.Main){
                        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@withContext
                        if(closing) return@withContext
                        seconds.value = seconds.value?.minus(1)
                    }
                }
            }
        }, Date(), 1000)
    }

    private fun changeTaskStatus(pageType: String, binMark:Int){
        when (pageType) {
            PAGE_TYPE_TAKE -> {
                when (binMark) {
                    viewModelBin1.binMarkBin1 -> {
                        val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                        if(state.toByte() != DoorState.STATE_CLOSED){
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                        }
                    }
                    viewModelBin2.binMarkBin2 -> {
                        val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_TWO)
                        if(state.toByte() != DoorState.STATE_CLOSED){
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                        }
                    }
                }
            }
            PAGE_TYPE_PUT -> {
                when (binMark) {
                    viewModelBin1.binMarkBin1 -> {
                        val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                        if(state.toByte() != DoorState.STATE_CLOSED){
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                        }
                    }
                    viewModelBin2.binMarkBin2 -> {
                        val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_TWO)
                        if(state.toByte() != DoorState.STATE_CLOSED){
                            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                        }
                    }
                }
            }
        }
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