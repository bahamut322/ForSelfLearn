package com.sendi.deliveredrobot.view.fragment

import android.content.Intent
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
import com.sendi.deliveredrobot.databinding.FragmentInputVerificationCodeBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.PhoneConfirmModel
import com.sendi.deliveredrobot.navigationtask.BillManager
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
import java.util.*

/**
 * @author heky
 * @date 2022-06-08
 * @describe 小程序任务验证码校验页面
 */
class InputVerificationCodeFragment : Fragment() {
    private lateinit var binding: FragmentInputVerificationCodeBinding
    private val basicSettingViewModel by viewModels<BasicSettingViewModel>({ requireActivity() })
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private lateinit var timer: Timer
    private lateinit var seconds:MutableLiveData<Int>
    private val mainScope = MainScope()
    private var opening = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val pageType = arguments?.getString(PAGE_TYPE)
        val binMark = arguments?.getInt(BIN_MARK)
        timer = Timer()
        seconds = MutableLiveData(basicSettingViewModel.basicConfig.sendPutObjectTime)
        timerSchedule(timer,seconds)
        binding.textViewPlaceName.apply {
            //地点
            text = when (pageType) {
                PAGE_TYPE_TAKE -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> viewModelBin1.remoteOrderModel?.to?.pointName?:""
                        viewModelBin2.binMarkBin2 -> viewModelBin2.remoteOrderModel?.to?.pointName?:""
                        else -> ""
                    }
                }
                PAGE_TYPE_PUT -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> viewModelBin1.remoteOrderModel?.from?.pointName?:""
                        viewModelBin2.binMarkBin2 -> viewModelBin2.remoteOrderModel?.from?.pointName?:""
                        else -> ""
                    }
                }
                else -> ""
            }
        }
