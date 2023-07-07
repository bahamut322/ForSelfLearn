package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentLiftBinding
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LiftFragment : Fragment() {
    private lateinit var binding: FragmentLiftBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lift, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (RobotStatus.needDelay) return
        MainScope().launch {
            SpeakHelper.speak(String.format(getString(R.string.hello_mr_lift),RobotStatus.expectLocation?.floorName?:""))
            virtualTaskExecute(8, "电梯语音")
            SpeakHelper.speak(getString(R.string.happy_to_spend_the_time_with_you))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageView.apply {
            if (LiftHelper.findFloorScore(RobotStatus.currentLocation?.floorName ?: "") < LiftHelper.findFloorScore(
                    RobotStatus.expectLocation?.floorName ?: ""
                )
            ) {
                Glide.with(this).asGif().load(R.raw.go_up).into(this)
            } else {
                Glide.with(this).asGif().load(R.raw.go_down).into(this)
            }
        }
        binding.motionLayoutLift.apply {
            transitionToState(R.id.state2)
        }
        binding.bottomAlarmTextView.apply {
            bottomAlarmText1 = String.format(
                resources.getString(R.string.go_up),
                "${RobotStatus.currentLocation?.floorName ?: ""}楼",
                "${RobotStatus.expectLocation?.floorName ?: ""}楼"
            )
            if (LiftHelper.findFloorScore(RobotStatus.currentLocation?.floorName ?: "") < LiftHelper.findFloorScore(
                    RobotStatus.expectLocation?.floorName ?: ""
                )
            ) {
                startUpArrowAnim()
            } else {
                startDownArrowAnim()
            }
        }
    }
}