package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentHomeBinding
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.model.RequestTenancyModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.AppUtils
import com.sendi.deliveredrobot.view.widget.CloseDeadlineDialog
import com.sendi.deliveredrobot.view.widget.ExpireDeadlineDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.DateViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @describe 主页面
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val dateViewModel by viewModels<DateViewModel>({ requireActivity() })
    private lateinit var mainScope: CoroutineScope
    private var localDays:Int = Int.MIN_VALUE //记录上一次获取的使用剩余天数
    private var remindDialog: Dialog? = null

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm")
    private val dayOfWeekChinese = arrayOf("星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    var controller: NavController? = null
    private var view1: LinearLayout? = null
    private var view2: LinearLayout? = null
    private var view3: LinearLayout? = null
    private var view4: LinearLayout? = null
    private var view5: LinearLayout? = null
    var rescolors: Array<String>? = null
    private var view1_1_0: LinearLayout? = null
    private var view1_2_0: LinearLayout? = null
    private var view2_1_0: LinearLayout? = null
    private var view2_2_0: LinearLayout? = null
    private var view2_3_0: LinearLayout? = null
    private var view3_1_0: LinearLayout? = null
    private var view3_2_0: LinearLayout? = null
    private var view3_3_0: ConstraintLayout? = null
    private var view3_4_0: ConstraintLayout? = null
    private var view4_1_0: LinearLayout? = null
    private var view4_2_0: ConstraintLayout? = null
    private var view4_3_0: ConstraintLayout? = null
    private var view4_4_0: ConstraintLayout? = null
    private var view4_5_0: ConstraintLayout? = null
    private var view5_1_0: ConstraintLayout? = null
    private var view5_2_0: ConstraintLayout? = null
    private var view5_3_0: ConstraintLayout? = null
    private var view5_4_0: ConstraintLayout? = null
    private var view5_5_0: ConstraintLayout? = null
    private var view5_6_0: ConstraintLayout? = null

    //背景
    private var view1_1_1: ImageView? = null
    private var view1_2_1: ImageView? = null
    private var view2_1_1: ImageView? = null
    private var view2_2_1: ImageView? = null
    private var view2_3_1: ImageView? = null
    private var view3_1_1: ImageView? = null
    private var view3_2_1: ImageView? = null
    private var view3_3_1: ImageView? = null
    private var view3_4_1: ImageView? = null
    private var view4_1_1: ImageView? = null
    private var view4_2_1: ImageView? = null
    private var view4_3_1: ImageView? = null
    private var view4_4_1: ImageView? = null
    private var view4_5_1: ImageView? = null
    private var view5_1_1: ImageView? = null
    private var view5_2_1: ImageView? = null
    private var view5_3_1: ImageView? = null
    private var view5_4_1: ImageView? = null
    private var view5_5_1: ImageView? = null
    private var view5_6_1: ImageView? = null

    //标题文字
    private var view1_1_2: TextView? = null
    private var view1_2_2: TextView? = null
    private var view2_1_2: TextView? = null
    private var view2_2_2: TextView? = null
    private var view2_3_2: TextView? = null
    private var view3_1_2: TextView? = null
    private var view3_2_2: TextView? = null
    private var view3_3_2: TextView? = null
    private var view3_4_2: TextView? = null
    private var view4_1_2: TextView? = null
    private var view4_2_2: TextView? = null
    private var view4_3_2: TextView? = null
    private var view4_4_2: TextView? = null
    private var view4_5_2: TextView? = null
    private var view5_1_2: TextView? = null
    private var view5_2_2: TextView? = null
    private var view5_3_2: TextView? = null
    private var view5_4_2: TextView? = null
    private var view5_5_2: TextView? = null
    private var view5_6_2: TextView? = null

    //英文
    private var view1_1_3: TextView? = null
    private var view1_2_3: TextView? = null
    private var view2_1_3: TextView? = null
    private var view2_2_3: TextView? = null
    private var view2_3_3: TextView? = null
    private var view3_1_3: TextView? = null
    private var view3_2_3: TextView? = null
    private var view3_3_3: TextView? = null
    private var view3_4_3: TextView? = null
    private var view4_1_3: TextView? = null
    private var view4_2_3: TextView? = null
    private var view4_3_3: TextView? = null
    private var view4_4_3: TextView? = null
    private var view4_5_3: TextView? = null
    private var view5_1_3: TextView? = null
    private var view5_2_3: TextView? = null
    private var view5_3_3: TextView? = null
    private var view5_4_3: TextView? = null
    private var view5_5_3: TextView? = null
    private var view5_6_3: TextView? = null
    var TAG = "Tag"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = DataBindingUtil.bind(view)!!

        return view
    }

    override fun onStart() {
        super.onStart()
        mainScope = MainScope()
        resetConfig()
        //弹出送物任务提示窗
        val message = when {
            (!viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished)) && (!viewModelBin2.previousTaskFinished  || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished)) -> resources.getString(
                R.string.bin_1_bin_2_not_take
            )
            !viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished) -> resources.getString(R.string.bin_1_not_take)
            !viewModelBin2.previousTaskFinished || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished) -> resources.getString(R.string.bin_2_not_take)
            else -> ""
        }
        if (!TextUtils.isEmpty(message)) {
            remindDialog = DialogHelper.getRemindDialog(message).apply { show() }
        }
        //查询使用期限
        CloudMqttService.publish(RequestTenancyModel().toString())
        RobotStatus.tenancy.observe(viewLifecycleOwner){
            basicSettingViewModel?.basicConfig?.robotUseDeadLine = it.deadline
            MainScope().launch(Dispatchers.Default) {
                dao.updateBasicConfig(basicSettingViewModel?.basicConfig!!)
            }
            when (it.useType) {
                1,2 -> {
                    //租赁
                    if (it.days == localDays) return@observe
                    localDays = it.days?:0
                    judgeDays(it.days?:0)
                }
            }
        }
        IdleGateDataHelper.reportIdleGateCount()
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
        remindDialog?.dismiss()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.checkPermission(activity, 0)
        controller = Navigation.findNavController(view)
        view1 = view.findViewById(R.id.view1)
        view2 = view.findViewById(R.id.view2)
        view3 = view.findViewById(R.id.view3)
        view4 = view.findViewById(R.id.view4)
        view5 = view.findViewById(R.id.view5)
        //布局
        view1_1_0 = view.findViewById(R.id.view1_1_0)
        view1_2_0 = view.findViewById(R.id.view1_2_0)
        //标题文字
        view1_1_2 = view.findViewById(R.id.view1_1_2)
        view1_2_2 = view.findViewById(R.id.view1_2_2)
        //图片
        view1_1_1 = view.findViewById(R.id.view1_1_1)
        view1_2_1 = view.findViewById(R.id.view1_2_1)
        //英文
        view1_1_3 = view.findViewById(R.id.view1_1_3)
        view1_2_3 = view.findViewById(R.id.view1_2_3)

        //布局
        view2_1_0 = view.findViewById(R.id.view2_1_0)
        view2_2_0 = view.findViewById(R.id.view2_2_0)
        view2_3_0 = view.findViewById(R.id.view2_3_0)
        //标题文字
        view2_1_2 = view.findViewById(R.id.view2_1_2)
        view2_2_2 = view.findViewById(R.id.view2_2_2)
        view2_3_2 = view.findViewById(R.id.view2_3_2)
        //图片
        view2_1_1 = view.findViewById(R.id.view2_1_1)
        view2_2_1 = view.findViewById(R.id.view2_2_1)
        view2_3_1 = view.findViewById(R.id.view2_3_1)
        //英文
        view2_1_3 = view.findViewById(R.id.view2_1_3)
        view2_2_3 = view.findViewById(R.id.view2_2_3)
        view2_3_3 = view.findViewById(R.id.view2_3_3)

        //布局
        view3_1_0 = view.findViewById(R.id.view3_1_0)
        view3_2_0 = view.findViewById(R.id.view3_2_0)
        view3_3_0 = view.findViewById(R.id.view3_3_0)
        view3_4_0 = view.findViewById(R.id.view3_4_0)
        //标题文字
        view3_1_2 = view.findViewById(R.id.view3_1_2)
        view3_2_2 = view.findViewById(R.id.view3_2_2)
        view3_3_2 = view.findViewById(R.id.view3_3_2)
        view3_4_2 = view.findViewById(R.id.view3_4_2)
        //图片
        view3_1_1 = view.findViewById(R.id.view3_1_1)
        view3_2_1 = view.findViewById(R.id.view3_2_1)
        view3_3_1 = view.findViewById(R.id.view3_3_1)
        view3_4_1 = view.findViewById(R.id.view3_4_1)
        //英文
        view3_1_3 = view.findViewById(R.id.view3_1_3)
        view3_2_3 = view.findViewById(R.id.view3_2_3)
        view3_3_3 = view.findViewById(R.id.view3_3_3)
        view3_4_3 = view.findViewById(R.id.view3_4_3)

        //布局
        view4_1_0 = view.findViewById(R.id.view4_1_0)
        view4_2_0 = view.findViewById(R.id.view4_2_0)
        view4_3_0 = view.findViewById(R.id.view4_3_0)
        view4_4_0 = view.findViewById(R.id.view4_4_0)
        view4_5_0 = view.findViewById(R.id.view4_5_0)
        //标题
        view4_1_2 = view.findViewById(R.id.view4_1_2)
        view4_2_2 = view.findViewById(R.id.view4_2_2)
        view4_3_2 = view.findViewById(R.id.view4_3_2)
        view4_4_2 = view.findViewById(R.id.view4_4_2)
        view4_5_2 = view.findViewById(R.id.view4_5_2)
        //图片
        view4_1_1 = view.findViewById(R.id.view4_1_1)
        view4_2_1 = view.findViewById(R.id.view4_2_1)
        view4_3_1 = view.findViewById(R.id.view4_3_1)
        view4_4_1 = view.findViewById(R.id.view4_4_1)
        view4_5_1 = view.findViewById(R.id.view4_5_1)
        //英文
        view4_1_3 = view.findViewById(R.id.view4_1_3)
        view4_2_3 = view.findViewById(R.id.view4_2_3)
        view4_3_3 = view.findViewById(R.id.view4_3_3)
        view4_4_3 = view.findViewById(R.id.view4_4_3)
        view4_5_3 = view.findViewById(R.id.view4_5_3)

        //布局
        view5_1_0 = view.findViewById(R.id.view5_1_0)
        view5_2_0 = view.findViewById(R.id.view5_2_0)
        view5_3_0 = view.findViewById(R.id.view5_3_0)
        view5_4_0 = view.findViewById(R.id.view5_4_0)
        view5_5_0 = view.findViewById(R.id.view5_5_0)
        view5_6_0 = view.findViewById(R.id.view5_6_0)
        //标题
        view5_1_2 = view.findViewById(R.id.view5_1_2)
        view5_2_2 = view.findViewById(R.id.view5_2_2)
        view5_3_2 = view.findViewById(R.id.view5_3_2)
        view5_4_2 = view.findViewById(R.id.view5_4_2)
        view5_5_2 = view.findViewById(R.id.view5_5_2)
        view5_6_2 = view.findViewById(R.id.view5_6_2)
        //图片
        view5_1_1 = view.findViewById(R.id.view5_1_1)
        view5_2_1 = view.findViewById(R.id.view5_2_1)
        view5_3_1 = view.findViewById(R.id.view5_3_1)
        view5_4_1 = view.findViewById(R.id.view5_4_1)
        view5_5_1 = view.findViewById(R.id.view5_5_1)
        view5_6_1 = view.findViewById(R.id.view5_6_1)
        //英文
        view5_1_3 = view.findViewById(R.id.view5_1_3)
        view5_2_3 = view.findViewById(R.id.view5_2_3)
        view5_3_3 = view.findViewById(R.id.view5_3_3)
        view5_4_3 = view.findViewById(R.id.view5_4_3)
        view5_5_3 = view.findViewById(R.id.view5_5_3)
        view5_6_3 = view.findViewById(R.id.view5_6_3)
