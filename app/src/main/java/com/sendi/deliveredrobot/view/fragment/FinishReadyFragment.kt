package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentFinishDockBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023-07-25
 * @describe 到达待命点
 */
class FinishReadyFragment : Fragment() {
    private lateinit var binding: FragmentFinishDockBinding
    private var controller: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finish_dock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        controller = Navigation.findNavController(requireView())
        binding.successTv.text = "已到达待命点"
        Universal.guideTask = false
        RobotStatus.selectRoutMapItem!!.postValue(-1)
        Universal.businessTask = null
        MainScope().launch {
            virtualTaskExecute(2, "到达待命点")
            RobotStatus.batteryStateNumber.value = true
            when (FunctionSkip.selectFunction()) {
                //智能引领
                0 -> {
                    controller!!.navigate(R.id.action_FinishReadyFragment_to_GuideFragment)
                    Toast.makeText(context, "智能引领", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能引领")
                }
                //智能讲解
                1 -> {
                    controller!!.navigate(R.id.action_FinishReadyFragment_to_explanationFragment)
                    Toast.makeText(context, "智能讲解", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能讲解")
                }
                //智能问答
                2 -> {
                    Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能问答")
                }
                //更多服务
                3 -> {
                    controller!!.navigate(R.id.appContentFragment)
                    Toast.makeText(context, "更多服务", Toast.LENGTH_SHORT).show()
                    LogUtil.i("更多服务")
                }
                5 ->{
                    controller!!.navigate(R.id.action_FinishReadyFragment_to_businessFragment)
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "业务办理", Toast.LENGTH_SHORT).show()
                    }
                }
                //不只有一个选项
                4 -> {
                    controller!!.navigate(R.id.action_FinishReadyFragment_to_homeFragment)
                }

                -1 -> {
                    Toast.makeText(context, "请勾选主页面功能模块", Toast.LENGTH_SHORT).show()
                }
            }
            UpdateReturn().method()
//            MyApplication.instance!!.sendBroadcast(
//                Intent().apply {
//                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
//                }
//            )
        }
    }
}