package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentChargeBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChargeFragment : Fragment() {
    private lateinit var binding: FragmentChargeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charge, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        Glide.with(this).asGif().load(R.raw.charge_bg).into(binding.imageViewCharge)
        RobotStatus.batteryPower.observe(viewLifecycleOwner) {
            MainScope().launch {
                withContext(Dispatchers.Main) {
                    binding.textViewProgress.text =
                        CommonHelper.getPresentSpan((it * 100).toInt(), 2.5f)
                }
            }
        }
        RobotStatus.chargeStatus.observe(viewLifecycleOwner){
            if(!it){
                findNavController().popBackStack()
            }
        }
        RobotStatus.batteryPower.observe(viewLifecycleOwner){
            if((it * 100).toInt() > RobotStatus.LOW_POWER_VALUE){
                findNavController().popBackStack()
            }
        }
    }
}