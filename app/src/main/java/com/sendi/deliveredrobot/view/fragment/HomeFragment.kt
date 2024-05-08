package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.TYPE_STAND_STILL
import com.sendi.deliveredrobot.databinding.FragmentHomeBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.BasicSettingHelper
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.SecondScreenManageHelper
import com.sendi.deliveredrobot.model.LogoConfig
import com.sendi.deliveredrobot.model.PhoneConfigModel
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoUsherPointBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.passWordToSetting
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.AppUtils
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.MainPresenter
import com.sendi.deliveredrobot.view.inputfilter.IMainView
import com.sendi.deliveredrobot.view.widget.CloseDeadlineDialog
import com.sendi.deliveredrobot.view.widget.ExpireDeadlineDialog
import com.sendi.deliveredrobot.view.widget.FaceRecognition
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.view.widget.OneKeyCallPhoneDialog
import com.sendi.deliveredrobot.viewmodel.BasicSettingViewModel
import com.sendi.deliveredrobot.viewmodel.DateViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.concurrent.thread


/**
 * @describe 主页面
 */
class HomeFragment : BaseFragment(), IMainView {
    private lateinit var binding: FragmentHomeBinding
    private val basicSettingViewModel: BasicSettingViewModel? by viewModels({ requireActivity() })
    private val dateViewModel by viewModels<DateViewModel>({ requireActivity() })
    private lateinit var mainScope: CoroutineScope
    private var localDays: Int = Int.MIN_VALUE //记录上一次获取的使用剩余天数
    private var remindDialog: Dialog? = null
    private var rescolors: Array<String>? = null
    private var mPresenter: MainPresenter? = null

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("HH:mm")
    private val dayOfWeekChinese =
        arrayOf("星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private var controller: NavController? = null
    private var fromeSettingDialog: FromeSettingDialog? = null
    private var shoppingName = ""
    private val queryBasic = QuerySql.QueryBasic()
    private var oneKeyCallPhoneDialog: OneKeyCallPhoneDialog? = null
    private val robotConfig = QuerySql.robotConfig()
    private val gson = Gson()

    override fun onResume() {
        super.onResume()
        LogUtil.i("homefragment onResume")
        mPresenter?.startTipsTimer()
    }

    override fun onPause() {
        //有其他操作时结束计时
        mPresenter?.endTipsTimer()
        LogUtil.i("homefragment onPause")
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mPresenter = MainPresenter(this@HomeFragment)
        super.onCreate(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun showTipsView() {
        LogUtil.i("当前在主页面${robotConfig.sleepTime}分钟无操作->待机页面")
        fromeSettingDialog!!.dismiss()
        if (robotConfig.sleep == 1) {
            try {
                homeFragmentNavigateToFragment(R.id.action_homeFragment_to_standbyFragment)
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
        LogUtil.i("homefragment onDestroyView")
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        mainScope = MainScope()
        resetConfig()
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
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i("homefragment onStop")
        mainScope.cancel()
        remindDialog?.dismiss()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppUtils.checkPermission(activity, 0)
        fromeSettingDialog = FromeSettingDialog(context)
//        RobotStatus.sdScreenStatus.postValue(0)
        SecondScreenManageHelper.refreshSecondScreen(SecondScreenManageHelper.STATE_IDLE)
        controller = Navigation.findNavController(requireView())
        RobotStatus.shoppingConfigList!!.observe(viewLifecycleOwner) {
            showFunction(true)
        }
        binding.sloganName.text = robotConfig.slogan
        LogUtil.d("当前待机时间：" + robotConfig.sleepTime)
        //通过观察者模式观察弹窗触摸
        RobotStatus.onTouch.observe(viewLifecycleOwner) {
            if (RobotStatus.onTouch.value == true) {
                mPresenter?.endTipsTimer()
            } else {
                mPresenter?.startTipsTimer()
            }
        }

        if (queryBasic.etiquette || queryBasic.identifyVip) {
            FaceRecognition.suerFaceInit(
                extractFeature = queryBasic.identifyVip,
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
            passWordToSetting.observe(viewLifecycleOwner) {
                if (passWordToSetting.value == true) {
                    fromeSettingDialog!!.dismiss()
                    try {
                        homeFragmentNavigateToFragment(R.id.action_homeFragment_to_planSettingFragment)
                    } catch (_: Exception) {

                    }
                    passWordToSetting.postValue(false)
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
                homeFragmentNavigateToFragment(R.id.conversationFragment)
            }
        }
        binding.viewOneKeyCallForeground.apply {
            setOnClickListener {
                if (oneKeyCallPhoneDialog == null) {
                    oneKeyCallPhoneDialog = OneKeyCallPhoneDialog(requireContext())
                    val list =  gson.fromJson(robotConfig.phoneConfigJsonArray, Array<PhoneConfigModel>::class.java)
                    oneKeyCallPhoneDialog!!.setData(list.toList())
                }
                oneKeyCallPhoneDialog?.show()
            }
        }
        if (!robotConfig.logoConfigJson.isNullOrEmpty()) {
            val logoConfig = gson.fromJson(robotConfig.logoConfigJson, LogoConfig::class.java)
            handleLogoConfig(logoConfig)
        }
        RobotStatus.robotConfig?.observe(viewLifecycleOwner) { it ->
            binding.textView61.text = String.format(getString(R.string.ask), it.wakeUpWord)
            binding.sloganName.text = it.slogan ?: getString(R.string.Welcome_used)
            it.phoneConfig?.let { phoneConfig ->
                val list =  gson.fromJson(phoneConfig.toString(), Array<PhoneConfigModel>::class.java)
                oneKeyCallPhoneDialog?.setData(list.toList())
            }
            it.logoConfig?.let { logoConfig ->
                handleLogoConfig(logoConfig)
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
        if(queryBasic.oneKeyCallPhone == 1){
            binding.groupOneKeyCall.visibility = View.VISIBLE
        }else{
            binding.groupOneKeyCall.visibility = View.GONE
        }
    }

    /**
     * 不同标签的点击事件
     */
    private fun itemOnclickListen(Onclick: String?) {
        when (Onclick) {
            "智能引领" -> {
                if (RobotStatus.chargeStatus.value == false && RobotStatus.currentStatus != TYPE_STAND_STILL) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    homeFragmentNavigateToFragment(R.id.action_homeFragment_to_guideFragment)
                }
            }

            "智能讲解" -> {
                if(RobotStatus.chargeStatus.value == false && RobotStatus.currentStatus != TYPE_STAND_STILL){
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    homeFragmentNavigateToFragment(R.id.action_homeFragment_to_explanationFragment)
                    Log.d("TAG", "点击智能讲解 ")
                }
            }

            "更多服务" -> {
                //跳转到测温模式
                Toast.makeText(context, "更多服务", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "点击更多服务")
                homeFragmentNavigateToFragment(R.id.appContentFragment)
            }

            "智能问答" -> {
                homeFragmentNavigateToFragment(R.id.conversationFragment)
            }

            "业务办理" -> {
                if(RobotStatus.chargeStatus.value == false && RobotStatus.currentStatus != TYPE_STAND_STILL){
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    homeFragmentNavigateToFragment(R.id.action_homeFragment_to_businessFragment)
                    Log.d("TAG", "业务办理 ")
                }
            }

            "礼仪迎宾" -> {
                if(RobotStatus.chargeStatus.value == false && RobotStatus.currentStatus != TYPE_STAND_STILL){
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

    private fun homeFragmentNavigateToFragment(actionId: Int) {
        navigateToFragment(actionId)
    }

    private fun handleLogoConfig(logoConfig: LogoConfig){
        when (logoConfig.isOpenLogo) {
            LogoConfig.IS_OPEN_LOGO_TYPE_DEFAULT -> {
                binding.imageViewLogo.also { imageViewLogo ->
                    val drawableSDLogo =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_sendi_logo)
                    drawableSDLogo?.apply {
                        val drawableWidth = intrinsicWidth
                        val ratio = drawableWidth * 1f / intrinsicHeight
                        imageViewLogo.apply {
                            layoutParams = imageViewLogo.layoutParams.apply {
                                width = (40 * ratio).toInt()
                                height = 40
                            }
                            background = drawableSDLogo
                            visibility = View.VISIBLE
                            binding.textViewLogo.visibility = View.GONE
                        }

                    }
                }
            }

            LogoConfig.IS_OPEN_LOGO_TYPE_IMAGE -> {
                binding.imageViewLogo.also { imageViewLogo ->
                    Glide.with(imageViewLogo)
                        .load("${BuildConfig.HTTP_HOST}${logoConfig.robotLogo}")
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(
                                p0: Drawable,
                                p1: Transition<in Drawable>?
                            ) {
                                p0.apply {
                                    val drawableWidth = intrinsicWidth
                                    val ratio = drawableWidth * 1f / intrinsicHeight
                                    imageViewLogo.apply {
                                        layoutParams = imageViewLogo.layoutParams.apply {
                                            width = (40 * ratio).toInt()
                                            height = 40
                                        }
                                        visibility = View.VISIBLE
                                        binding.textViewLogo.visibility = View.GONE
                                    }
                                    imageViewLogo.background = p0
                                }
                            }
                        })
                }
            }

            LogoConfig.IS_OPEN_LOGO_TYPE_TEXT -> {
                binding.textViewLogo.apply {
                    text = logoConfig.robotCopyWriting
                    visibility = View.VISIBLE
                    binding.imageViewLogo.visibility = View.GONE
                }
            }
        }
    }
}