//        binding.buttonGuide.apply {
//            setOnClickListener {
//                mainScope.launch(Dispatchers.Default) {
//                    DialogHelper.loadingDialog.show()
//                    if (!judgeBeforeNavigate()) {
//                        DialogHelper.loadingDialog.dismiss()
//                        ToastUtil.show("请让机器人处于充电状态")
//                        return@launch
//                    }else{
//                        val chargePoint = dao.queryChargePoint()
//                        ROSHelper.setCalculateChargePose(chargePoint)
//                    }
//                    DialogHelper.loadingDialog.dismiss()
//                    if (BillManager.billList().isNotEmpty()) {
//                        return@launch
//                    }
//                    if (RobotStatus.originalLocation == null) {
//                        ToastUtil.show(resources.getString(R.string.charge_point_not_set))
//                        return@launch
//                    }
//                    if ((RobotStatus.batteryPower.value!! * 100).toInt() < RobotStatus.LOW_POWER_VALUE) {
//                        ToastUtil.show(getString(R.string.power_low_can_not_start_task))
//                        return@launch
//                    }
//                    when (basicSettingViewModel?.basicConfig?.guideModeVerifyPassword) {
//                        0 -> {
//                            findNavController().navigate(
//                                R.id.inputRoomNumberFragment,
//                                Bundle().apply {
//                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_GUIDE)
//                                }
//                            )
//                        }
//                        1 -> {
//                            findNavController().navigate(
//                                R.id.inputPasswordFragment,
//                                Bundle().apply {
//                                    putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_GUIDE)
//                                }
//                            )
//                        }
//                    }
//                    IdleGateDataHelper.reportIdleGateCount(0)
//                }
//            }
//        }

