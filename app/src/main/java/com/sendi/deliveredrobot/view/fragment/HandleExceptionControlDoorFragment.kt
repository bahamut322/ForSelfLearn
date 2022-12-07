package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import chassis_msgs.DoorState
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentHandleExceptionControlDoorBinding
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.DoubleSameSendTaskBillOne
import com.sendi.deliveredrobot.navigationtask.DoubleSameSendTaskBillTwo
import com.sendi.deliveredrobot.navigationtask.RobotStatus
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
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-08-01
 * @description 通用处理异常-紧急开仓
 */
@SuppressLint("SetTextI18n")
class HandleExceptionControlDoorFragment : Fragment() {
    companion object{
        var controling = false
    }
    private lateinit var openBin:String
    private lateinit var closeBin:String
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val viewModelBin1: SendPlaceBin1ViewModel by viewModels({ requireActivity() })
    private val viewModelBin2: SendPlaceBin2ViewModel by viewModels({ requireActivity() })
    private val mainScope = MainScope()
    private var doorOpenClicked1 by Delegates.observable(false){
        _,_,newValue ->
        when (newValue) {
            true -> {
                //已打开
                timerSchedule(Timer(), DoorState.DOOR_ONE)
            }
            false -> {
                //已关闭
                if(!viewModelBin1.previousTaskFinished || !viewModelBin1.previousRemoteOrderSendFinished){
                    binding.textViewBinControlCommit1.text = openBin
                }else{
                    binding.textViewBinControlCommit1.text = "已完成操作"
                }
            }
        }
    }
    private var doorOpenClicked2 by Delegates.observable(false){
            _,_,newValue ->
        when (newValue) {
            true -> {
                //已打开
                timerSchedule(Timer(), DoorState.DOOR_TWO)
            }
            false -> {
                //已关闭
                if(!viewModelBin2.previousTaskFinished || !viewModelBin2.previousRemoteOrderSendFinished){
                    binding.textViewBinControlCommit2.text = openBin
                }else{
                    binding.textViewBinControlCommit2.text = "已完成操作"
                }

            }
        }
    }
    private var second1 by Delegates.observable(180){
        _,_,newValue ->
        if(newValue < 1){
            controling = true
            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
        }
        binding.textViewBinControlCommit1.text = "${closeBin}(${newValue}s)"
    }
    private var second2 by Delegates.observable(180){
            _,_,newValue ->
        if(newValue < 1){
            controling = true
            ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
        }
        binding.textViewBinControlCommit2.text = "${closeBin}(${newValue}s)"
    }
    private var verifiedPassword by Delegates.observable(false){
        _, _, newValue ->
        when (newValue) {
            true -> {
                // 已验证
                binding.groupInputPassword.visibility = View.GONE
                judgeControlWhichBin()
            }
            false -> {
                // 未验证
                binding.groupInputPassword.visibility = View.VISIBLE
            }
        }
    }
    private lateinit var binding: FragmentHandleExceptionControlDoorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openBin = resources.getString(R.string.open_bin)
        closeBin = resources.getString(R.string.close_bin)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_handle_exception_control_door, container, false)
    }

    override fun onStart() {
        super.onStart()
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
                    controling = false
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            doorOpenClicked1 = true
                            viewModelBin1.previousTaskFinished = true
                            viewModelBin1.previousRemoteOrderSendFinished = true
                            viewModelBin1.resetBill()
                            viewModelBin1.clearSelected()
                            if(!viewModelBin2.previousTaskFinished || !viewModelBin2.previousRemoteOrderSendFinished || doorOpenClicked2){
//                                if(RobotStatus.twoSamePlace){
//                                    RobotStatus.twoSamePlace = false
//                                    binding.textViewBinControlCommit2.callOnClick()
//                                }else{
//                                    // 如果2仓还需要紧急开仓
//                                    setButtonEnable(true)
//                                }
                                when (BillManager.currentBill()) {
                                    is DoubleSameSendTaskBillOne, is DoubleSameSendTaskBillTwo -> {
                                        binding.textViewBinControlCommit2.callOnClick()
                                    }
                                    else -> {
                                        // 如果2仓还需要紧急开仓
                                        setButtonEnable(true)
                                    }
                                }
                            }else{
                                setButtonEnable(true, DoorState.DOOR_ONE)
                            }
                        }
                        DoorState.DOOR_TWO -> {
                            doorOpenClicked2 = true
                            viewModelBin2.previousTaskFinished = true
                            viewModelBin2.previousRemoteOrderSendFinished = true
                            viewModelBin2.resetBill()
                            viewModelBin2.clearSelected()
                            if(!viewModelBin1.previousTaskFinished || !viewModelBin1.previousRemoteOrderSendFinished || doorOpenClicked1){
                                // 如果1仓还需要紧急开仓
                                setButtonEnable(true)
                            }else{
                                setButtonEnable(true, DoorState.DOOR_TWO)
                            }
                        }
                    }
                }
                DoorState.STATE_CLOSED -> {
                    controling = false
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            doorOpenClicked1 = false
                            if(!viewModelBin2.previousTaskFinished || !viewModelBin2.previousRemoteOrderSendFinished || doorOpenClicked2){
                                when (BillManager.currentBill()) {
                                    is DoubleSameSendTaskBillOne, is DoubleSameSendTaskBillTwo -> {
                                        binding.textViewBinControlCommit2.callOnClick()
                                    }
                                    else -> {
                                        // 如果1仓还需要紧急开仓
                                        setButtonEnable(true, DoorState.DOOR_TWO)
                                    }
                                }

                            }
                        }
                        DoorState.DOOR_TWO -> {
                            doorOpenClicked2 = false
                            if(!viewModelBin1.previousTaskFinished || !viewModelBin1.previousRemoteOrderSendFinished || doorOpenClicked1){
                                // 如果1仓还需要紧急开仓
                                setButtonEnable(true, DoorState.DOOR_ONE)
                            }
                        }
                    }
                }
                DoorState.STATE_OPENING -> {
                    controling = true
                    setButtonEnable(false)
                }
                DoorState.STATE_CLOSING -> {
                    controling = true
                    setButtonEnable(false)
                }
                DoorState.STATE_OPEN_FAILED -> {
                    controling = false
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            if(viewModelBin2.previousTaskFinished && viewModelBin2.previousRemoteOrderSendFinished){
                                // 如果2仓还需要紧急开仓
                                setButtonEnable(true)
                            }else{
                                setButtonEnable(true, DoorState.DOOR_ONE)
                            }
                        }
                        DoorState.DOOR_TWO -> {
                            if(viewModelBin1.previousTaskFinished && viewModelBin1.previousRemoteOrderSendFinished){
                                // 如果1仓还需要紧急开仓
                                setButtonEnable(true)
                            }else{
                                setButtonEnable(true, DoorState.DOOR_TWO)
                            }
                        }
                    }
                }
                DoorState.STATE_CLOSE_FAILED -> {
                    controling = true
                    setButtonEnable(false)
                }
                DoorState.STATE_HALF_OPEN -> {
                    controling = false
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            if(viewModelBin2.previousTaskFinished && viewModelBin2.previousRemoteOrderSendFinished){
                                // 如果2仓还需要紧急开仓
                                setButtonEnable(true)
                            }else{
                                setButtonEnable(true, DoorState.DOOR_ONE)
                            }
                        }
                        DoorState.DOOR_TWO -> {
                            if(viewModelBin1.previousTaskFinished && viewModelBin1.previousRemoteOrderSendFinished){
                                // 如果1仓还需要紧急开仓
                                setButtonEnable(true)
                            }else{
                                setButtonEnable(true, DoorState.DOOR_TWO)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.customIndicatorTextView.apply {
            val verifyPassword = basicSettingViewModel?.basicConfig?.verifyPassword?:"00000"
            setTextLength(verifyPassword.length)
            setIndicatorTextViewListener(object :
                CustomIndicatorTextView.IndicatorTextViewCallback {
                override fun fullText(text: String) {
                    if (verifyPassword == text) {
                        verifiedPassword = true
                    }else{
                        clearText()
                        ToastUtil.show("密码错误，请重新输入")
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

        binding.textViewPlace1.apply {
            text = getPlaceName(this)
        }

        binding.textViewPlace2.apply {
            text = getPlaceName(this)
        }
        binding.textViewBinControlCommit1.apply {
            isEnabled = true
            doorOpenClicked1 = doorOpenClicked1
            setOnClickListener {
                isEnabled = false
                controling = true
                when (doorOpenClicked1) {
                    true -> {
                        when (BillManager.currentBill()) {
                            is DoubleSameSendTaskBillOne, is DoubleSameSendTaskBillTwo -> {
                                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                                if(state.toByte() != DoorState.STATE_CLOSED){
                                    ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE)
                                }else{
                                    ROSHelper.controlBin(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO)
                                }
                            }
                            else -> {
                                controlDoor(RobotCommand.CMD_CLOSE, DoorState.DOOR_ONE, this)
                            }
                        }

                    }
                    false -> {
                        when (BillManager.currentBill()) {
                            is DoubleSameSendTaskBillOne, is DoubleSameSendTaskBillTwo -> {
                                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                                if(state.toByte() != DoorState.STATE_OPENED){
                                    ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE)
                                }else{
                                    ROSHelper.controlBin(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO)
                                }
                            }
                            else -> {
                                controlDoor(RobotCommand.CMD_OPEN, DoorState.DOOR_ONE, this)
                            }
                        }
                    }
                }
            }
        }
        binding.textViewBinControlCommit2.apply {
            isEnabled = true
            doorOpenClicked2 = doorOpenClicked2
            setOnClickListener {
                isEnabled = false
                controling = true
                when (doorOpenClicked2) {
                    true -> {
                        controlDoor(RobotCommand.CMD_CLOSE, DoorState.DOOR_TWO, this)
                    }
                    false -> {
                        controlDoor(RobotCommand.CMD_OPEN, DoorState.DOOR_TWO, this)
                    }
                }
            }
        }
        verifiedPassword = verifiedPassword
    }

    /**
     * @description 判断可以控制哪个仓门
     */
    private fun judgeControlWhichBin(){
        when (BillManager.currentBill()) {
            is DoubleSameSendTaskBillOne, is DoubleSameSendTaskBillTwo -> {
                if (!viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished) || !viewModelBin2.previousTaskFinished || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished)) {
                    binding.groupBin1.visibility = View.VISIBLE
                    binding.groupBin2.visibility = View.GONE
                }
            }
            else -> {
                //1号仓
                if(!viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished)){
                    binding.groupBin1.visibility = View.VISIBLE
                }
                //2号仓
                if(!viewModelBin2.previousTaskFinished || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished)){
                    binding.groupBin2.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * @description 获取送往地址
     */
    private fun getPlaceName(textView: TextView): String{
        return when(textView) {
            binding.textViewPlace1 -> {
                var result = ""
                if (!viewModelBin1.previousTaskFinished) {
                    val location = viewModelBin1.getCurrentSelectedLocation()
                    result = "${location?.floorName?:""}F-${location?.pointName}"
                }
                if(viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished){
                    val location = viewModelBin1.remoteOrderModel?.to
                    result = "${location?.floorName?:""}F-${location?.pointName}"
                }
                result
            }
            binding.textViewPlace2 -> {
                var result = ""
                if (!viewModelBin2.previousTaskFinished) {
                    val location = viewModelBin2.getCurrentSelectedLocation()
                    result = "${location?.floorName?:""}F-${location?.pointName}"
                }
                if(viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished){
                    val location = viewModelBin2.remoteOrderModel?.to
                    result = "${location?.floorName?:""}F-${location?.pointName}"
                }
                result
            }
            else -> ""
        }
    }

    private fun setButtonEnable(enable: Boolean, door:Byte = -1){
        mainScope.launch(Dispatchers.Main) {
            when (door) {
                DoorState.DOOR_ONE -> {
                    binding.textViewBinControlCommit1.apply {
                        isEnabled = enable
                        isClickable = enable
                    }
                }
                DoorState.DOOR_TWO -> {
                    binding.textViewBinControlCommit2.apply {
                        isEnabled = enable
                        isClickable = enable
                    }
                }
                else -> {
                    binding.textViewBinControlCommit1.apply {
                        isEnabled = enable
                        isClickable = enable
                    }
                    binding.textViewBinControlCommit2.apply {
                        isEnabled = enable
                        isClickable = enable
                    }
                }
            }
        }
    }

    private fun timerSchedule(
        timer: Timer,
        door: Byte
    ) {
        timer.schedule(object : TimerTask() {
            override fun run() {
                mainScope.launch(Dispatchers.Main) {
                    if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                    if (controling) return@launch
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            if (doorOpenClicked1 && second1 > 0) {
                                second1--
                            }
                        }
                        DoorState.DOOR_TWO -> {
                            if (doorOpenClicked2 && second2 > 0) {
                                second2--
                            }
                        }
                    }
                }
            }
        }, Date(), 1000)
    }

    /**
     * @description 控制仓门
     */
    private fun controlDoor(cmd: Byte, door: Byte, view: TextView){
        val resultCode = ROSHelper.controlBin(cmd, door)
        if(resultCode != 0){
            view.isEnabled = true
            controling = false
        }
    }

}