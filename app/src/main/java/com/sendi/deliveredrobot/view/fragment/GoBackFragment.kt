package com.sendi.deliveredrobot.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import chassis_msgs.SafeState
import com.bumptech.glide.Glide
import com.iflytek.vtncaetest.engine.EngineConstants
import com.iflytek.vtncaetest.engine.WakeupEngine
import com.iflytek.vtncaetest.engine.WakeupListener
import com.iflytek.vtncaetest.utils.CopyAssetsUtils
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO_HOME
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.databinding.FragmentGoBackBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoUsherPointBillFactory
import com.sendi.deliveredrobot.navigationtask.GoUsherPointTaskBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.topic.SafeStateTopic
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.FaceRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class GoBackFragment : BaseFragment() {
    private lateinit var binding: FragmentGoBackBinding
    private lateinit var seconds: MutableLiveData<Int>
    private lateinit var mainScope: CoroutineScope
    private val totalSize = 6
    private val columnMaxButtonSize = 2
    private val viewHeight = 432
    private val viewWidth = 336
    private var timer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())
    val res = QuerySql.QueryBasic().defaultValue.split(" ").toTypedArray()
    private var remainSeconds = REMAIN_DEFAULT
        set(value) = if(value < 0){
            mainScope.launch{
                withContext(Dispatchers.Default){
                    if(RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_PAUSE){
                        val result = ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
                        if(result){
                            withContext(Dispatchers.Main){
                                showGroupGoBacking()
                            }
                        }else{
                            ToastUtil.show("恢复失败")
                            field = REMAIN_DEFAULT
                        }
                    }else{
                        ToastUtil.show("恢复失败")
                        field = REMAIN_DEFAULT
                    }
                }
            }
            field = value
        }else{
            field = value
        }
    private var practicalSize = 1
        set(value) = if(value > totalSize) {
            field = 1
        } else {
            field = value
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_go_back, container, false)
    }

    override fun onStop() {
        super.onStop()
        SafeStateTopic.resetSafeStateListener()
        mainScope.cancel()
    }

    override fun onBaseResume() {
        // 空实现
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainScope = MainScope()
        binding = DataBindingUtil.bind(view)!!
        seconds = MutableLiveData(30)
        try {
            if(RobotStatus.pointItemIndex > -1){
                val myResultModel = QuerySql.queryPointDate(RobotStatus.selectRouteMapItemId)[RobotStatus.pointItemIndex]
                if (myResultModel.touch_type == 4) {
                    binding.goBackTv.visibility = View.GONE
                    binding.imageViewGoBack.apply {
                        Glide.with(this)
                            .asGif()
                            .load(myResultModel.touch_overTaskPic)
                            .placeholder(R.drawable.ic_warming) // 设置默认图片
                            .into(this)
                    }
                }
            }
            Log.d("TAG", "返回查询: ${QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_type},${Universal.guideTask}")
            if (QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_type == 4 && Universal.guideTask) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(QuerySql.selectGuideFouConfig().touchScreenConfig?.touch_overTaskPic)
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
            if (QuerySql.SelectActionData(
                    QuerySql.robotConfig().mapName,
                    Universal.businessTask,
                    Universal.shoppingType
                ).touchScreenConfig?.touch_type == 4 && Universal.businessTask.isNullOrEmpty()
            ) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(
                            QuerySql.SelectActionData(
                                QuerySql.robotConfig().mapName,
                                Universal.businessTask,
                                Universal.shoppingType
                            ).touchScreenConfig?.touch_overTaskPic
                        )
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }
            if (QuerySql.selectGreetConfig().touchScreenConfig?.touch_type == 4 && BillManager.currentBill() is GoUsherPointTaskBill) {
                binding.goBackTv.visibility = View.GONE
                binding.imageViewGoBack.apply {
                    Glide.with(this)
                        .asGif()
                        .load(
                            QuerySql.selectGreetConfig().touchScreenConfig?.touch_overTaskPic
                        )
                        .placeholder(R.drawable.ic_warming) // 设置默认图片
                        .into(this)
                }
            }

        } catch (_: Exception) {
        }

        binding.constraintLayoutPause.setOnClickListener {
            calculateButtons(practicalSize++)
        }
        binding.imageViewGoBack.let { imageView ->
            imageView.setOnClickListener {
                imageView.isEnabled = false
                mainScope.launch {
                    withContext(Dispatchers.Default){
                        if(RobotStatus.manageStatus != RobotCommand.MANAGE_STATUS_CONTINUE){
                            val result = ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                            if(!result){
                                initWakeUpEngine()
                                withContext(Dispatchers.Main){
                                    showGroupGoBackPause()
                                }
                            }else{
                                ToastUtil.show("暂停失败")
                            }
                        }else{
                            ToastUtil.show("暂停失败")
                        }
                        imageView.isEnabled = true
                    }
                }
            }
        }
        binding.textViewReturn.let {textView ->
            textView.setOnClickListener {
                textView.isEnabled = false
                mainScope.launch{
                    withContext(Dispatchers.Default){
                        if(RobotStatus.manageStatus != RobotCommand.MANAGE_STATUS_PAUSE){
                            val result = ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
                            if(!result){
                                destroyWakeEngine()
                                withContext(Dispatchers.Main){
                                    showGroupGoBacking()
                                }
                            }else{
                                ToastUtil.show("恢复失败")
                            }
                        }else{
                            ToastUtil.show("恢复失败")
                        }
                        textView.isEnabled = true
                    }
                }
            }
        }
        SafeStateTopic.setSafeStateListener { safeState ->
            if (safeState.safeState == SafeState.STATE_IS_TRIGGING) {
                LogUtil.d("急停按下")
                when(RobotStatus.manageStatus){
                    RobotCommand.MANAGE_STATUS_CONTINUE -> {
                        mainScope.launch(Dispatchers.Default) {
                            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
                        }
                    }
                }
            }
            if (safeState.safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                LogUtil.d("急停抬起")
                when(RobotStatus.manageStatus){
                    RobotCommand.MANAGE_STATUS_PAUSE -> {
                        if (binding.imageViewGoBack.visibility == View.VISIBLE){
                            mainScope.launch(Dispatchers.Default) {
                                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quitFragment()
        SafeStateTopic.resetSafeStateListener()
    }

    /**
     * @description 计算功能按钮的排列
     *              规则:1.一列最多显示columnMaxButtonSize个按钮
     *                  2.最大按钮容量为totalSize，实际按钮总个数为practicalSize，大按钮个数为bigButtonSize，小按钮个数为smallButtonSize
     *                  3.按钮由左向右、由上往下排列，优先插入大按钮
     *                  4.如果totalSize % practicalSize != 0, 则bigButtonSize = totalSize % practicalSize，smallButtonSize = practicalSize - bigButtonSize
     *                  5.如果totalSize % practicalSize == 0, 则判断practicalSize > (totalSize / columnMaxButtonSize)，如果为真，则smallButtonSize = practicalSize，bigButtonSize = 0;如果为假，则bigButtonSize = practicalSize,smallButtonSize = 0
     * @return Pair<bigButtonSize, smallButtonSize>
     */
    private fun calculateButtonPosition(
        totalSize: Int,
        practicalSize: Int,
        columnMaxButtonSize: Int): Pair<Int, Int> {
        val bigButtonSize: Int
        val smallButtonSize: Int
        if(totalSize % practicalSize != 0) {
            bigButtonSize = totalSize % practicalSize
            smallButtonSize = practicalSize - bigButtonSize
        } else {
            if(practicalSize > (totalSize / columnMaxButtonSize)) {
                smallButtonSize = practicalSize
                bigButtonSize = 0
            } else {
                bigButtonSize = practicalSize
                smallButtonSize = 0
            }
        }
        return Pair(bigButtonSize, smallButtonSize)
    }

    /**
     * @description 根据bigButtonSize和smallButtonSize排列按钮
     */
    private fun arrangeButtonPosition(pair: Pair<Int, Int>, columnMaxButtonSize: Int): ArrayList<View>{
        val views = ArrayList<View>()
        val bigButtonSize = pair.first
        val smallButtonSize = pair.second
        val smallButtonWrapperSize = smallButtonSize / columnMaxButtonSize
        binding.linearLayoutButtons.apply {
            removeAllViews()
            layoutParams = layoutParams.apply {
                width = (bigButtonSize + smallButtonWrapperSize) * (viewWidth + (bigButtonSize + smallButtonWrapperSize - 1) * 32)
            }
            for(i in 0 until bigButtonSize) {
                //添加大按钮
                addBigButton(this, views)
            }
            for(i in 0 until smallButtonWrapperSize) {
                //添加小按钮Wrapper
                addSmallButtonWrapper(this, columnMaxButtonSize, views)
            }
        }
        return views
    }

    private fun addBigButton(viewGroup: ViewGroup, views: ArrayList<View>) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_big_button, viewGroup, false)
        viewGroup.addView(view)
        views.add(view)
    }

    private fun addSmallButtonWrapper(viewGroup: ViewGroup, columnMaxButtonSize: Int = 2, views: ArrayList<View>) {
        val linearLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, viewHeight).apply {
                setMargins(16,0,16,0)
            }
            orientation = LinearLayout.VERTICAL
        }
        for(i in 0 until columnMaxButtonSize) {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_small_button, viewGroup, false)
            linearLayout.addView(view)
            views.add(view)
        }
        viewGroup.addView(linearLayout)
    }

    private fun calculateAndSetViewParams(inputStr: String, view:View){
        view.apply {
            setBackgroundResource(calculateColor(inputStr))
            findViewById<View>(R.id.view_icon).setBackgroundResource(calculateImage(inputStr))
            findViewById<TextView>(R.id.text_view_main_title).text = inputStr
            findViewById<TextView>(R.id.text_view_sub_title).text = calculateEnglish(inputStr)
            setOnClickListener {
//                ToastUtil.show(inputStr)
                when (val id = calculateNavigateId(inputStr)) {
                    GO_USHER_ID -> {
                        executeGoUsherBill()
                    }
                    null -> {}
                    else -> {
                        findNavController().navigate(id)
                    }
                }
            }
        }
    }

    private fun calculateEnglish(inputStr: String): String {
        return when (inputStr) {
            "智能引领" -> {
                "GUIDANCE"
            }
            "智能讲解" -> {
                "COMMENTARY"
            }
            "智能问答" -> {
                "Q & A"
            }
            "更多服务" -> {
                "APPLICATION"
            }
            "礼仪迎宾" -> {
                "GREET GUESTS"
            }
            "业务办理" -> {
                "BUSINESS"
            }
            else -> {
                "ENGLISH"
            }
        }
    }

    /**
     * 不同标签背景
     */
    private fun calculateColor(inputStr: String): Int {

        return when (inputStr) {
            "智能引领" -> {
                R.drawable.item2
            }
            "智能讲解" -> {
                R.drawable.item1
            }
            "智能问答" -> {
                R.drawable.item4
            }
            "更多服务" -> {
                R.drawable.item3
            }
            "礼仪迎宾" -> {
                R.drawable.item3
            }
            "业务办理" -> {
                R.drawable.item1
            }
            else -> {
                R.drawable.item2
            }
        }
    }

    /**
     * 不同标签的图片显示
     */
    private fun calculateImage(inputStr: String): Int {
        return when (inputStr) {
            "智能引领" -> {
                R.drawable.leadership_svg
            }
            "智能讲解" -> {
                R.drawable.explain_svg
            }
            "智能问答" -> {
                R.drawable.qa_svg
            }
            "更多服务" -> {
                R.drawable.application_svg
            }
            "礼仪迎宾" -> {
                R.drawable.welcome_svg
            }
            "业务办理" -> {
                R.drawable.business_svg
            }else -> {
                R.drawable.leadership_svg
            }
        }
    }

    private fun calculateNavigateId(inputStr: String): Int?{
        return when (inputStr) {
            "智能引领" -> {
                R.id.GuideFragment
            }
            "智能讲解" -> {
                R.id.ExplanationFragment
            }
            "智能问答" -> {
                R.id.conversationFragment
            }
            "更多服务" -> {
                R.id.appContentFragment
            }
            "礼仪迎宾" -> {
                GO_USHER_ID
            }
            "业务办理" -> {
                R.id.businessFragment
            }else -> {
                null
            }
        }
    }

    private fun showGroupGoBackPause(){
        binding.groupGoBacking.visibility = View.GONE
        binding.groupGoBackPause.visibility = View.VISIBLE
        timer = Timer()
        timer?.schedule(object : TimerTask(){
            override fun run() {
                if (RobotStatus.stopButtonPressed.value == RobotCommand.STOP_BUTTON_PRESSED) return
                val spannableString = CommonHelper.getTimeSpan(remainSeconds--, 1.6f)
                handler.post {
                    binding.textViewSeconds.text = spannableString
                }
            }
        }, Date(), 1000L)
    }

    private fun showGroupGoBacking(){
        binding.groupGoBacking.visibility = View.VISIBLE
        binding.groupGoBackPause.visibility = View.GONE
        timer?.cancel()
        remainSeconds = 30
    }

    private suspend fun initWakeUpEngine(){
        wakeupListener = WakeupListener { angle, beam, score, keyWord ->
            LogUtil.i("angle:$angle,beam:$beam,score:$score,keyWord:$keyWord")
            quitFragment()
            timer?.cancel()
            findNavController().navigate(R.id.conversationFragment)
        }
        DialogHelper.loadingDialog.show()
        withContext(Dispatchers.Default) {
            if(!isResumed) {
                DialogHelper.loadingDialog.dismiss()
                return@withContext
            }
            CopyAssetsUtils.portingFile(MyApplication.context)
            initSDK()
            startRecord()
            DialogHelper.loadingDialog.dismiss()
        }
    }

    private suspend fun destroyWakeEngine(){
        withContext(Dispatchers.Default){
            DialogHelper.loadingDialog.show()
            LogUtil.i("destroyWakeEngine")
            FaceRecognition.onDestroy()
            if (EngineConstants.isRecording) {
                stopRecord()
            }
            if (recorder != null) {
                recorder!!.destroyRecord()
                recorder = null
            }
            if (wakeupListener != null) {
                wakeupListener = null
            }
            WakeupEngine.destroy()
            LogUtil.i("destroyWakeEngine is Done!")
            DialogHelper.loadingDialog.dismiss()
        }
    }

    private fun calculateButtons(practicalSize: Int) {
        val views = arrangeButtonPosition(
            calculateButtonPosition(
                totalSize = totalSize,
                practicalSize = practicalSize,
                columnMaxButtonSize = columnMaxButtonSize
            ), columnMaxButtonSize = columnMaxButtonSize
        )
        for ((index, s) in res.withIndex()) {
            if (views.size > index) {
                views[index].apply {
                    calculateAndSetViewParams(s, this)
                }
            }
        }
    }

    private fun executeGoUsherBill(){
        mainScope.launch(Dispatchers.Default) {
            BillManager.currentBill()?.earlyFinish()
            BillManager.clearBill()
            val bill = GoUsherPointBillFactory.createBill(
                TaskModel(
                    location = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao().selectGreetPoint(QuerySql.selectGreetConfig().greetPoint)
                )
            )
            BillManager.addAllAtIndex(bill)
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
        }
    }

    companion object {
        private const val REMAIN_DEFAULT = 30
        private const val GO_USHER_ID = -1
    }
}