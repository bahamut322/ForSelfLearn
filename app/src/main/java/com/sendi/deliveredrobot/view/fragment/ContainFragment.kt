package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentContainBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

/**
 * @describe 被围堵
 */
class ContainFragment : Fragment() {
    private lateinit var binding: FragmentContainBinding
    private var timer = Timer()
    private val mainScope = MainScope()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contain, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
//        binding.imageView.apply {
//            Glide.with(this).asGif().load(R.raw.contain).into(this)
//        }
        binding.motionLayoutContain.apply {
            transitionToState(R.id.state2)
        }
//        binding.bottomAlarmTextView.apply {
//            bottomAlarmText1 = resources.getString(R.string.please_do_not_crowd_me)
//        }
//        val excuseMe = getString(R.string.excuse_me)
//        timer.schedule(object :TimerTask(){
//            override fun run() {
//                if(RobotStatus.stopButtonPressed.value != RobotCommand.STOP_BUTTON_PRESSED){
//                    mainScope.launch(Dispatchers.IO) {
//                        SpeakHelper.speak(excuseMe)
//                    }
//                }
//            }
//        },Date(),6000)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        mainScope.cancel()
    }
}