//        binding.buttonSend.apply {
//            setOnClickListener {
//                mainScope.launch(Dispatchers.Default) {
//                DialogHelper.loadingDialog.show()
//                if (!judgeBeforeNavigate()) {
//                    ToastUtil.show("请让机器人处于充电状态")
//                    DialogHelper.loadingDialog.dismiss()
//                    return@launch
//                }else{
//                    val chargePoint = dao.queryChargePoint()
//                    ROSHelper.setCalculateChargePose(chargePoint)
//                }
//                DialogHelper.loadingDialog.dismiss()
//                if (BillManager.billList().isNotEmpty()) {
//                    return@launch
//                }
//                if (RobotStatus.originalLocation == null) {
//                    ToastUtil.show(resources.getString(R.string.charge_point_not_set))
//                    return@launch
//                }
//                if ((RobotStatus.batteryPower.value!! * 100).toInt() < RobotStatus.LOW_POWER_VALUE) {
//                    ToastUtil.show(getString(R.string.power_low_can_not_start_task))
//                    return@launch
//                }
//                when (basicSettingViewModel?.basicConfig?.sendModeVerifyPassword) {
//                    0 -> {
//                        findNavController().navigate(
//                            R.id.putObjectFragment,
//                            Bundle().apply {
//                                putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_SEND)
//                            }
//                        )
//                    }
//                    1 -> {
//                        findNavController().navigate(
//                            R.id.inputPasswordFragment,
//                            Bundle().apply {
//                                putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_SEND)
//                            }
//                        )
//                    }
//                }
//                IdleGateDataHelper.reportIdleGateCount(0)
//            }
//            }
//        }

        binding.imageViewSetting.setOnClickListener() {
                controller!!.navigate(R.id.action_homeFragment_to_settingHomeFragment)
        }
        binding.textViewClock.apply {
            val date = Date()
            text = sdf.format(date)
        }
        binding.textViewDayOfMonth.apply {
            text = "${
                Calendar.getInstance().get(Calendar.MONTH) + 1
            }月${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}日"
        }
        binding.textViewDayOfWeek.apply {
            text = dayOfWeekChinese[Calendar.getInstance()
                .get(Calendar.DAY_OF_WEEK) - 1]
        }
