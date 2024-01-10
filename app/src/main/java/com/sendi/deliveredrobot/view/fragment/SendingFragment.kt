package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentSendingBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.util.*

class SendingFragment : Fragment() {
    private lateinit var binding: FragmentSendingBinding
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private var timer2:Timer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sending, container, false)
    }
    val mainScope = MainScope()

    override fun onStart() {
        super.onStart()
        mainScope.launch {
            withContext(Dispatchers.Default){
                virtualTaskExecute(5, "send语音前")
                if (CommonHelper.atChargePointFloor()) {
                    //如果是在充电桩的楼层
                    AudioMngHelper(requireContext()).setVoice100(
                        (basicSettingViewModel?.basicConfig?.sendVolumeLobby ?:60) / 2
                    )
                    timer2 = Timer()
                    timer2Schedule()
                }else{
                    AudioMngHelper(requireContext()).setVoice100(
                        (basicSettingViewModel?.basicConfig?.sendVolume ?:60) / 2
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageViewSending.apply {
            Glide.with(this).asGif().load(R.raw.sending).into(this)
        }

        binding.bottomAlarmTextViewSending.apply {
            bottomAlarmText1 = resources.getString(R.string.sending)
        }
        binding.motionLayoutSending.apply {
            transitionToState(R.id.state2)
        }
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
    }

    /**
     * @describe 第二个定时器（用于循环播报语音）
     */
    private fun timer2Schedule() {
        val welcome1 = String.format(getString(R.string.welcome_i_am_xiao_di), RobotStatus.robotConfig?.value)
        val welcome2 = getString(R.string.i_am_going_to_work_if_you_need_my_serve)
        val welcome3 = String.format(getString(R.string.i_hope_to_serve_you), RobotStatus.robotConfig?.value)
//        mainScope.launch {
//            SpeakHelper.speak(welcome1)
//            delay(4000)
//            SpeakHelper.speak(welcome2)
//            delay(6000)
//            SpeakHelper.speak(welcome3)
//        }
        timer2!!.schedule(object : TimerTask() {
            override fun run() {
                mainScope.launch(Dispatchers.IO) {
                    if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
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