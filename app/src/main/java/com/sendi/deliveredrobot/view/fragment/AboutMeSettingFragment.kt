package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentAboutMeSettingBinding
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class AboutMeSettingFragment : Fragment() {
    lateinit var binding: FragmentAboutMeSettingBinding
    var controller: NavController? = null
    private val viewModel by viewModels<BasicSettingViewModel>({requireActivity()})
    var previousClickTime:Long = 0
    lateinit var mainScope: CoroutineScope
    companion object {
        const val OPERATE_MAX = 7
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_me_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        binding.viewProducer.apply {
            var count = 0
            setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if(((currentTime - previousClickTime) / 1000) < 1){
                    count++
                }else{
                    count = 1
                }
                previousClickTime = currentTime
                if (count in 3 until OPERATE_MAX) {
                    ToastUtil.show(String.format(getString(R.string.need_steps_to_into_debug_mode),"${OPERATE_MAX - count}"))
                } else if (count == OPERATE_MAX) {
                    count = 0
                    controller!!.navigate(R.id.action_settingHomeFragment_to_verifyToDebugFragment)

                }
            }
        }
//        binding.textViewUseTimeLimit.apply {
//            text =  viewModel.basicConfig.robotUseDeadLine
//        }
        binding.textViewSerialNumber.apply {
            text = RobotStatus.SERIAL_NUMBER
        }
        binding.textViewRobotVersion.apply {
            text = RobotStatus.chassisVersionName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainScope.cancel()
    }
}