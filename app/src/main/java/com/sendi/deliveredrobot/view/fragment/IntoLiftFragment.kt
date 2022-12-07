package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentIntoLiftBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import java.util.*

class IntoLiftFragment : Fragment() {
    private lateinit var binding: FragmentIntoLiftBinding
    private var timer:Timer? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_into_lift, container, false)
    }

    override fun onStart() {
        super.onStart()
        timer = Timer()
//        SpeakHelper.speak(MyApplication.instance!!.getString(R.string.i_going_into_lift))
        timer?.schedule(object :TimerTask(){
            override fun run() {
                if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED){
                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.i_going_into_lift))
                }
            }
        },Date(),15000)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageView.apply {
            Glide.with(this).asGif().load(R.raw.into_lift).into(this)
        }
        binding.motionLayoutIntoLift.apply {
            transitionToState(R.id.state2)
        }
        binding.bottomAlarmTextView.apply {
            bottomAlarmText1 = resources.getString(R.string.into_lift)
        }
    }

    override fun onStop() {
        super.onStop()
        if (timer == null) {
            return
        }
        timer?.cancel()
    }
}