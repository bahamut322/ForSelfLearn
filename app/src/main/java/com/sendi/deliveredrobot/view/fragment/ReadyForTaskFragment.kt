package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentReadyForTaskBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoBackTaskBill

/**
 * @describe 准备工作中页面
 */
class ReadyForTaskFragment : Fragment() {
    lateinit var binding:FragmentReadyForTaskBinding
    private var speakText = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ready_for_task, container, false)
    }

    override fun onStart() {
        super.onStart()
        speakText = if (BillManager.currentBill() is GoBackTaskBill) {
            getString(R.string.retry_dock)
        }else{
            getString(R.string.i_ready_to_work_excuse_me)
        }
        SpeakHelper.speakWithoutStop(speakText)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.bottomAlarmTextViewReadyForTask.apply {
            bottomAlarmText1 = speakText
        }
        binding.motionLayoutReadyForTask.apply {
            transitionToState(R.id.state2)
        }
    }

}