package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentHomeBinding
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus.PassWordToSetting
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.AppUtils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.MainPresenter
import com.sendi.deliveredrobot.view.inputfilter.IMainView
import com.sendi.deliveredrobot.view.widget.CloseDeadlineDialog
import com.sendi.deliveredrobot.view.widget.ExpireDeadlineDialog
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @describe 主页面
 */
class HomeFragment : Fragment(), IMainView {
    private lateinit var binding: FragmentHomeBinding
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val viewModelBin1 by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
    private val viewModelBin2 by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
    private val dateViewModel by viewModels<DateViewModel>({ requireActivity() })
    private lateinit var mainScope: CoroutineScope
    private var localDays: Int = Int.MIN_VALUE //记录上一次获取的使用剩余天数
    private var remindDialog: Dialog? = null
    private var rescolors: Array<String>? = null
    private var mPresenter: MainPresenter? = null
    private val faceViewModel: FaceViewModel? by viewModels({ requireActivity() })

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm")
    private val dayOfWeekChinese = arrayOf("星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private var controller: NavController? = null
    private var fromeSettingDialog: FromeSettingDialog? = null


    override fun onResume() {
        //启动默认开始计时
        mPresenter?.startTipsTimer()
        super.onResume()
    }

    override fun onPause() {
        //有其他操作时结束计时
        mPresenter?.endTipsTimer()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mPresenter = MainPresenter(this)
        super.onCreate(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun showTipsView() {
        println("无操作")
        fromeSettingDialog!!.dismiss()
        if (QuerySql.robotConfig().sleep == 1){
            controller!!.navigate(R.id.action_homeFragment_to_standbyFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = DataBindingUtil.bind(view)!!
        // 将myTouchListener注册到分发列表
        (this.activity as MainActivity?)!!.registerMyTouchListener(myTouchListener)
        return view
    }

    //监听屏幕触摸事件
    private val myTouchListener: MainActivity.MyTouchListener =
        object : MainActivity.MyTouchListener {
            override fun onTouchEvent(event: MotionEvent?) {
                mPresenter?.startTipsTimer()
            }
        }

    /**取消触摸监听以及屏幕计时*/
    override fun onDestroyView() {
        (this.activity as MainActivity?)!!.unRegisterMyTouchListener(myTouchListener)
        mPresenter?.endTipsTimer()
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        mainScope = MainScope()
        resetConfig()
        //弹出送物任务提示窗
        val message = when {
            (!viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished)) && (!viewModelBin2.previousTaskFinished || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished)) -> resources.getString(
                R.string.bin_1_bin_2_not_take
            )
            !viewModelBin1.previousTaskFinished || (viewModelBin1.previousRemoteOrderPutFinished && !viewModelBin1.previousRemoteOrderSendFinished) -> resources.getString(
                R.string.bin_1_not_take
            )
            !viewModelBin2.previousTaskFinished || (viewModelBin2.previousRemoteOrderPutFinished && !viewModelBin2.previousRemoteOrderSendFinished) -> resources.getString(
                R.string.bin_2_not_take
            )
            else -> ""
        }
        if (!TextUtils.isEmpty(message)) {
            remindDialog = DialogHelper.getRemindDialog(message).apply { show() }
        }
        //查询使用期限
//        CloudMqttService.publish(RequestTenancyModel().toString())
        RobotStatus.tenancy.observe(viewLifecycleOwner) {
            basicSettingViewModel?.basicConfig?.robotUseDeadLine = it.deadline
            MainScope().launch(Dispatchers.Default) {
                dao.updateBasicConfig(basicSettingViewModel?.basicConfig!!)
            }
            when (it.useType) {
                1, 2 -> {
                    //租赁
                    if (it.days == localDays) return@observe
                    localDays = it.days ?: 0
                    judgeDays(it.days ?: 0)
                }
            }
        }
        IdleGateDataHelper.reportIdleGateCount()
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
        remindDialog?.dismiss()
        //释放人脸识别资源
        faceViewModel?.onDestroy()

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.checkPermission(activity, 0)
        fromeSettingDialog = FromeSettingDialog(context)
        RobotStatus.sdScreenStatus?.postValue(0)
        controller = Navigation.findNavController(view)
        LogUtil.i("待机时间：" + QuerySql.robotConfig().sleepTime)
        //通过观察者模式观察弹窗触摸
        RobotStatus.onTouch.observe(viewLifecycleOwner) {
            if (RobotStatus.onTouch.value == true) {
                mPresenter?.endTipsTimer()
            } else {
                mPresenter?.startTipsTimer()
            }
        }

//        val b = Vbar()
//        binding.imageView2.setOnClickListener {
//            thread {
//                b.vbarOpen()
//            }
//            val t = object : Thread() {
//                override fun run() {
//                    super.run()
//                    while (true) {
//                        val str = b.resultsingle
//                        if (str != null) {
//                            (object : java.lang.Runnable {
//                                override fun run() {
//                                    run {
//                                        LogUtil.i("ddasdq"+str)
//                                    }
//                                }
//                            })
//                        }
//                        try {
//                            sleep(1)
//                        } catch (e: InterruptedException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//            t.start()
//        }


        if (QuerySql.QueryBasic().identifyVip == true) {
            faceViewModel?.suerfaceInit(binding.SurfaceView)
            if (QuerySql.QueryBasic().defaultValue != "") {
                binding.homeClay.setBackgroundResource(R.drawable.guests_open_bg)
            } else {
                binding.homeClay.setBackgroundResource(R.drawable.once_guests_bg)
            }
        } else {
            binding.homeClay.setBackgroundResource(R.drawable.bg)
        }
        //改变人脸识别工具类，人脸识别数量的最大值
        if (QuerySql.QueryBasic().tempMode == 0) {
            Log.d("TAG", "当前为单人测温")
            Utils.mNmsLimit = 1
        } else {
            Log.d("TAG", "当前为多人测温")
            Utils.mNmsLimit = 10
        }
        if (QuerySql.QueryBasic().defaultValue != null) {
            rescolors = QuerySql.QueryBasic().defaultValue.split(" ").toTypedArray()
            when (rescolors!!.size - 1) {
                0 -> {
                    allInvisible()
                }
                1 -> allInvisible()
                2 -> {
                    allInvisible()
                    binding.include.view1.visibility = View.VISIBLE
                    initView1()
                }
                3 -> {
                    allInvisible()
                    binding.include.view2.visibility = View.VISIBLE
                    initView2()
                }
                4 -> {
                    allInvisible()
                    binding.include.view3.visibility = View.VISIBLE
                    initView3()
                }
                else -> {}
            }
        }

        binding.imageViewSetting.setOnClickListener {
            //密码弹窗
            fromeSettingDialog!!.show()
            //弹窗点击事件。回到主页面
            PassWordToSetting.observe(viewLifecycleOwner) {
                if (PassWordToSetting.value == true) {
                    fromeSettingDialog!!.dismiss()
                    controller!!.navigate(R.id.action_homeFragment_to_planSettingFragment)
                    PassWordToSetting.postValue(false)
                }
            }

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
        binding.include.view110.setBackgroundResource(calculateColor(rescolors!![0]))
        binding.include.view111.setImageResource(calculateImage(rescolors!![0]))
        binding.include.view112.text = rescolors!![0]
        binding.include.view110.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view113.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view120.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view121.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view122.text = rescolors!![1]
        binding.include.view120.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view123.text = calculateEnglish(rescolors!![1])
    }

    //当有三个Item的时候
    private fun initView2() {
        //item1
        binding.include.view210.setBackgroundResource(calculateColor(rescolors!![0]))
        binding.include.view211.setImageResource(calculateImage(rescolors!![0]))
        binding.include.view212.text = rescolors!![0]
        binding.include.view210.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view213.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view220.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view221.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view222.text = rescolors!![1]
        binding.include.view220.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view223.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view230.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view231.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view232.text = rescolors!![2]
        binding.include.view230.setOnClickListener {
            itemOnclickListen(
                rescolors!![2]
            )
        }
        binding.include.view233.text = calculateEnglish(rescolors!![2])
    }

    //当有四个Item的时候
    private fun initView3() {
        //item1
        binding.include.view310.setBackgroundResource(calculateColor(rescolors!![0]))
        binding.include.view311.setImageResource(calculateImage(rescolors!![0]))
        binding.include.view312.text = rescolors!![0]
        binding.include.view310.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view313.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view320.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view321.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view322.text = rescolors!![1]
        binding.include.view320.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view323.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view330.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view331.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view332.text = rescolors!![2]
        binding.include.view330.setOnClickListener {
            itemOnclickListen(
                rescolors!![2]
            )
        }
        binding.include.view333.text = calculateEnglish(rescolors!![2])
        //item4
        binding.include.view340.setBackgroundResource(calculateColor(rescolors!![3]))
        binding.include.view341.setImageResource(calculateImage(rescolors!![3]))
        binding.include.view342.text = rescolors!![3]
        binding.include.view340.setOnClickListener {
            itemOnclickListen(
                rescolors!![3]
            )
        }
        binding.include.view343.text = calculateEnglish(rescolors!![3])
    }


    /**
     * 不同标签的点击事件
     */
    private fun itemOnclickListen(Onclick: String?) {
        when (Onclick) {
//            "礼仪迎宾" -> Toast.makeText(context, "礼仪迎宾", Toast.LENGTH_SHORT).show()
            "智能引领" -> {
                controller!!.navigate(R.id.action_homeFragment_to_guideFragment)
            }
            "智能讲解" -> {
                controller!!.navigate(R.id.action_homeFragment_to_explanationFragment)
                Log.d("TAG", "点击智能讲解 ")
                    SpeakHelper.speak(QuerySql.QueryExplainConfig().routeListText)
            }
            "轻应用" -> {
                //跳转到测温模式
//                controller!!.navigate(R.id.action_homeFragment_to_cameraPreviewFragment)
                Log.d("TAG", "点击轻应用")
            }
            "智能问答" -> Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()
//            "功能模块" -> Toast.makeText(context, "功能模块", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allInvisible() {
        binding.include.view1.visibility = View.GONE
        binding.include.view2.visibility = View.GONE
        binding.include.view3.visibility = View.GONE
        binding.include.view4.visibility = View.GONE
        binding.include.view5.visibility = View.GONE
    }

    /**
     * 标签下的英文
     */
    private fun calculateEnglish(String: String): String {
        var str = "English"
        if ("智能引领" == String) {
            str = "GUIDANCE"
        } else if ("智能讲解" == String) {
            str = "COMMENTARY"
        } else if ("智能问答" == String) {
            str = "Q & A"
        } else if ("轻应用" == String) {
            str = "APPLICATION"
        }
        return str
    }

    /**
     * 不同标签背景
     */
    private fun calculateColor(colorstr: String): Int {
        var color = R.color.colorAccent
        if ("智能引领" == colorstr) {
            color = R.drawable.item2
        } else if ("智能讲解" == colorstr) {
            color = R.drawable.item1
        } else if ("智能问答" == colorstr) {
            color = R.drawable.item4
        } else if ("轻应用" == colorstr) {
            color = R.drawable.item3
        }
        return color
    }

    /**
     * 不同标签的图片显示
     */
    private fun calculateImage(Imagestr: String): Int {
        var image: Int = R.drawable.leadership_svg
        if ("智能引领" == Imagestr) {
            image = R.drawable.leadership_svg
        } else if ("智能讲解" == Imagestr) {
            image = R.drawable.explain_svg
        } else if ("智能问答" == Imagestr) {
            image = R.drawable.qa_svg
        } else if ("轻应用" == Imagestr) {
            image = R.drawable.application_svg
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
    private fun judgeDays(days: Int) {
        when {
            days <= 0 -> {
                // 到期
                ExpireDeadlineDialog(requireContext()).show()
            }
            days in 0..5 || days == 10 -> {
                // 1-5天到期提醒
                val content =
                    String.format(resources.getString(R.string.deadline_leave_days, "$days"))
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
    private suspend fun judgeBeforeNavigate(): Boolean {
        val resultCode: Int
        withContext(Dispatchers.Default) {
            resultCode = if (RobotStatus.chargeStatus.value == true) {
                1
            } else {
                val chargePoint = dao.queryChargePoint()
                ROSHelper.judgeBeforeNavigate(
                    labelMapName = chargePoint?.subPath ?: "",
                    odomPose = RobotStatus.odomPose,
                    chargePoint = chargePoint
                )
            }
        }
        return resultCode == 1
    }
}
