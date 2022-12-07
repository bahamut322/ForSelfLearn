package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import chassis_msgs.DoorState
import com.google.android.material.tabs.TabLayout
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentChooseSendPlaceBin1Binding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.LiftHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.model.PlaceModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.topic.DoorStateTopic
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class ChooseSendPlaceBin1Fragment : Fragment() {
    private lateinit var binding: FragmentChooseSendPlaceBin1Binding
    private val sendPlaceBin1ViewModel by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private lateinit var timer: Timer
    private lateinit var seconds: MutableLiveData<Int>
    private var closing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_send_place_bin1, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        timer = Timer()
        seconds = MutableLiveData(basicSettingViewModel?.basicConfig?.sendPutObjectTime)

        MainScope().launch {
            if(sendPlaceBin1ViewModel.data.size == 0){
                val queryFloorPoints: List<QueryPointEntity>
                withContext(Dispatchers.Default){
                    queryFloorPoints = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                        .queryFloorPoints()
                }
                val publicAreaPointsMap = queryFloorPoints.groupBy {
//                    it.floorCode
                    it.floorName
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
                val sortDataList = dataList.sortedBy {
                    LiftHelper.findFloorScore(it[0].location.floorName?:"")
                }.toList() as ArrayList<ArrayList<PlaceModel>>
                sendPlaceBin1ViewModel.data = sortDataList
            }
            val fragments: Array<Fragment> = Array(sendPlaceBin1ViewModel.data.size) {
                SendPlaceBin1ListFragment(it)
            }
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
                for (data in sendPlaceBin1ViewModel.data) {
                    addTab(newTab().apply {
                        text = "${data[0].location.floorName}楼"
                    })
                }
                getTabAt(0)?.select()
            }
        }
        DoorStateTopic.setDoorStateListener {
                doorState ->
            val state = doorState.state
            val door = doorState.door
            when (state) {
                DoorState.STATE_OPENED -> {
                }
                DoorState.STATE_CLOSED -> {
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            MyApplication.instance!!.sendBroadcast(
                                Intent().apply {
                                    action = ACTION_NAVIGATE
                                    putExtra(NAVIGATE_ID,R.id.putObjectFragment)
                                    putExtra(NAVIGATE_BUNDLE,Bundle().apply {
                                        putInt(CHOOSE_BIN, CHOOSE_BIN1)
                                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE))
                                    })
                                }
                            )
                        }
                        DoorState.DOOR_TWO -> {
                        }
                    }
                }
                DoorState.STATE_OPENING -> {
                }
                DoorState.STATE_CLOSING -> {
                    SpeakHelper.speak(MyApplication.instance!!.getString(R.string.door_closing_take_care_hands))
                    closing = true
                }
                DoorState.STATE_OPEN_FAILED -> {
                }
                DoorState.STATE_CLOSE_FAILED -> {
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            binding.tvCloseBinCommit.apply {
                                isEnabled = true
                            }
                        }
                    }
                    closing = false
                }
                DoorState.STATE_HALF_OPEN -> {
                    when (door) {
                        DoorState.DOOR_ONE -> {
                            binding.tvCloseBinCommit.apply {
                                isEnabled = true
                            }
                        }
                    }
                    closing = false
                }
            }
        }

        binding.tvCloseBinCommit.apply {
            setOnClickListener {
//                timer.cancel()
                val state = ROSHelper.controlBin(cmd = RobotCommand.CMD_CHECK,door = DoorState.DOOR_ONE)
                if(state.toByte() != DoorState.STATE_CLOSED){
                    isEnabled = false
                    ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_ONE)
                }
            }
        }

        binding.imageViewSquareClear.apply {
            setOnClickListener {
                sendPlaceBin1ViewModel.clearSelected()
            }
        }
        sendPlaceBin1ViewModel.place.observe(viewLifecycleOwner) {
            if (TextUtils.isEmpty(it)) {
                with(binding.imageViewLocation) {
                    visibility = View.GONE
                }
                with(binding.textViewSelectedPlace) {
                    visibility = View.GONE
                }
                with(binding.imageViewSquareClear) {
                    visibility = View.GONE
                }
            } else {
                with(binding.imageViewLocation) {
                    visibility = View.VISIBLE
                }
                with(binding.textViewSelectedPlace) {
                    text =
                        String.format(context.resources.getString(R.string.send_place_selected), it)
                    visibility = View.VISIBLE
                }
                with(binding.imageViewSquareClear) {
                    visibility = View.VISIBLE
                }
            }
        }

        timerSchedule(timer, seconds)
        seconds.observe(viewLifecycleOwner) {
            binding.textViewSeconds.apply {
                if (it < 1) {
                    ROSHelper.controlBin(cmd = RobotCommand.CMD_CLOSE, door = DoorState.DOOR_ONE)
//                    findNavController().navigate(
//                        R.id.action_chooseSendPlaceBin1Fragment_to_putObjectFragment,
//                        Bundle().apply {
//                            putInt(CHOOSE_BIN, CHOOSE_BIN1)
//                        })
                    timer.cancel()
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
                        if(closing) return@withContext
                        seconds.value = seconds.value?.minus(1)
                    }
                }
            }
        }, Date(), 1000)
    }
}