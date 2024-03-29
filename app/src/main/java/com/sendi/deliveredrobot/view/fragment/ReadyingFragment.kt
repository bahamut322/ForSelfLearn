package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGuidingBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.*
import java.util.*


class ReadyingFragment : Fragment() {
    private lateinit var binding: FragmentGuidingBinding
    private val viewModelBasicSetting by viewModels<BasicSettingViewModel>({ requireActivity() })
    private var timer: Timer? = null
    private var timer2: Timer? = null
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var mainScope: CoroutineScope
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gifGuiding = Glide.with(this).asGif().load(R.drawable.img_goback)
        gifStopGuide = Glide.with(this).asGif().load(R.drawable.img_goback)
    }

    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guiding, container, false)
    }

    private lateinit var gifGuiding: RequestBuilder<GifDrawable>

    private lateinit var gifStopGuide: RequestBuilder<GifDrawable>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        mainScope.launch {
            val bill = BillManager.currentBill()
            if (bill is GoToReadyPointBill) {
                Universal.speakInt++
                // 设置标志位为true，表示已经进入过该方法
                var pointName = bill?.endTarget()
                pointName = pointName?.toList()?.joinToString(" ")
                binding.RoomName.text = pointName
                if (Universal.speakInt % 2 != 0) {
                    BaiduTTSHelper.getInstance().speak(
                        String.format(
                            getString(R.string.hello_we_are_going_to_please_follow_me_1),
                            QuerySql.robotConfig().wakeUpWord,
                            pointName
                        ),
                        ""
                    )
                }
            }
        }

        seconds = MutableLiveData(viewModelBasicSetting.basicConfig.guideWalkPauseTime)
        seconds.observe(viewLifecycleOwner) {
            if (it < 1) {
                timer?.cancel()
                mainScope.launch {
                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
                        timer?.cancel()
                    } else {
                        ToastUtil.show("继续失败，请重试")
                    }
                }
            }
        }

    }
}