//        binding.groupSendButton.apply {
//            when (basicSettingViewModel?.basicConfig?.sendModeOpen) {
//                0 -> visibility = View.GONE
//                1 -> {
//                    visibility = View.VISIBLE
//                    binding.imageViewVerifySend.apply {
//                        visibility = when (basicSettingViewModel?.basicConfig?.sendModeVerifyPassword) {
//                            0 -> View.GONE
//                            1 -> View.VISIBLE
//                            else -> View.GONE
//                        }
//                    }
//                }
//                else -> visibility = View.GONE
//            }
//        }
//        binding.groupGuideButton.apply {
//            when (basicSettingViewModel?.basicConfig?.guideModeOpen) {
//                0 -> visibility = View.GONE
//                1 -> {
//                    visibility =  View.VISIBLE
//                    binding.imageViewVerifyGuide.apply {
//                        visibility = when (basicSettingViewModel?.basicConfig?.guideModeVerifyPassword) {
//                            0 -> View.GONE
//                            1 -> View.VISIBLE
//                            else -> View.GONE
//                        }
//                    }
//                }
//                else -> visibility = View.GONE
//            }
//        }
        dateViewModel.date.observe(viewLifecycleOwner) {
            binding.textViewClock.apply {
                text = it
            }
            binding.textViewDayOfMonth.apply {
                text = "${
                    Calendar.getInstance().get(Calendar.MONTH) + 1
                }月${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}日"
            }
            binding.textViewDayOfWeek.apply {
                text = dayOfWeekChinese[Calendar.getInstance()
                    .get(Calendar.DAY_OF_WEEK) - 1]
            }
        }
        //启动定位
