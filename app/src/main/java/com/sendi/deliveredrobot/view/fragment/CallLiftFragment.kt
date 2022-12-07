package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentCallLiftBinding
import com.sendi.deliveredrobot.helpers.SpeakHelper

class CallLiftFragment : Fragment() {
    lateinit var binding: FragmentCallLiftBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call_lift, container, false)
    }

    override fun onStart() {
        super.onStart()
        SpeakHelper.speak(getString(R.string.please_left_the_center_for_me))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageView.apply {
            Glide.with(this).asGif().load(R.raw.call_lift).into(this)
        }
        binding.constraintLayoutCallLift.apply {
            transitionToState(R.id.state2)
        }
        binding.bottomAlarmTextView.apply {
            bottomAlarmText1 = resources.getString(R.string.calling_lift)
        }
    }
}