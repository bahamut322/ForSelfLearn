package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentOutLiftBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import java.util.*

class OutLiftFragment : Fragment() {
    private lateinit var binding:FragmentOutLiftBinding
    private var timer:Timer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_out_lift, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (!RobotStatus.needDelay){
            timer = Timer()
//            SpeakHelper.speak(getString(R.string.i_arrived_please_left_front_way_for_me))
            timer?.schedule(object :TimerTask(){
                override fun run() {
                    if(findNavController().currentDestination?.label == "fragment_guiding"){
                        return
                    }
                    if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED){
                        SpeakHelper.speak(getString(R.string.i_arrived_please_left_front_way_for_me))
                    }
                }
            },Date(),15000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageView.apply {
            Glide.with(this).asGif().load(R.raw.out_lift).into(this)
        }
        binding.motionLayoutOutLift.apply {
            transitionToState(R.id.state2)
        }
        binding.bottomAlarmTextView.apply {
            bottomAlarmText1 = resources.getString(R.string.out_lift)
        }
    }

    override fun onStop() {
        super.onStop()
        if(timer == null){
            return
        }
        timer?.cancel()
    }
}