package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.alibaba.fastjson.JSONObject
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentBusinessingBinding
import com.sendi.deliveredrobot.entity.ShoppingActionDB
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueues
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.Advance
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog
import com.sendi.deliveredrobot.view.widget.Order
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog
import com.sendi.deliveredrobot.view.widget.Stat
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理前往中
 */
class BusinessIngFragment : Fragment() {
    private lateinit var binding: FragmentBusinessingBinding
    private lateinit var mainScope: CoroutineScope
    private var controller: NavController? = null
    private var viewModel: BusinessViewModel? = null
    var pointName: String = RobotStatus.shoppingName
    private var baseViewModel: BaseViewModel? = null
    private var actionData: ShoppingActionDB? = ShoppingActionDB()
    private val mediatorLiveData =
        MediatorLiveData<Pair<Int, Int>>()//储存两个Int类型的值来观察，监听多个LiveData源的变化
    private var processClickDialog: ProcessClickDialog? = null
    private var finishTaskDialog: FinishTaskDialog? = null
    private val liveData1 = RobotStatus.ArrayPointExplan
    private val liveData2 = RobotStatus.progress
    private var layoutParams: ConstraintLayout.LayoutParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskConsumer =
            Consumer { task: String ->
                // 执行任务的代码
                if (BuildConfig.IS_SPEAK) {
                    BaiduTTSHelper.getInstance().speaks(task, "explanation")
                }
                LogUtil.i("${actionData?.name} 的Task: $task")
            }
        // 创建TaskQueue实例
        Universal.taskQueue = TaskQueues(taskConsumer)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_businessing, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!

