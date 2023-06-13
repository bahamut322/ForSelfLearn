package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import chassis_msgs.SafeState
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentGoBackBinding
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueue
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.LowPowerDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.properties.Delegates

class GoBackFragment : Fragment() {
    private lateinit var binding: FragmentGoBackBinding
    private var timer: Timer? = null
    private var timer2:Timer? = null
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var mainScope: CoroutineScope
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })

    private var state by Delegates.observable(0){
            _, _, newValue ->
        when (newValue) {
            0 -> {
                IdleGateDataHelper.reportIdleGateCount()
                binding.imageViewGoBack.apply {
                    isEnabled = true
                }
                mainScope.launch {
                    withContext(Dispatchers.Default) {
                        virtualTaskExecute(5, "GoBackFragment语音前")
                        if (CommonHelper.atChargePointFloor()) {
                            //如果是在充电桩的楼层
                            timer2?.cancel()
                            timer2?.purge()
                            timer2 = Timer()
                            timer2Schedule()
                        }
                    }
                }
                seconds.value = 30
                LogUtil.i("current:state2")
            }
            1 -> {
                IdleGateDataHelper.reportIdleGateCount(0)
                timer?.cancel()
                timer2?.cancel()
                timer2?.purge()
                if (RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED) {
                    SpeakHelper.speak(getString(R.string.can_i_help_you))
                    timer = Timer()
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            mainScope.launch {
                                withContext(Dispatchers.Main) {
                                    seconds.value = seconds.value?.minus(1)
                                }
                            }
                        }
                    }, Date(), 1000)
                }
                LogUtil.i("current:state3")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_go_back, container, false)
    }

    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        seconds = MutableLiveData(30)
//        if (QuerySql.queryMyData(RobotStatus.selectRoutMapItem!!.value!!)[0].touch_type == 4) {
//            binding.goBackTv.visibility = View.GONE
//            binding.imageViewGoBack.apply {
//                Glide.with(this).asGif().load(QuerySql.queryMyData(RobotStatus.selectRoutMapItem!!.value!!)[0].touch_overTaskPic).into(this)
//            }
//        }
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
                    if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@launch
                    SpeakHelper.speakWithoutStop(welcome1)
                    SpeakHelper.speakWithoutStop(welcome2)
                    SpeakHelper.speakWithoutStop(welcome3)
                }
            }
        }, Date(), 25000)
    }
}