package com.sendi.deliveredrobot.view.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import chassis_msgs.DoorState
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentPutObjectBinding
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class PutObjectFragment : Fragment() {
    private lateinit var binding: FragmentPutObjectBinding
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val viewModelBasicSetting by viewModels<BasicSettingViewModel>({ requireActivity() })
    private val mainScope = MainScope()
    private var fromType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_put_object, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
        when (arguments?.get(CHOOSE_BIN)) {
            CHOOSE_BIN1 -> Glide.with(binding.imageViewRobot).asGif()
                .load(R.raw.take_from_bin_1_opened)
                .into(binding.imageViewRobot)
            CHOOSE_BIN2 -> Glide.with(binding.imageViewRobot).asGif()
                .load(R.raw.take_from_bin_2_opened)
                .into(binding.imageViewRobot)
        }
        binding.textViewSendCommit.apply {
            isEnabled = false
            isClickable = false
            setOnClickListener {
                setButtonEnable(false)
                isClickable = false
                val location1 = when (viewModelBin1.getCurrentSelectedLocation() == null) {
                    true -> {
                        viewModelBin2.previousTaskFinished = false
                        viewModelBin2.getCurrentSelectedLocation()
                    }
                    false -> {
                        viewModelBin1.previousTaskFinished = false
                        viewModelBin1.getCurrentSelectedLocation()
                    }
                }
                val location2 = when (location1) {
                    viewModelBin1.getCurrentSelectedLocation() -> {
                        val location = viewModelBin2.getCurrentSelectedLocation()
                        if (location != null) {
                            viewModelBin2.previousTaskFinished = false
                        }
                        location
                    }
                    viewModelBin2.getCurrentSelectedLocation() -> {
                        val location = viewModelBin1.getCurrentSelectedLocation()
                        if (location != null) {
                            viewModelBin1.previousTaskFinished = false
                        }
                        location
                    }
                    else -> null
                }
                mainScope.launch {
                    val bill = SendTaskBillFactory.createBill(
                        TaskModel(
                            location = location1,
                            location2 = location2,
                        )
                    )
                    BillManager.currentBill()?.earlyFinish()
                    BillManager.addAllAtIndex(bill)
                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_STOP)
                }
            }
        }

        binding.textViewHome.apply {
            setOnClickListener {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })
            }
            visibility = when (fromType) {
                InputPasswordFromType.HOME_SEND -> {
                    View.VISIBLE
                }
                InputPasswordFromType.GO_BACKING_SEND -> {
                    View.GONE
                }
                else -> {
                    View.GONE
                }
            }
        }

        binding.imageViewHome.apply {
            setOnClickListener {
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })
            }
            visibility = when (fromType) {
                InputPasswordFromType.HOME_SEND -> {
                    View.VISIBLE
                }
                InputPasswordFromType.GO_BACKING_SEND -> {
                    View.GONE
                }
                else -> {
                    View.GONE
                }
            }
        }

        binding.textViewBack.apply {
            setOnClickListener {
                findNavController().popBackStack(R.id.goBackFragment, false)
                mainScope.launch {
                    ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                }
            }
            visibility = when (fromType) {
                InputPasswordFromType.HOME_SEND -> {
                    View.GONE
                }
                InputPasswordFromType.GO_BACKING_SEND -> {
                    View.VISIBLE
                }
                else -> {
                    View.GONE
                }
            }
        }
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            MyApplication.instance!!.sendBroadcast(
                                Intent().apply {
                                    action = ACTION_NAVIGATE
                                    putExtra(NAVIGATE_ID,R.id.chooseSendPlaceBin1Fragment)
                                    putExtra(NAVIGATE_BUNDLE,Bundle().apply {
                                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE,fromType)
                                    })
                                }
                            )
                        }
                        DoorState.DOOR_TWO -> {
                            MyApplication.instance!!.sendBroadcast(
                                Intent().apply {
                                    action = ACTION_NAVIGATE
                                    putExtra(NAVIGATE_ID,R.id.chooseSendPlaceBin2Fragment)
                                    putExtra(NAVIGATE_BUNDLE,Bundle().apply {
                                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE,fromType)
                                    })
                                }
                            )
                        }
                    }
                }
                DoorState.STATE_CLOSED -> {
                }
                DoorState.STATE_OPENING -> {
                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_opening_please_put_things))
                }
                DoorState.STATE_CLOSING -> {
                }
                DoorState.STATE_OPEN_FAILED -> {
                    setButtonEnable(true)
                }
                DoorState.STATE_CLOSE_FAILED -> {
                }
                DoorState.STATE_HALF_OPEN -> {
                    setButtonEnable(true)
                }
            }


        }

        binding.textViewBin1Open.apply {
            setOnClickListener {
                setButtonEnable(false)
                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                if(state.toByte() != DoorState.STATE_OPENED){
                    ROSHelper.controlBin(cmd = RobotCommand.CMD_OPEN, door = DoorState.DOOR_ONE)
                }
            }
        }

        binding.textViewBin2Open.apply {
            setOnClickListener {
                setButtonEnable(false)
                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_TWO)
                if(state.toByte() != DoorState.STATE_OPENED) {
                    ROSHelper.controlBin(cmd = RobotCommand.CMD_OPEN, door = DoorState.DOOR_TWO)
                }
            }
        }

        viewModelBin1.place.observe(viewLifecycleOwner) {
            with(binding.textViewSendToBin1) {
                text = if (TextUtils.isEmpty(it)) {
                    context.resources.getString(R.string.send_to_default)
                } else {
                    String.format(context.resources.getString(R.string.send_to), it)
                }
            }
            if (TextUtils.isEmpty(it) && TextUtils.isEmpty(viewModelBin2.place.value)) {
                with(binding.textViewSendCommit) {
                    isEnabled = false
                    isClickable = false
                    setTextColor(ContextCompat.getColor(context, R.color.color_A0BAEF))
                }
                when (fromType) {
                    InputPasswordFromType.HOME_SEND -> {
                        binding.imageViewHome.apply {
                            visibility = View.VISIBLE
                        }
                        binding.textViewHome.apply {
                            visibility = View.VISIBLE
                        }
                    }
                    InputPasswordFromType.GO_BACKING_SEND -> {
                        binding.textViewBack.apply {
                            visibility = View.VISIBLE
                        }
                    }
                }
            }else{
                with(binding.textViewSendCommit){
                    isEnabled = true
                    isClickable = true
                    setTextColor(Color.WHITE)
                }
                when (fromType) {
                    InputPasswordFromType.HOME_SEND -> {
                        binding.imageViewHome.apply {
                            visibility = View.GONE
                        }
                        binding.textViewHome.apply {
                            visibility = View.GONE
                        }
                    }
                    InputPasswordFromType.GO_BACKING_SEND -> {
                        binding.textViewBack.apply {
                            visibility = View.GONE
                        }
                    }
                }
            }


        }
        viewModelBin2.place.observe(viewLifecycleOwner) {
            with(binding.textViewSendToBin2) {
                text = if (TextUtils.isEmpty(it)) {
                    context.resources.getString(R.string.send_to_default)
                } else {
                    String.format(context.resources.getString(R.string.send_to), it)
                }
            }
            if (TextUtils.isEmpty(it) && TextUtils.isEmpty(viewModelBin1.place.value)) {
                with(binding.textViewSendCommit) {
                    isEnabled = false
                    isClickable = false
                    setTextColor(ContextCompat.getColor(context, R.color.color_A0BAEF))
                }
                when (fromType) {
                    InputPasswordFromType.HOME_SEND -> {
                        binding.imageViewHome.apply {
                            visibility = View.VISIBLE
                        }
                        binding.textViewHome.apply {
                            visibility = View.VISIBLE
                        }
                    }
                    InputPasswordFromType.GO_BACKING_SEND -> {
                        binding.textViewBack.apply {
                            visibility = View.VISIBLE
                        }
                    }
                }
            }else{
                with(binding.textViewSendCommit){
                    isEnabled = true
                    isClickable = true
                    setTextColor(Color.WHITE)
                }
                when (fromType) {
                    InputPasswordFromType.HOME_SEND -> {
                        binding.imageViewHome.apply {
                            visibility = View.GONE
                        }
                        binding.textViewHome.apply {
                            visibility = View.GONE
                        }
                    }
                    InputPasswordFromType.GO_BACKING_SEND -> {
                        binding.textViewBack.apply {
                            visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setButtonEnable(enable: Boolean){
        mainScope.launch(Dispatchers.Main) {
            binding.textViewBin1Open.isEnabled = enable
            binding.textViewBin2Open.isEnabled = enable
            if (enable) {
                if(!TextUtils.isEmpty(viewModelBin2.place.value) && !TextUtils.isEmpty(viewModelBin1.place.value)){
                    return@launch
                }
            }
            binding.textViewSendCommit.isEnabled = enable
        }
    }
}