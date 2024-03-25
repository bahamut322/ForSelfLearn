package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentExplanArriveBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.viewmodel.StartExplainViewModel
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
    private val viewModelGuide: StartExplainViewModel by viewModels({ requireActivity() })

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
        BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(QuerySql.QueryExplainConfig().endText, route = QuerySql.queryPointDate(RobotStatus.selectRouteMapItemId)[0].routename))
//        viewModelGuide.splitTextByPunctuation(QuerySql.QueryExplainConfig().endText)
        binding.bottomAlarmTextViewArrive.text = BillManager.currentBill()?.endTarget() ?: ""
        binding.motionLayoutGuideArrive.apply {
            transitionToState(R.id.state2)
        }
        //开始倒计时
        timer.start()
    }



}