//        mLocationClient?.startLocation()
    }


    //当有两个Item的时候
    private fun initView1() {
        //item1
        view1_1_0!!.setBackgroundResource(calculateColor(rescolors!![0]))
        view1_1_1!!.setImageResource(calculateImage(rescolors!![0]))
        view1_1_2!!.text = rescolors!![0]
        view1_1_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![0]
            )
        }
        view1_1_3!!.text = calculateEnglish(rescolors!![0])
        //item2
        view1_2_0!!.setBackgroundResource(calculateColor(rescolors!![1]))
        view1_2_1!!.setImageResource(calculateImage(rescolors!![1]))
        view1_2_2!!.text = rescolors!![1]
        view1_2_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![1]
            )
        }
        view1_2_3!!.text = calculateEnglish(rescolors!![1])
    }

    //当有三个Item的时候
    private fun initView2() {
        //item1
        view2_1_0!!.setBackgroundResource(calculateColor(rescolors!![0]))
        view2_1_1!!.setImageResource(calculateImage(rescolors!![0]))
        view2_1_2!!.text = rescolors!![0]
        view2_1_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![0]
            )
        }
        view2_1_3!!.text = calculateEnglish(rescolors!![0])
        //item2
        view2_2_0!!.setBackgroundResource(calculateColor(rescolors!![1]))
        view2_2_1!!.setImageResource(calculateImage(rescolors!![1]))
        view2_2_2!!.text = rescolors!![1]
        view2_2_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![1]
            )
        }
        view2_2_3!!.text = calculateEnglish(rescolors!![1])
        //item3
        view2_3_0!!.setBackgroundResource(calculateColor(rescolors!![2]))
        view2_3_1!!.setImageResource(calculateImage(rescolors!![2]))
        view2_3_2!!.text = rescolors!![2]
        view2_3_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![2]
            )
        }
        view2_3_3!!.text = calculateEnglish(rescolors!![2])
    }

    //当有四个Item的时候
    private fun initView3() {
        //item1
        view3_1_0!!.setBackgroundResource(calculateColor(rescolors!![0]))
        view3_1_1!!.setImageResource(calculateImage(rescolors!![0]))
        view3_1_2!!.text = rescolors!![0]
        view3_1_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![0]
            )
        }
        view3_1_3!!.text = calculateEnglish(rescolors!![0])
        //item2
        view3_2_0!!.setBackgroundResource(calculateColor(rescolors!![1]))
        view3_2_1!!.setImageResource(calculateImage(rescolors!![1]))
        view3_2_2!!.text = rescolors!![1]
        view3_2_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![1]
            )
        }
        view3_2_3!!.text = calculateEnglish(rescolors!![1])
        //item3
        view3_3_0!!.setBackgroundResource(calculateColor(rescolors!![2]))
        view3_3_1!!.setImageResource(calculateImage(rescolors!![2]))
        view3_3_2!!.text = rescolors!![2]
        view3_3_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![2]
            )
        }
        view3_3_3!!.text = calculateEnglish(rescolors!![2])
        //item4
        view3_4_0!!.setBackgroundResource(calculateColor(rescolors!![3]))
        view3_4_1!!.setImageResource(calculateImage(rescolors!![3]))
        view3_4_2!!.text = rescolors!![3]
        view3_4_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![3]
            )
        }
        view3_4_3!!.text = calculateEnglish(rescolors!![3])
    }

    //当有五个Item的时候
    private fun initView4() {
        //item1
        view4_1_0!!.setBackgroundResource(calculateColor(rescolors!![0]))
        view4_1_1!!.setImageResource(calculateImage(rescolors!![0]))
        view4_1_2!!.text = rescolors!![0]
        view4_1_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![0]
            )
        }
        view4_1_3!!.text = calculateEnglish(rescolors!![0])
        //item2
        view4_2_0!!.setBackgroundResource(calculateColor(rescolors!![1]))
        view4_2_1!!.setImageResource(calculateImage(rescolors!![1]))
        view4_2_2!!.text = rescolors!![1]
        view4_2_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![1]
            )
        }
        view4_2_3!!.text = calculateEnglish(rescolors!![1])
        //item3
        view4_3_0!!.setBackgroundResource(calculateColor(rescolors!![2]))
        view4_3_1!!.setImageResource(calculateImage(rescolors!![2]))
        view4_3_2!!.text = rescolors!![2]
        view4_3_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![2]
            )
        }
        view4_3_3!!.text = calculateEnglish(rescolors!![2])
        //item4
        view4_4_0!!.setBackgroundResource(calculateColor(rescolors!![3]))
        view4_4_1!!.setImageResource(calculateImage(rescolors!![3]))
        view4_4_2!!.text = rescolors!![3]
        view4_4_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![3]
            )
        }
        view4_4_3!!.text = calculateEnglish(rescolors!![3])
        //item5
        view4_5_0!!.setBackgroundResource(calculateColor(rescolors!![4]))
        view4_5_1!!.setImageResource(calculateImage(rescolors!![4]))
        view4_5_2!!.text = rescolors!![4]
        view4_5_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![4]
            )
        }
        view4_5_3!!.text = calculateEnglish(rescolors!![4])
    }

    //当选中所有Item的时候
    private fun initView5() {
        //item1
        view5_1_0!!.setBackgroundResource(calculateColor(rescolors!![0]))
        view5_1_1!!.setImageResource(calculateImage(rescolors!![0]))
        view5_1_2!!.text = rescolors!![0]
        view5_1_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![0]
            )
        }
        view5_1_3!!.text = calculateEnglish(rescolors!![0])
        //item2
        view5_2_0!!.setBackgroundResource(calculateColor(rescolors!![1]))
        view5_2_1!!.setImageResource(calculateImage(rescolors!![1]))
        view5_2_2!!.text = rescolors!![1]
        view5_2_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![1]
            )
        }
        view5_2_3!!.text = calculateEnglish(rescolors!![1])
        //item3
        view5_3_0!!.setBackgroundResource(calculateColor(rescolors!![2]))
        view5_3_1!!.setImageResource(calculateImage(rescolors!![2]))
        view5_3_2!!.text = rescolors!![2]
        view5_3_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![2]
            )
        }
        view5_3_3!!.text = calculateEnglish(rescolors!![2])
        //item4
        view5_4_0!!.setBackgroundResource(calculateColor(rescolors!![3]))
        view5_4_1!!.setImageResource(calculateImage(rescolors!![3]))
        view5_4_2!!.text = rescolors!![3]
        view5_4_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![3]
            )
        }
        view5_4_3!!.text = calculateEnglish(rescolors!![3])
        //item5
        view5_5_0!!.setBackgroundResource(calculateColor(rescolors!![4]))
        view5_5_1!!.setImageResource(calculateImage(rescolors!![4]))
        view5_5_2!!.text = rescolors!![4]
        view5_5_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![4]
            )
        }
        view5_5_3!!.text = calculateEnglish(rescolors!![4])
        //item6
        view5_6_0!!.setBackgroundResource(calculateColor(rescolors!![5]))
        view5_6_1!!.setImageResource(calculateImage(rescolors!![5]))
        view5_6_2!!.text = rescolors!![5]
        view5_6_0!!.setOnClickListener { view: View? ->
            itemOnclickListen(
                rescolors!![5]
            )
        }
        view5_6_3!!.text = calculateEnglish(rescolors!![5])
    }

    override fun onResume() {
        super.onResume()
        val sp = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        val selectItem = sp.getString("SelectItem", "") // 获取存储在文件中的数据
        if (selectItem != null) {
            rescolors = selectItem.split(" ").toTypedArray()
            when (rescolors!!.size-1) {
                0 -> {
                    allInvisible()
                }
                1 -> allInvisible()
                2 -> {
                    allInvisible()
                    view1!!.visibility = View.VISIBLE
                    initView1()
                }
                3 -> {
                    allInvisible()
                    view2!!.visibility = View.VISIBLE
                    initView2()
                }
                4 -> {
                    allInvisible()
                    view3!!.visibility = View.VISIBLE
                    initView3()
                }
                5 -> {
                    allInvisible()
                    view4!!.visibility = View.VISIBLE
                    initView4()
                }
                6 -> {
                    allInvisible()
                    view5!!.visibility = View.VISIBLE
                    initView5()
                }
                else -> {}
            }
        }
    }

    /**
     * 不同标签的点击事件
     */
    fun itemOnclickListen(Onclick: String?) {
        when (Onclick) {
            "礼仪迎宾" -> Toast.makeText(context, "礼仪迎宾", Toast.LENGTH_SHORT).show()
            "智能引领" -> {
//                controller!!.navigate(R.id.action_smartFoodDeliveryFragment_to_guideFragment)
                Log.d(TAG, "点击智能引领 ")
            }
            "智能讲解" -> {
//                controller!!.navigate(R.id.action_smartFoodDeliveryFragment_to_explainFragment)
                Log.d(TAG, "点击智能讲解 ")
            }
            "轻应用" -> {
//                controller!!.navigate(R.id.action_smartFoodDeliveryFragment_to_lightApplicationFragment)
                //跳转到测温模式
                controller!!.navigate(R.id.action_homeFragment_to_cameraPreviewFragment)
                Log.d(TAG, "点击轻应用")
            }
            "智能问答" -> Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()
            "功能模块" -> Toast.makeText(context, "功能模块", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allInvisible() {
        view1!!.visibility = View.GONE
        view2!!.visibility = View.GONE
        view3!!.visibility = View.GONE
        view4!!.visibility = View.GONE
        view5!!.visibility = View.GONE
    }

    /**
     * 不同标签不同英文
     */
    fun calculateEnglish(String: String): String? {
        var str = "English"
        if ("礼仪迎宾" == String) {
            str = "GREET GUESTS"
        } else if ("智能引领" == String) {
            str = "GUIDACE"
        } else if ("智能讲解" == String) {
            str = "COMMENTARY"
        } else if ("智能问答" == String) {
            str = "Q & A"
        } else if ("轻应用" == String) {
            str = "APPLICATION"
        } else if ("功能模块" == String) {
            str = "MODULE"
        }
        return str
    }

    /**
     * 不同标签背景
     */
    fun calculateColor(colorstr: String): Int {
        var color = R.color.colorAccent
        if ("礼仪迎宾" == colorstr) {
            color = R.drawable.item3
        } else if ("智能引领" == colorstr) {
            color = R.drawable.item2
        } else if ("智能讲解" == colorstr) {
            color = R.drawable.item1
        } else if ("智能问答" == colorstr) {
            color = R.drawable.item4
        } else if ("轻应用" == colorstr) {
            color = R.drawable.item3
        } else if ("功能模块" == colorstr) {
            color = R.drawable.item1
        }
        return color
    }

    /**
     * 不同标签的图片显示
     */
    fun calculateImage(Imagestr: String): Int {
        var image: Int = R.drawable.leadership
        if ("礼仪迎宾" == Imagestr) {
            image = R.drawable.qa
        } else if ("智能引领" == Imagestr) {
            image = R.drawable.leadership
        } else if ("智能讲解" == Imagestr) {
            image = R.drawable.explain
        } else if ("智能问答" == Imagestr) {
            image = R.drawable.qa
        } else if ("轻应用" == Imagestr) {
            image = R.drawable.application
        } else if ("功能模块" == Imagestr) {
            image = R.drawable.explain
        }
        return image
    }
    /**
     * @describe 按照数据库的值做基础设置
     */
    private fun resetConfig() {
        //设置亮度
        BasicSettingHelper.setBrightness(
            requireActivity(),
            basicSettingViewModel?.basicConfig?.brightness!!.toInt()
        )
    }

    /**
     * 判断剩余天数
     */
    @SuppressLint("StringFormatMatches")
    private fun judgeDays(days: Int){
        when {
            days <= 0 -> {
                // 到期
                ExpireDeadlineDialog(requireContext()).show()
            }
            days in 0 .. 5 || days == 10 -> {
                // 1-5天到期提醒
                val content = String.format(resources.getString(R.string.deadline_leave_days,"$days"))
                val target = "$days"
                val startOffset = content.indexOf(target)
                val spannableStringBuilder = CommonHelper.getTipsSpan(
                    startOffset = startOffset,
                    string1 = target,
                    string2 = content,
                    color = ContextCompat.getColor(requireContext(), R.color.color_FF8282),
                    times = 1.6f
                )
                CloseDeadlineDialog(requireContext(), spannableStringBuilder).show()
            }
        }
    }

    /**
     * @description 判断是否满足条件出发
     */
    private suspend fun judgeBeforeNavigate(): Boolean{
        val resultCode: Int
        withContext(Dispatchers.Default){
            resultCode = if(RobotStatus.chargeStatus.value == true){
                1
            }else{
                val chargePoint = dao.queryChargePoint()
                ROSHelper.judgeBeforeNavigate(
                    labelMapName = chargePoint?.subPath?:"",
                    odomPose = RobotStatus.odomPose,
                    chargePoint = chargePoint
                )
            }
        }
        return resultCode == 1
    }
}