        viewModel = ViewModelProvider(this).get(BusinessViewModel::class.java)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)

        processClickDialog = ProcessClickDialog(requireActivity())
        finishTaskDialog = FinishTaskDialog(requireActivity())
        processClickDialog?.setCountdownTime(QuerySql.QueryBasic().businessWhetherTime)//打断任务时间

        status()
        actionData = QuerySql.SelectActionData(Universal.MapName, pointName,RobotStatus.shoppingType)
        LogUtil.e("数据内容：${JSONObject.toJSONString(actionData)}")


        controller = Navigation.findNavController(requireView())
        //副屏显示
        viewModel!!.secondBusinessScreenModel(actionData)
        //小屏显示
        try {


        layoutThis(
            actionData?.touchScreenConfig!!.touch_picPlayTime,
            actionData?.touchScreenConfig!!.touch_imageFile ?: "",
            actionData?.touchScreenConfig!!.touch_type,
            actionData?.touchScreenConfig!!.touch_textPosition,
            actionData?.touchScreenConfig!!.touch_fontLayout,
            actionData?.touchScreenConfig?.touch_fontContent ?: "",
            actionData?.touchScreenConfig!!.touch_fontBackGround ?: "",
            actionData?.touchScreenConfig!!.touch_fontColor ?: "",
            actionData?.touchScreenConfig!!.touch_fontSize,
            actionData?.touchScreenConfig!!.touch_picType
        )
        }catch (_:Exception){

        }
        lifecycleScope.launch {
            delay(2000L) // 延迟1秒

            if (actionData?.actionType == 2) {
                RobotStatus.repeatedReading++
                //添加任务
                if (RobotStatus.repeatedReading % 2 == 0) {
                    viewModel!!.splitTextByPunctuation(actionData?.moveText!!)
                }
            } else {
                viewModel!!.splitTextByPunctuation(actionData?.standText)
            }
            binding.businessName.text = String.format(getString(R.string.business_going), actionData!!.name)
        }

        // 添加第一个源（到点）
        mediatorLiveData.addSource(liveData1) { value1 ->
            val value2 = liveData2.value
            if (value1 != null && value2 != null) {
                mediatorLiveData.value = Pair(value1, value2)
            }
        }
        // 添加第二个源（进度）
        mediatorLiveData.addSource(liveData2) { value2 ->
            val value1 = liveData1.value
            if (value1 != null && value2 != null) {
                mediatorLiveData.value = Pair(value1, value2)
            }
        }
        viewModel!!.downTimer(
            actionData?.waitingTime!!,
            actionData?.actionType!!,
            controller!!
        )
        mediatorLiveData.observe(viewLifecycleOwner) { (value1, value2) ->
            //非定点任务
            when (actionData?.actionType) {
                2 -> {
                    if (value2 == Universal.ExplainLength && value1 == 1 && !viewModel!!.hasArrive) {
                        viewModel!!.hasArrive = true
                        Order.setFlage("0")
                        LogUtil.i("到点，并任务执行完毕")
                        arriveSpeak(actionData?.arriveText!!)
                    } else if (actionData?.moveText.isNullOrEmpty() && value1 == 1 && !viewModel!!.hasArrive) {
                        viewModel!!.hasArrive = true
                        LogUtil.i("到点，并任务执行完毕")
                        arriveSpeak(actionData?.arriveText!!)
                    }
                }
            }
        }
        //定点任务观察
        RobotStatus.progress.observe(viewLifecycleOwner) {
            //定点任务
            when (actionData?.actionType) {
                1 -> {
                    LogUtil.i("day:${viewModel!!.hasArrive},${Universal.ExplainLength},${it}")
                    if (it == Universal.ExplainLength && !viewModel!!.hasArrive) {
                        LogUtil.i("任务执行完毕")
                        //定点任务完成倒计时
                        viewModel!!.countDownTimer!!.startCountDown()
                    }
                }
            }
        }

        //暂停
        binding.pauseCon.setOnClickListener {
            if (QuerySql.QueryBasic().businessInterrupt)
                processClickDialog?.show()
            pause()
        }
    }

    private fun status() {
        Stat.setOnChangeListener {
            if (Stat.getFlage() == 2) {
                //暂停
                Universal.taskQueue.pause()
                MediaPlayerHelper.getInstance().pause()
                BaiduTTSHelper.getInstance().pause()
            } else if (Stat.getFlage() == 3) {
                //继续
                Universal.taskQueue.resume()
                MediaPlayerHelper.getInstance().resume()
                BaiduTTSHelper.getInstance().resume()
            }
        }
    }

    /**
     * 到点任务
     */
    private fun arriveSpeak(arriveText: String?) {
        if (!viewModel!!.hasArrive) {
            return
        }
        viewModel!!.splitTextByPunctuation(arriveText!!)

        if (arriveText.isEmpty() && viewModel!!.hasArrive) {
            LogUtil.i("到点，并任务执行完毕")
            viewModel!!.countDownTimer!!.startCountDown()
        }
        RobotStatus.progress.observe(viewLifecycleOwner) {
            if (it == Universal.ExplainLength && viewModel!!.hasArrive) {
                Order.setFlage("0")
                LogUtil.i("到点，并任务执行完毕")
                viewModel!!.countDownTimer!!.startCountDown()

            }
        }

    }

    //暂停
    private fun pause() {
        processClickDialog?.otherBtn?.visibility = View.GONE //切换其他任务
        processClickDialog?.nextBtn?.visibility = View.GONE //下一个任务
        processClickDialog?.finishBtn?.setOnClickListener {
            secondRecognition()
        }
    }

    //二次确认
    private fun secondRecognition() {
        finishTaskDialog?.show()
        finishTaskDialog?.YesExit?.setOnClickListener {
            when (actionData!!.actionType) {
                2 -> {
                    processClickDialog?.dismiss()
                    finishTaskDialog?.dismiss()
                    //返回
                    viewModel!!.finishTask()
                }

                1 -> {
                    viewModel!!.pageJump(controller!!)
                }
            }
        }
        finishTaskDialog?.NoExit?.setOnClickListener { finishTaskDialog?.dismiss() }
    }

    /**
     * @param picPlayTime    轮播时间
     * @param file           路径
     * @param type           类型： 1-图片 2-视频 6-文字 7-图片+文字
     * @param textPosition   文字x位置
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     * @param picType        图片样式
     */
    private fun layoutThis(
        picPlayTime: Int?,
        file: String?,
        type: Int?,
        textPosition: Int?,
        fontLayout: Int?,
        fontContent: String?,
        fontBackGround: String?,
        fontColor: String?,
        fontSize: Int?,
        picType: Int?
    ) {
        when (type) {
            1, 2 -> {
                //读取文件
                getFilesAllName(file, picType!!, picPlayTime!!)
                binding.verticalTV.visibility = View.GONE
                binding.horizontalTV.visibility = View.GONE
                binding.pointImage.visibility = View.VISIBLE
            }

            6 -> {
                binding.pointImage.visibility = View.GONE
                layoutParams = binding.verticalTV.layoutParams as ConstraintLayout.LayoutParams
                when (textPosition) {
                    0 -> {
                        binding.horizontalTV.gravity = Gravity.CENTER //居中
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    1 -> {
                        binding.horizontalTV.gravity = Gravity.TOP //居上
                        layoutParams!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        binding.verticalTV.layoutParams = layoutParams
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    2 -> {
                        binding.horizontalTV.gravity = Gravity.BOTTOM //居下
                        layoutParams!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        binding.verticalTV.layoutParams = layoutParams
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }
                }
            }

            7 -> {
                //读取文件
                getFilesAllName(file, picType!!, picPlayTime!!)
                layoutParams = binding.verticalTV.layoutParams as ConstraintLayout.LayoutParams
                when (textPosition) {
                    0 -> {
                        binding.horizontalTV.gravity = Gravity.CENTER //居中
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    1 -> {
                        binding.horizontalTV.gravity = Gravity.TOP //居上
                        layoutParams!!.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        binding.verticalTV.layoutParams = layoutParams
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }

                    2 -> {
                        binding.horizontalTV.gravity = Gravity.BOTTOM //居下
                        layoutParams!!.topToTop = ConstraintLayout.LayoutParams.UNSET
                        binding.verticalTV.layoutParams = layoutParams
                        textLayoutThis(
                            fontLayout!!,
                            fontContent!!,
                            fontBackGround!!,
                            fontColor!!,
                            fontSize!!
                        )
                    }
                }
                binding.pointImage.visibility = View.VISIBLE
            }
        }
    }

    /**
     * @param fontLayout     文字方向：1-横向，2-纵向
     * @param fontContent    文字
     * @param fontBackGround 背景颜色
     * @param fontColor      文字颜色
     * @param fontSize       文字大小：1-大，2-中，3-小,
     */
    private fun textLayoutThis(
        fontLayout: Int,
        fontContent: String,
        fontBackGround: String,
        fontColor: String,
        fontSize: Int
    ) {

        //横向
        if (fontLayout == 1) {
            //隐藏纵向文字，显示横向文字
            binding.verticalTV.visibility = View.GONE
            binding.horizontalTV.visibility = View.VISIBLE
            //显示内容
            binding.horizontalTV.text = baseViewModel!!.getLength(fontContent)
            //背景颜色&图片
            binding.horizontalTV.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            //文字颜色
            binding.horizontalTV.setTextColor(Color.parseColor(fontColor + ""))
            //字体大小
            when (fontSize) {
                1 -> {
                    binding.horizontalTV.textSize = 30F
                }

                2 -> {
                    binding.horizontalTV.textSize = 20F
                }

                3 -> {
                    binding.horizontalTV.textSize = 10F
                }
            }
        } else {
            //纵向
            //隐藏横向文字，显示纵向文字
            binding.verticalTV.visibility = View.VISIBLE
            binding.horizontalTV.visibility = View.GONE
            //显示内容
            binding.verticalTV.text = fontContent
            //背景颜色
            binding.verticalTV.setBackgroundColor(Color.parseColor(fontBackGround + ""))
            //文字颜色
            binding.verticalTV.textColor = Color.parseColor(fontColor + "")
            //字体大小
            when (fontSize) {
                1 -> {
                    binding.verticalTV.textSize = 30
                }

                2 -> {
                    binding.verticalTV.textSize = 20
                }

                3 -> {
                    binding.verticalTV.textSize = 10
                }
            }
        }
    }

    private fun getFilesAllName(path: String?, picType: Int, picPlayTime: Int) {
        try {
            val file = File(path!!)
            if (file.isFile) {
                // This is a file
                val fileList: MutableList<Advance> = ArrayList()
                if (BaseViewModel.checkIsImageFile(file.path)) {
                    fileList.add(Advance(file.path, "2", picType, picPlayTime)) // image
                } else {
                    fileList.add(Advance(file.path, "1", 1, picPlayTime)) // video
                }
                binding.pointImage.setData(fileList)
            } else if (file.isDirectory) {
                // This is a directory
                val files = file.listFiles()
                if (files != null) {
                    val fileList: MutableList<Advance> = ArrayList()
                    for (value in files) {
                        if (BaseViewModel.checkIsImageFile(value.path)) {
                            fileList.add(Advance(value.path, "2", picType, picPlayTime)) // image
                        } else {
                            fileList.add(Advance(value.path, "1", 1, picPlayTime)) // video
                        }
                    }
                    binding.pointImage.setData(fileList)
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "轮播数据读取异常: $e")
        }
    }
}