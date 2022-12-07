package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import chassis_msgs.DoorState
import chassis_msgs.VersionGetResponse
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.adapter.SettingHomeListAdapter
import com.sendi.deliveredrobot.databinding.FragmentSettingHomeBinding
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
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
            }
            if (versionGetResponse?.success == true) {
                withContext(Dispatchers.Main) {
                    RobotStatus.chassisVersionName = versionGetResponse?.version ?: ""
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("TAG11111one", "onStop: ")
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
        fragments.add(WifiSettingFragment())
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
        binding.returnHome.setOnClickListener(View.OnClickListener {
            controller!!.navigate(R.id.action_settingHomeFragment_to_homeFragment)
            for (fragment in fragments) {
                fragment.onPause()
            }
        })
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