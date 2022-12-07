package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentCallRoomBinding
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.TaskQueue

class CallRoomFragment : Fragment() {
    lateinit var binding: FragmentCallRoomBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_call_room, container, false)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.imageViewCallRoom.apply {
            Glide.with(this).asGif().load(R.raw.call_room).into(this)
        }
        binding.motionLayoutCallRoom.apply {
            transitionToState(R.id.state2)
        }
        binding.bottomAlarmTextViewCallRoom.apply {
            bottomAlarmText1 = getString(R.string.calling)
            bottomAlarmText2 =  "${BillManager.currentBill()?.endTarget()}"
        }
    }
}