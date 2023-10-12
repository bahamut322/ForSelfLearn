package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentSettingPlanBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoBackTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SettingViewModel

/**
 * @Author Swn
 * @describe 设置页面——今日计划
 * @Data 2023-04-17 08:54
 */
class PlanSettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingPlanBinding
    private var controller: NavController? = null
    private val viewModel by viewModels<SettingViewModel>({ requireActivity() })
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
                if (viewModel.isNumCharOne(1) || viewModel.isNumCharOne(4)) {
                    Toast.makeText(context, "未检测到RGB摄像头/测温摄像头", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    controller!!.navigate(R.id.action_planSettingFragment_to_cameraPreviewFragment)
                }
            }
        }
        //回桩
        binding.returnBlack.apply {
            setOnClickListener {
                val bill = GoBackTaskBillFactory.createBill(TaskModel())
                BillManager.addAllAtIndex(bill)
                BillManager.currentBill()?.executeNextTask()
            }
        }
        //返回主页面
        binding.returnHome.apply {
            setOnClickListener {
                when (FunctionSkip.selectFunction()) {
                    //智能引领
                    0 -> {
                        controller!!.navigate(R.id.action_planSettingFragment_to_GuideFragment)
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(context, "智能引领", Toast.LENGTH_SHORT).show()
                        }
                        LogUtil.i("智能引领")
                    }
                    //智能讲解
                    1 -> {
                        controller!!.navigate(R.id.action_planSettingFragment_to_explanationFragment)
                        if (BuildConfig.DEBUG) {
                            Toast.makeText(context, "智能讲解", Toast.LENGTH_SHORT).show()
                        }
                        LogUtil.i("智能讲解")
                    }
                    //智能问答
                    2 -> {
                        Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()
                        LogUtil.i("智能问答")
                    }
                    //轻应用
                    3 -> {
                        Toast.makeText(context, "轻应用", Toast.LENGTH_SHORT).show()
                        LogUtil.i("轻应用")
                    }
                    //不只有一个选项
                    4 -> {
                        controller!!.navigate(R.id.action_planSettingFragment_to_homeFragment)
                    }

                    -1 -> {
                        Toast.makeText(context, "请勾选主页面功能模块", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}