//        binding.textViewMessage.apply {
//            //根据argument判断显示内容
//            text = when (pageType) {
//                PAGE_TYPE_TAKE -> {
//                    when (binMark) {
//                        viewModelBin1.binMarkBin1 -> resources.getString(R.string.take_your_package_in_bin_1)
//                        viewModelBin2.binMarkBin2 -> resources.getString(R.string.take_your_package_in_bin_2)
//                        else -> ""
//                    }
//                }
//                PAGE_TYPE_PUT -> {
//                    when (binMark) {
//                        viewModelBin1.binMarkBin1 -> resources.getString(R.string.put_your_package_io_bin_1)
//                        viewModelBin2.binMarkBin2 -> resources.getString(R.string.put_your_package_in_bin_2)
//                        else -> ""
//                    }
//                }
//                else -> ""
//            }
//        }
        binding.viewRobot.apply {
            background = when (binMark) {
                        viewModelBin1.binMarkBin1 -> ContextCompat.getDrawable(requireContext(),R.drawable.ic_robot_1)
                        viewModelBin2.binMarkBin2 -> ContextCompat.getDrawable(requireContext(),R.drawable.ic_robot_2)
                        else -> ContextCompat.getDrawable(requireContext(),R.drawable.ic_robot_1)
                    }
        }
        binding.textViewRobotName.apply {
            text = RobotStatus.tenancy.value?.robotName?:""
        }
        binding.textViewName.apply {
            //姓名
            text = when (pageType) {
                PAGE_TYPE_TAKE -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> desensitizationName(viewModelBin1.remoteOrderModel?.toName?:"*")
                        viewModelBin2.binMarkBin2 -> desensitizationName(viewModelBin2.remoteOrderModel?.toName?:"*")
                        else -> ""
                    }
                }
                PAGE_TYPE_PUT -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> desensitizationName(viewModelBin1.remoteOrderModel?.fromName?:"*")
                        viewModelBin2.binMarkBin2 -> desensitizationName(viewModelBin2.remoteOrderModel?.fromName?:"*")
                        else -> ""
                    }
                }
                else -> ""
            }
        }
        binding.textViewPhone.apply {
            text = when (pageType) {
                PAGE_TYPE_TAKE -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> desensitizationPhone(viewModelBin1.remoteOrderModel?.toPhone?:"*")
                        viewModelBin2.binMarkBin2 -> desensitizationPhone(viewModelBin2.remoteOrderModel?.toPhone?:"*")
                        else -> ""
                    }
                }
                PAGE_TYPE_PUT -> {
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> desensitizationPhone(viewModelBin1.remoteOrderModel?.fromPhone?:"*")
                        viewModelBin2.binMarkBin2 -> desensitizationPhone(viewModelBin2.remoteOrderModel?.fromPhone?:"*")
                        else -> ""
                    }
                }
                else -> ""
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
                            with(viewModelBin1){
                                val phone: String = when (pageType) {
                                    PAGE_TYPE_PUT -> remoteOrderModel?.fromPhone ?: ""
                                    PAGE_TYPE_TAKE -> remoteOrderModel?.toPhone ?: ""
                                    else -> "1"
                                }
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = phone).toString())
                                MyApplication.instance!!.sendBroadcast(
                                    Intent().apply {
                                        action = ACTION_NAVIGATE
                                        putExtra(NAVIGATE_ID, R.id.remoteOrderCloseDoorFragment)
                                        putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                                            putString(PAGE_TYPE, pageType)
                                            putInt(BIN_MARK, binMarkBin1)
                                        })
                                    }
                                )
                            }
                        }
                        DoorState.DOOR_TWO -> {
                            with(viewModelBin2){
                                val phone: String = when (pageType) {
                                    PAGE_TYPE_PUT -> remoteOrderModel?.fromPhone ?: ""
                                    PAGE_TYPE_TAKE -> remoteOrderModel?.toPhone ?: ""
                                    else -> "1"
                                }
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = phone).toString())
                                MyApplication.instance!!.sendBroadcast(
                                    Intent().apply {
                                        action = ACTION_NAVIGATE
                                        putExtra(NAVIGATE_ID, R.id.remoteOrderCloseDoorFragment)
                                        putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                                            putString(PAGE_TYPE, pageType)
                                            putInt(BIN_MARK, binMarkBin2)
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
                DoorState.STATE_CLOSED -> {

                }
                DoorState.STATE_OPENING -> {
                    opening = true
                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_opening_take_care_hands))
                }
                DoorState.STATE_CLOSING -> {

                }
                DoorState.STATE_OPEN_FAILED -> {
                    opening = false
                }
                DoorState.STATE_CLOSE_FAILED -> {}
                DoorState.STATE_HALF_OPEN -> {
                    opening =false
                }
            }
        }
        binding.customIndicatorTextView.apply {
            setTextLength(4)
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                    //校验是否为手机后四位，成功则跳转页面，否则进入下一任务
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> {
                            with(viewModelBin1){
                                val code:String = when (pageType) {
                                    PAGE_TYPE_PUT -> {
                                        val fromPhone = remoteOrderModel?.fromPhone?:""
                                        if(fromPhone.isNotEmpty() && fromPhone.length > 4){
                                            fromPhone.substring(fromPhone.length - 4)
                                        }else{
                                            "0000"
                                        }
                                    }
                                    PAGE_TYPE_TAKE -> {
                                        val toPhone = remoteOrderModel?.toPhone?:""
                                        if(toPhone.isNotEmpty() && toPhone.length > 4){
                                            toPhone.substring(toPhone.length - 4)
                                        }else{
                                            "0000"
                                        }
                                    }
                                    else -> "0000"
                                }
                                if (text == code) {
                                    // 校验通过
                                    clearText()
                                    val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                                    if(state.toByte() != DoorState.STATE_OPENED){
                                        ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE)
                                    }
                                }else{
                                    clearText()
                                    ToastUtil.show("验证码错误")
                                }
                            }
                        }
                        viewModelBin2.binMarkBin2 -> {
                            with(viewModelBin2){
                                val code:String = when (pageType) {
                                    PAGE_TYPE_PUT -> {
                                        val fromPhone = remoteOrderModel?.fromPhone?:""
                                        if(fromPhone.isNotEmpty() && fromPhone.length > 4){
                                        fromPhone.substring(fromPhone.length - 4)
                                        }else{
                                            "0000"
                                        }
                                    }
                                    PAGE_TYPE_TAKE -> {
                                        val toPhone = remoteOrderModel?.toPhone?:""
                                        if(toPhone.isNotEmpty() && toPhone.length > 4) {
                                            toPhone.substring(toPhone.length - 4)
                                        }else{
                                            "0000"
                                        }
                                    }
                                    else -> "0000"
                                }
                                if (text == code) {
                                    // 校验通过
                                    clearText()
                                    val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_TWO)
                                    if(state.toByte() != DoorState.STATE_OPENED){
                                        ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
                                    }
                                }else{
                                    clearText()
                                    ToastUtil.show("验证码错误")
                                }
                            }
                        }
                    }
                }

                override fun notFull() {

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
        seconds.observe(viewLifecycleOwner) {
            binding.textViewSeconds.apply {
                if (it < 1) {
                    timer.cancel()
                    when (binMark) {
                        viewModelBin1.binMarkBin1 -> {
                            with(viewModelBin1){
                                val phone:String = when (pageType) {
                                    PAGE_TYPE_PUT -> remoteOrderModel?.fromPhone?:""
                                    PAGE_TYPE_TAKE -> remoteOrderModel?.toPhone?:""
                                    else -> "1"
                                }
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = phone).toString())
                            }
                        }
                        viewModelBin2.binMarkBin2 -> {
                            with(viewModelBin2){
                                val phone:String = when (pageType) {
                                    PAGE_TYPE_PUT -> remoteOrderModel?.fromPhone?:""
                                    PAGE_TYPE_TAKE -> remoteOrderModel?.toPhone?:""
                                    else -> "1"
                                }
                                //结束拨号
                                CloudMqttService.publish(PhoneConfirmModel(number = phone).toString())
                            }
                        }
                    }
                    //超时，进入下一任务
                    BillManager.currentBill()?.executeNextTask()
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

    private fun desensitizationName(name: String): String{
        return if(name.isNotEmpty()){"姓名：${name.substring(0,1)}**"}else "姓名："
    }

    private fun desensitizationPhone(phone: String): String{
        return if(phone.isNotEmpty()) {"尾号：***${phone.substring(phone.length - 1)}"}else "尾号："
    }
}