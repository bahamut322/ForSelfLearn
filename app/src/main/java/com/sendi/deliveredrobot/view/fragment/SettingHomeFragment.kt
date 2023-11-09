package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import chassis_msgs.DoorState
import chassis_msgs.VersionGetResponse
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.adapter.SettingHomeListAdapter
import com.sendi.deliveredrobot.databinding.FragmentSettingHomeBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.viewmodel.SettingHomeViewModel
import kotlinx.coroutines.*

class SettingHomeFragment : Fragment() {

    var controller: NavController? = null
    private lateinit var binding: FragmentSettingHomeBinding
    val viewModel by viewModels<SettingHomeViewModel>({ requireActivity() })
    var mainScope: CoroutineScope? = null
    val fragments = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScope = MainScope()
        //获取底盘版本号
        mainScope?.launch {
            var versionGetResponse: VersionGetResponse? = null
            withContext(Dispatchers.Default) {
                versionGetResponse = ROSHelper.getVersion(1) ?: return@withContext
                LogUtil.d("底盘版本：$versionGetResponse")
            }
            if (versionGetResponse?.success == true) {
                withContext(Dispatchers.Main) {
                    RobotStatus.chassisVersionName = versionGetResponse?.version ?: ""
                    LogUtil.d("底盘版本：${RobotStatus.chassisVersionName}")

                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mainScope?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        binding = DataBindingUtil.bind(view)!!
        fragments.add(DebugBasicSettingFragment())
        fragments.add(ModeSettingFragment())
        fragments.add(NetworkFragment())
        fragments.add(AboutMeSettingFragment())
        binding.recyclerViewSettingHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SettingHomeListAdapter(viewModel)
        }
        viewModel.currentSettingPosition.observe(viewLifecycleOwner) {
            val bt = parentFragmentManager.beginTransaction()
            bt.replace(R.id.frameLayoutContainer, fragments[it])
            bt.commit()
        }
//        binding.textViewHome.apply {
//            setOnClickListener {
//                val jumpStatus: Boolean = findNavController().popBackStack(R.id.homeFragment, false)
//                if (!jumpStatus) {
//                    //如果开机后没有到达过主页
//                    findNavController().popBackStack()
//                    findNavController().navigate(R.id.homeFragment)
//                }
//            }
//            setOnLongClickListener {
//                postDelayed({
//                    if (!it.isPressed) return@postDelayed
//                    val jumpStatus: Boolean = findNavController().popBackStack(R.id.homeFragment, false)
//                    if (!jumpStatus) {
//                        //如果开机后没有到达过主页
//                        findNavController().popBackStack()
//                        findNavController().navigate(R.id.homeFragment)
//                    }
//                },4500)
//                true
//            }
//        }

//        binding.imageViewHome.apply {
//            setOnClickListener {
//                val jumpStatus: Boolean = findNavController().popBackStack(R.id.homeFragment, false)
//                if (!jumpStatus) {
//                    //如果开机后没有到达过主页
//                    findNavController().popBackStack()
//                    findNavController().navigate(R.id.homeFragment)
//                }
//            }
//            setOnLongClickListener {
//                postDelayed({
//                    if (!it.isPressed) return@postDelayed
//                    val jumpStatus: Boolean = findNavController().popBackStack(R.id.homeFragment, false)
//                    if (!jumpStatus) {
//                        //如果开机后没有到达过主页
//                        findNavController().popBackStack()
//                        findNavController().navigate(R.id.homeFragment)
//                    }
//                },4500)
//                true
//            }
//        }
        //返回时判断数据长度来，判断全选是否勾选一个功能
        binding.returnHome.setOnClickListener {
            for (fragment in fragments) {
                fragment.onDestroy()
            }
            when(FunctionSkip.selectFunction()){
                //智能引领
                0 ->{
                    controller!!.navigate(R.id.action_settingHomeFragment_to_GuideFragment)
                    Toast.makeText(context, "智能引领", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能引领")
                }
                //智能讲解
                1 ->{
                    controller!!.navigate(R.id.action_settingHomeFragment_to_explanationFragment)
                    Toast.makeText(context, "智能讲解", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能讲解")
                }
                //智能问答
                2->{
                    Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()
                    LogUtil.i("智能问答")
                }
                //轻应用
                3->{
                    Toast.makeText(context, "轻应用", Toast.LENGTH_SHORT).show()
                    LogUtil.i("轻应用")
                }
                5 ->{
                    controller!!.navigate(R.id.action_settingHomeFragment_to_businessFragment)
                    Toast.makeText(context, "业务办理", Toast.LENGTH_SHORT).show()
                    LogUtil.i("业务办理")
                }
                //不只有一个选项
                4->{
                    controller!!.navigate(R.id.action_settingHomeFragment_to_homeFragment)
                }
                -1->{
                    Toast.makeText(context, "请勾选主页面功能模块", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * @describe 检测仓门状态
     */
    private fun checkDoor(): Boolean {
        val doorOneState = ROSHelper.controlBin(RobotCommand.CMD_CHECK, DoorState.DOOR_ONE)
        val doorTwoState = ROSHelper.controlBin(RobotCommand.CMD_CHECK, DoorState.DOOR_TWO)
        if (doorOneState.toByte() != DoorState.STATE_CLOSED || doorTwoState.toByte() != DoorState.STATE_CLOSED) {
            return false
        }
        return true
    }
}