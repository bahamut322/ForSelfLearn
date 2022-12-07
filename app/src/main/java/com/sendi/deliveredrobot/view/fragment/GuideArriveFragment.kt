package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentGuideArriveBinding
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.TaskQueue

class GuideArriveFragment : Fragment() {
    private lateinit var binding: FragmentGuideArriveBinding
//    private val viewModelGuide: GuidePlaceViewModel by viewModels({ requireActivity() })
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guide_arrive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        Glide.with(this).asGif().load(R.raw.guide_arrive).into(binding.imgArrive)

        binding.bottomAlarmTextViewArrive.apply {
            bottomAlarmText1 = getString(R.string.arrived)
            bottomAlarmText2 = BillManager.currentBill()?.endTarget()?:""
        }
        binding.motionLayoutGuideArrive.apply {
            transitionToState(R.id.state2)
        }
    }
}