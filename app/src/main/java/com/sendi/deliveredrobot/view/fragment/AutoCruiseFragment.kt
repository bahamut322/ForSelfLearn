package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentAutoCruiseBinding
import com.sendi.deliveredrobot.navigationtask.RobotStatus

/**
 * @describe 自动巡航
 */
class AutoCruiseFragment : Fragment() {
    lateinit var binding: FragmentAutoCruiseBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_cruise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.switchAutoCruise.apply {
            setOnCheckedChangeListener { _, isChecked ->
                RobotStatus.autoCruise = isChecked
            }
            isChecked = RobotStatus.autoCruise
        }
    }

}