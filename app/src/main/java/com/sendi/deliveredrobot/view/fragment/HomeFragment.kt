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
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.*
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.*
import com.sendi.deliveredrobot.navigationtask.RobotStatus.PassWordToSetting
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.AppUtils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.MainPresenter
import com.sendi.deliveredrobot.view.inputfilter.IMainView
import com.sendi.deliveredrobot.view.widget.CloseDeadlineDialog
import com.sendi.deliveredrobot.view.widget.ExpireDeadlineDialog
import com.sendi.deliveredrobot.view.widget.FaceRecognition
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.*
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


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

    //    private val faceViewModel: FaceViewModel? by viewModels({ requireActivity() })
    private val viewModelSetting by viewModels<SettingViewModel>({ requireActivity() })

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm")
    private val dayOfWeekChinese =
        arrayOf("星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private var controller: NavController? = null
    private var fromeSettingDialog: FromeSettingDialog? = null
    private var shoppingName = ""
    private val fastRecognition: FaceRecognition = FaceRecognition()
    private val queryBasic = QuerySql.QueryBasic()

    override fun onResume() {
        mPresenter?.startTipsTimer()
        super.onResume()
        LogUtil.i("homefragment onResume")
        BaseVoiceRecorder.getInstance()?.recordCallback = { conversation, pinyinString,_ ->
            LogUtil.i("听到--->$conversation")
            if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
                LogUtil.i("包含${WakeupWordHelper.wakeupWord}")
                controller?.navigate(R.id.conversationFragment)
            }
        }
        BaseVoiceRecorder.getInstance()?.talkingCallback = { talking ->
            when (talking) {
                true -> {
//                    println("****talking")
                }

                false -> {
//                    println("not talking")
                }
            }
        }
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
        LogUtil.i("当前在主页面${QuerySql.robotConfig().sleepTime}分钟无操作->待机页面")
        fromeSettingDialog!!.dismiss()
        if (QuerySql.robotConfig().sleep == 1) {
            try {
                controller!!.navigate(R.id.action_homeFragment_to_standbyFragment)
            } catch (_: Exception) {
            }
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
        fastRecognition.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.checkPermission(activity, 0)
        fromeSettingDialog = FromeSettingDialog(context)
        RobotStatus.sdScreenStatus?.postValue(0)
        controller = Navigation.findNavController(requireView())
        RobotStatus.shoppingConfigList!!.observe(viewLifecycleOwner) {
            showFunction(true)
        }
        binding.sloganName.text = QuerySql.robotConfig().slogan
        LogUtil.d("当前待机时间：" + QuerySql.robotConfig().sleepTime)
        //通过观察者模式观察弹窗触摸
        RobotStatus.onTouch.observe(viewLifecycleOwner) {
            if (RobotStatus.onTouch.value == true) {
                mPresenter?.endTipsTimer()
            } else {
                mPresenter?.startTipsTimer()
            }
        }

        if (queryBasic.etiquette || queryBasic.identifyVip) {
            fastRecognition.suerFaceInit(
                extractFeature = queryBasic.identifyVip,
                surfaceView = binding.SurfaceView,
                owner = this,
                needEtiquette = queryBasic.etiquette,
            )
            val backgroundRes =
                if (queryBasic.defaultValue != "") R.drawable.guests_open_bg else R.drawable.once_guests_bg
            binding.homeClay.setBackgroundResource(backgroundRes)
        }
        showFunction()

        binding.imageViewSetting.setOnClickListener {
            //密码弹窗
            fromeSettingDialog!!.show()
            //弹窗点击事件。回到主页面
            PassWordToSetting.observe(viewLifecycleOwner) {
                if (PassWordToSetting.value == true) {
                    fromeSettingDialog!!.dismiss()
                    try {
                        controller!!.navigate(R.id.action_homeFragment_to_planSettingFragment)
                    } catch (_: Exception) {

                    }
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
        binding.textView61.apply {
            setOnClickListener {
                controller?.navigate(R.id.conversationFragment)
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
        binding.include.view112.text = itemName(rescolors!![0])
        binding.include.view110.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view113.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view120.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view121.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view122.text = itemName(rescolors!![1])
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
        binding.include.view212.text = itemName(rescolors!![0])
        binding.include.view210.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view213.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view220.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view221.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view222.text = itemName(rescolors!![1])
        binding.include.view220.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view223.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view230.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view231.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view232.text = itemName(rescolors!![2])
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
        binding.include.view312.text = itemName(rescolors!![0])
        binding.include.view310.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view313.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view320.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view321.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view322.text = itemName(rescolors!![1])
        binding.include.view320.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view323.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view330.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view331.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view332.text = itemName(rescolors!![2])
        binding.include.view330.setOnClickListener {
            itemOnclickListen(
                rescolors!![2]
            )
        }
        binding.include.view333.text = calculateEnglish(rescolors!![2])
        //item4
        binding.include.view340.setBackgroundResource(calculateColor(rescolors!![3]))
        binding.include.view341.setImageResource(calculateImage(rescolors!![3]))
        binding.include.view342.text = itemName(rescolors!![3])
        binding.include.view340.setOnClickListener {
            itemOnclickListen(
                rescolors!![3]
            )
        }
        binding.include.view343.text = calculateEnglish(rescolors!![3])
    }

    //当有五个Item的时候
    private fun initView4() {
        //item1
        binding.include.view410.setBackgroundResource(calculateColor(rescolors!![0]))
        binding.include.view411.setImageResource(calculateImage(rescolors!![0]))
        binding.include.view412.text = itemName(rescolors!![0])
        binding.include.view410.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view413.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view420.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view421.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view422.text = itemName(rescolors!![1])
        binding.include.view420.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view423.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view430.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view431.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view432.text = itemName(rescolors!![2])
        binding.include.view430.setOnClickListener {
            itemOnclickListen(
                rescolors!![2]
            )
        }
        binding.include.view433.text = calculateEnglish(rescolors!![2])
        //item4
        binding.include.view440.setBackgroundResource(calculateColor(rescolors!![3]))
        binding.include.view441.setImageResource(calculateImage(rescolors!![3]))
        binding.include.view442.text = itemName(rescolors!![3])
        binding.include.view440.setOnClickListener {
            itemOnclickListen(
                rescolors!![3]
            )
        }
        binding.include.view443.text = calculateEnglish(rescolors!![3])
        //item5
        binding.include.view450.setBackgroundResource(calculateColor(rescolors!![4]))
        binding.include.view451.setImageResource(calculateImage(rescolors!![4]))
        binding.include.view452.text = itemName(rescolors!![4])
        binding.include.view450.setOnClickListener {
            itemOnclickListen(
                rescolors!![4]
            )
        }
        binding.include.view453.text = calculateEnglish(rescolors!![4])
    }

    //当选中所有Item的时候
    private fun initView5() {
        //item1
        binding.include.view510.setBackgroundResource(calculateColor(rescolors!![0]))
        binding.include.view511.setImageResource(calculateImage(rescolors!![0]))
        binding.include.view512.text = itemName(rescolors!![0])
        binding.include.view510.setOnClickListener {
            itemOnclickListen(
                rescolors!![0]
            )
        }
        binding.include.view513.text = calculateEnglish(rescolors!![0])
        //item2
        binding.include.view520.setBackgroundResource(calculateColor(rescolors!![1]))
        binding.include.view521.setImageResource(calculateImage(rescolors!![1]))
        binding.include.view522.text = itemName(rescolors!![1])
        binding.include.view520.setOnClickListener {
            itemOnclickListen(
                rescolors!![1]
            )
        }
        binding.include.view523.text = calculateEnglish(rescolors!![1])
        //item3
        binding.include.view530.setBackgroundResource(calculateColor(rescolors!![2]))
        binding.include.view531.setImageResource(calculateImage(rescolors!![2]))
        binding.include.view532.text = itemName(rescolors!![2])
        binding.include.view530.setOnClickListener {
            itemOnclickListen(
                rescolors!![2]
            )
        }
        binding.include.view533.text = calculateEnglish(rescolors!![2])
        //item4
        binding.include.view540.setBackgroundResource(calculateColor(rescolors!![3]))
        binding.include.view541.setImageResource(calculateImage(rescolors!![3]))
        binding.include.view542.text = itemName(rescolors!![3])
        binding.include.view540.setOnClickListener {
            itemOnclickListen(
                rescolors!![3]
            )
        }
        binding.include.view543.text = calculateEnglish(rescolors!![3])
        //item5
        binding.include.view550.setBackgroundResource(calculateColor(rescolors!![4]))
        binding.include.view551.setImageResource(calculateImage(rescolors!![4]))
        binding.include.view552.text = itemName(rescolors!![4])
        binding.include.view550.setOnClickListener {
            itemOnclickListen(
                rescolors!![4]
            )
        }
        binding.include.view553.text = calculateEnglish(rescolors!![4])
        //item6
        binding.include.view560.setBackgroundResource(calculateColor(rescolors!![5]))
        binding.include.view561.setImageResource(calculateImage(rescolors!![5]))
        binding.include.view562.text = itemName(rescolors!![5])
        binding.include.view560.setOnClickListener {
            itemOnclickListen(
                rescolors!![5]
            )
        }
        binding.include.view563.text = calculateEnglish(rescolors!![5])
    }


    private fun showFunction(renew: Boolean = false) {
        if (queryBasic.defaultValue != null) {
            shoppingName = if (renew) {
                RobotStatus.shoppingConfigList!!.value!!.name!!
            } else {
                QuerySql.ShoppingConfig().name!!
            }
            rescolors = queryBasic.defaultValue.split(" ").toTypedArray()
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

                5 -> {
                    allInvisible()
                    binding.include.view4.visibility = View.VISIBLE
                    initView4()
                }

                6 -> {
                    allInvisible()
                    binding.include.view5.visibility = View.VISIBLE
                    initView5()
                }

                else -> {}
            }
        }
        RobotStatus.robotConfig?.observe(viewLifecycleOwner) {
            binding.textView61.text = String.format(getString(R.string.ask), it.wakeUpWord)
            binding.sloganName.text = it.slogan
        }
    }

    /**
     * 不同标签的点击事件
     */
    private fun itemOnclickListen(Onclick: String?) {
        when (Onclick) {
            "智能引领" -> {
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    controller!!.navigate(R.id.action_homeFragment_to_guideFragment)
                }
            }

            "智能讲解" -> {
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    controller!!.navigate(R.id.action_homeFragment_to_explanationFragment)
                    Log.d("TAG", "点击智能讲解 ")
                    SpeakHelper.speak(QuerySql.QueryExplainConfig().routeListText)
                }
            }

            "更多服务" -> {
                //跳转到测温模式
                Toast.makeText(context, "更多服务", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "点击更多服务")
                controller?.navigate(R.id.appContentFragment)
            }

            "智能问答" -> Toast.makeText(context, "智能问答", Toast.LENGTH_SHORT).show()

            "业务办理" -> {
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    controller!!.navigate(R.id.action_homeFragment_to_businessFragment)
                    Log.d("TAG", "业务办理 ")
                }
            }

            "礼仪迎宾" -> {
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    thread {
                        val bill = GoUsherPointBillFactory.createBill(
                            TaskModel(
                                location = dao.selectGreetPoint(QuerySql.selectGreetConfig().greetPoint)
                            )
                        )
                        BillManager.addAllAtIndex(bill)
                        BillManager.currentBill()?.executeNextTask()
                    }
                }
            }
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
        } else if ("更多服务" == String) {
            str = "APPLICATION"
        } else if ("礼仪迎宾" == String) {
            str = "GRRRT GURSTS"
        } else if ("业务办理" == String) {
            str = "BUSINESS"
        }
        return str
    }

    private fun itemName(ItemName: String): String {
        return if (ItemName == "业务办理") {
            shoppingName
        } else {
            ItemName
        }
    }

    /**
     * 不同标签背景
     */
    private fun calculateColor(colorstr: String): Int {
        var color = R.drawable.item2
        if ("智能引领" == colorstr) {
            color = R.drawable.item2
        } else if ("智能讲解" == colorstr) {
            color = R.drawable.item1
        } else if ("智能问答" == colorstr) {
            color = R.drawable.item4
        } else if ("更多服务" == colorstr) {
            color = R.drawable.item3
        } else if ("礼仪迎宾" == colorstr) {
            color = R.drawable.item3
        } else if ("业务办理" == colorstr) {
            color = R.drawable.item1
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
        } else if ("更多服务" == Imagestr) {
            image = R.drawable.application_svg
        } else if ("礼仪迎宾" == Imagestr) {
            image = R.drawable.welcome_svg
        } else if ("业务办理" == Imagestr) {
            image = R.drawable.business_svg
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
