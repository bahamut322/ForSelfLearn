package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.qmuiteam.qmui.kotlin.onClick
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentSettingPlanBinding

/**
 * @Author Swn
 * @describe 设置页面——今日计划
 * @Data 2023-04-17 08:54
 */
class PlanSettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingPlanBinding
    private var controller: NavController? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        controller = Navigation.findNavController(view)
        //设置
        binding.setting.apply {
            setOnClickListener {
                controller!!.navigate(R.id.action_planSettingFragment_to_settingHomeFragment)
            }
        }
        //智能巡逻
        binding.patrol.apply {
            setOnClickListener {

            }
        }
        //专场讲解
        binding.explanation.apply {
            setOnClickListener {

            }
        }
        //门岗测温
        binding.temp.apply {
            setOnClickListener {
                controller!!.navigate(R.id.action_planSettingFragment_to_cameraPreviewFragment)
            }
        }
        //回桩
        binding.returnBlack.apply {
            setOnClickListener {

            }
        }
        //返回主页面
        binding.returnHome.apply {
            setOnClickListener {
                controller!!.navigate(R.id.action_planSettingFragment_to_homeFragment)
            }
        }
    }
}