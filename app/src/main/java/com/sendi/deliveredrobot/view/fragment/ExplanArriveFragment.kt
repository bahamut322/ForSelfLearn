package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentExplanArriveBinding
import com.sendi.deliveredrobot.databinding.FragmentGuideArriveBinding
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * @author swn
 * @describe 讲解结束
 */
class ExplanArriveFragment : Fragment() {
    private lateinit var binding: FragmentExplanArriveBinding
    lateinit var mainScope: CoroutineScope
//    private val viewModelGuide: GuidePlaceViewModel by viewModels({ requireActivity() })

    //倒计时
    val timer = object : CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.textViewSecond.text = (millisUntilFinished / 1000).toString()
            //暂停
            mainScope.launch {
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
            }
        }

        override fun onFinish() {
            mainScope.launch {
                //继续
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explan_arrive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        Glide.with(this).load(R.drawable.img_arrive).into(binding.imgArrive)
        mainScope = MainScope()
        binding.bottomAlarmTextViewArrive.text = BillManager.currentBill()?.endTarget() ?: ""
        binding.motionLayoutGuideArrive.apply {
            transitionToState(R.id.state2)
        }
        //开始倒计时
        timer.start()
    }



}