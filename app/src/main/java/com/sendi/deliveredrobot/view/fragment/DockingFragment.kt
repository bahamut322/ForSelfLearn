package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentDockingBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.ToastUtil

/**
 * @describe 充电桩对接中
 */
class DockingFragment : Fragment() {
    private lateinit var binding: FragmentDockingBinding
    private var isAutoDocking = true  //是否自动充电对接

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_docking, container, false)
    }

    override fun onStart() {
        super.onStart()
        when (SpeakHelper.getType()) {
            SpeakHelper.TYPE_BAIDU -> SpeakHelper.stop()
        }
        SpeakHelper.speak(String.format(getString(R.string.start_docking), QuerySql.robotConfig().wakeUpWord))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        Universal.guideTask = false
        RobotStatus.selectRouteMapItemId = -1
        Universal.businessTask = null
//        Glide.with(this).asGif().load(R.raw.docking).into(binding.imageViewDocking)
        binding.textViewBeginPush.apply {
            isClickable = true
            setOnClickListener {
                //停止dock
                val res = ROSHelper.controlDock(RobotCommand.CMD_PAUSE)
//                var res = ROSHelper.controlDock(RobotCommand.CMD_STOP)
                if(res){
                    isAutoDocking = false
                    binding.handChargDockingCl.apply {
                        visibility = View.VISIBLE
                    }
                    binding.autoChargDockingCl.apply {
                        visibility = View.GONE
                    }
                }else{
                    ToastUtil.show(getString(R.string.hand_dock_fail))
                }
            }
        }
    }
}