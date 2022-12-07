package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayout
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentChooseGuidePlaceBinding
import com.sendi.deliveredrobot.helpers.AudioMngHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.PlaceModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.GuidePlaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ChooseGuidePlaceFragment : Fragment() {
    private lateinit var binding: FragmentChooseGuidePlaceBinding
    private val viewModelGuidePlace by viewModels<GuidePlaceViewModel>({ requireActivity() })
    private val viewModelBasicSetting by viewModels<BasicSettingViewModel>({ requireActivity() })
    private lateinit var timer: Timer
    private lateinit var seconds: MutableLiveData<Int>
    private var fromType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_guide_place, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
        AudioMngHelper(requireContext()).setVoice100(viewModelBasicSetting.basicConfig.guideVolume!! / 2)
        MainScope().launch(Dispatchers.Default) {
            if (CommonHelper.atChargePointFloor()) {
                //如果是在充电桩的楼层
                AudioMngHelper(requireContext()).setVoice100(viewModelBasicSetting.basicConfig.guideVolumeLobby / 2)
            }else{
                AudioMngHelper(requireContext()).setVoice100((viewModelBasicSetting.basicConfig.guideVolume ?: 40) / 2)
            }
            val publicAreaPointsList =
                DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                    .queryPublicAreaPoints()
            val publicAreaPointsMap = publicAreaPointsList.groupBy {
                it.publicAreaId
            }
            val dataList: ArrayList<ArrayList<PlaceModel>> = ArrayList()
            for (entry in publicAreaPointsMap) {
                val tempList = ArrayList<PlaceModel>()
                for (queryPointEntity in entry.value) {
                    tempList.add(
                        PlaceModel(
                            false,
                            location = queryPointEntity
                        )
                    )
                }
                dataList.add(tempList)
            }
            viewModelGuidePlace.data = dataList
            val fragments: Array<Fragment> = Array(viewModelGuidePlace.data.size) {
                GuidePlaceListFragment(it)
            }
            withContext(Dispatchers.Main){
                binding.tabLayout.apply {
                    tabMode = TabLayout.MODE_AUTO
                    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            val textView = TextView(activity).apply {
                                textSize = PxUtil.sp2px(context, 36f).toFloat()
                                setTextColor(Color.WHITE)
                                text = tab!!.text
                                gravity = Gravity.CENTER
                            }
                            tab?.customView = textView
                            val bt = parentFragmentManager.beginTransaction()
                            bt.replace(R.id.flContainer, fragments[tab!!.position])
                            bt.commit()
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) {
                            tab?.customView = null
                        }

                        override fun onTabReselected(tab: TabLayout.Tab?) {
                        }
                    })
                    for (data in viewModelGuidePlace.data) {
                        addTab(newTab().apply {
                            text = "${data[0].location.publicAreaName}"
                        })
                    }
                    getTabAt(0)?.select()
                }
            }
        }

        binding = DataBindingUtil.bind(view)!!
        timer = Timer()
        seconds = MutableLiveData(30)
        binding.tvRoomNumber.apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.inputRoomNumberFragment,
                    Bundle().apply {
                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, fromType)
                    },
                )
                timer.cancel()
            }
        }
        binding.tvStartGuideCommit.apply {
            setOnClickListener {
                if(viewModelGuidePlace.getCurrentSelected() == null){
                    ToastUtil.show(getString(R.string.please_choose_destination))
                    return@setOnClickListener
                }
                isEnabled = false
                MainScope().launch(Dispatchers.Default) {
                    BillManager.currentBill()?.earlyFinish()
                    val bill = GuideTaskBillFactory.createBill(TaskModel(location = viewModelGuidePlace.getCurrentSelected()))
                    when (fromType) {
                        InputPasswordFromType.HOME_GUIDE -> {
                            BillManager.addAllAtIndex(bill)
                        }
                        InputPasswordFromType.GO_BACKING_GUIDE, InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                            BillManager.addAllAtIndex(bill, 1)
                        }
                    }
                    viewModelGuidePlace.clearGuidePlace()
                    timer.cancel()
                    ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
                }
            }
        }
        binding.textViewHome.apply {
            setOnClickListener {
                timer.cancel()
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })
            }
            visibility =
                if (BillManager.currentBill() != null && RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_PAUSE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        }

        binding.imageViewHome.apply {
            setOnClickListener {
                timer.cancel()
                MyApplication.instance?.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
                    putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                })
            }
            visibility =
                if (BillManager.currentBill() != null && RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_PAUSE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        }

        timerSchedule(timer, seconds)
        seconds.observe(viewLifecycleOwner) {
            binding.textViewSeconds.apply {
                if (it < 1) {
                    timer.cancel()
                    when (fromType) {
                        InputPasswordFromType.GUIDING_CHANGE_POINT -> {
                            findNavController().popBackStack(R.id.guidingFragment, false)
                            MainScope().launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.GO_BACKING_GUIDE -> {
                            findNavController().popBackStack(R.id.goBackFragment, false)
                            MainScope().launch {
                                ROSHelper.manageRobotUntilDone(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                        InputPasswordFromType.HOME_GUIDE -> {
                            MyApplication.instance?.sendBroadcast(Intent().apply {
                                action = ACTION_NAVIGATE
                                putExtra(NAVIGATE_ID, NAVIGATE_TO_HOME)
                            })
                        }
                    }
                }
                text = CommonHelper.getTimeSpan(it, 1.33f)
            }
        }
    }

    private fun timerSchedule(
        timer: Timer,
        seconds: MutableLiveData<Int>
    ) {
        timer.schedule(object : TimerTask() {
            override fun run() {
                MainScope().launch {
                    withContext(Dispatchers.Main){
                        if(RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return@withContext
                        seconds.value = seconds.value?.minus(1)
                    }
                }
            }
        }, Date(), 1000)
    }
}
