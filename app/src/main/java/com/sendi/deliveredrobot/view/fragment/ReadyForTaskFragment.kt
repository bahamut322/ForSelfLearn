package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentReadyForTaskBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper

/**
 * @describe 准备工作中页面
 */
class ReadyForTaskFragment : Fragment() {
    lateinit var binding:FragmentReadyForTaskBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ready_for_task, container, false)
    }

    override fun onStart() {
        super.onStart()
        SpeakHelper.speak(getString(R.string.i_ready_to_work_excuse_me))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.bottomAlarmTextViewReadyForTask.apply {
            bottomAlarmText1 = getString(R.string.i_ready_to_work_excuse_me)
        }
        binding.motionLayoutReadyForTask.apply {
            transitionToState(R.id.state2)
        }
    }

}