package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentBusinessingBinding
import com.sendi.deliveredrobot.entity.Table_Shopping_Action
import com.sendi.deliveredrobot.entity.TouchScreenShow
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.PlaceholderEnum
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog
import com.sendi.deliveredrobot.view.widget.MediaStatusManager
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog
import com.sendi.deliveredrobot.view.widget.Stat
import com.sendi.deliveredrobot.view.widget.TaskArray
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理前往中
 */
class BusinessIngFragment : Fragment() {
    private lateinit var binding: FragmentBusinessingBinding
    private var controller: NavController? = null
    private var viewModel: BusinessViewModel? = null
    private var baseViewModel: BaseViewModel? = null
    private var actionData: Table_Shopping_Action? = Table_Shopping_Action()
    private val mediatorLiveData =
        MediatorLiveData<Pair<Int, Int>>()//储存两个Int类型的值来观察，监听多个LiveData源的变化
    private var processClickDialog: ProcessClickDialog? = null
    private var finishTaskDialog: FinishTaskDialog? = null
    private val arrayPoint = RobotStatus.arrayPointExplain
    private val progress = RobotStatus.progress
    private var taskId = ""

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
        LogUtil.i("打断任务时间：" + QuerySql.QueryBasic().businessWhetherTime)
        arguments?.let {
            taskId = it.getString("taskId")!!
        }
        status()
        actionData = QuerySql.SelectActionData(
            QuerySql.robotConfig().mapName,
            Universal.shoppingName,
            Universal.shoppingType
        )
        LogUtil.d("数据内容：${JSONObject.toJSONString(actionData)}")


        TaskArray.setOnChangeListener {
            if (TaskArray.getToDo() == "3" && !processClickDialog!!.isShowing) {
                viewModel!!.countDownTimer!!.pause()
            }
            if (TaskArray.getToDo() == "6" && !processClickDialog!!.isShowing) {
                viewModel!!.countDownTimer!!.resume()
            }
        }

        controller = Navigation.findNavController(requireView())
        //副屏显示
        viewModel!!.secondBusinessScreenModel(actionData)
        //小屏显示
        try {
            //正常图片&文字
            TouchScreenShow().layoutThis(
                binding.bgCon,
                binding.include.verticalTV,
                binding.include.horizontalTV,
                binding.include.pointImage,
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
            //表情组（不可点击 单独处理）；tmd现在PM又变卦了，可以暂停了
            if (actionData?.touchScreenConfig?.touch_type == 4) {
                Glide.with(this)
                    .asGif()
                    .load(actionData?.touchScreenConfig!!.touch_walkPic)
                    .placeholder(R.drawable.ic_warming) // 设置默认图片
                    .into(binding.argPic)
            }
        } catch (_: Exception) {

        }
        lifecycleScope.launch {
            delay(1000L) // 延迟1秒

            if (actionData?.actionType == 2) {
                //添加任务
                BaiduTTSHelper.getInstance().speaks(
                    PlaceholderEnum.replaceText(
                        actionData?.moveText!!,
                        pointName = actionData?.pointName!!,
                        business = actionData?.name!!
                    )
                )
//                    viewModel!!.splitTextByPunctuation(actionData?.moveText!!)
                binding.businessName.text =
                    String.format(getString(R.string.business_going), Universal.shoppingName)
                Universal.businessTask = actionData!!.name
            } else {
                BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.replaceText(actionData?.standText!!,pointName = actionData?.pointName!!, business = actionData?.name!!))
//                viewModel!!.splitTextByPunctuation(actionData?.standText)
                binding.businessName.text =
                    String.format(getString(R.string.business_doing), Universal.shoppingName)
                //上报定点任务执行中
                ReportDataHelper.reportTaskDto(
                    TaskModel(endTarget = "定点导购", taskId = taskId),
                    TaskStageEnum.BusinessIngTask,
                    UpdateReturn.taskDto()
                )
            }
        }

        binding.bottomAlarmTextViewArrive.text = actionData!!.name

        // 添加第一个源（到点）
        mediatorLiveData.addSource(arrayPoint) { arrayPoint ->
            val value2 = progress.value
            if (arrayPoint != null && value2 != null) {
                mediatorLiveData.value = Pair(arrayPoint, value2)
            }
        }
        // 添加第二个源（进度）
        mediatorLiveData.addSource(progress) { progress ->
            val value1 = arrayPoint.value
            if (value1 != null && progress != null) {
                mediatorLiveData.value = Pair(value1, progress)
            }
        }
        viewModel!!.downTimer(
            actionData?.waitingTime!!,
            actionData?.actionType!!,
            controller!!,
            taskId
        )
        mediatorLiveData.observe(viewLifecycleOwner) { (arrayPoint, progress) ->
            //非定点任务
            when (actionData?.actionType) {
                2 -> {
                    if (progress == Universal.explainTextLength && arrayPoint == 1 && !viewModel!!.hasArrive) {
                        viewModel!!.hasArrive = true
                        MediaStatusManager.stopMediaPlay(false)
                        LogUtil.i("到点，并任务执行完毕")
                        RobotStatus.progress.value = 0
                        arriveSpeak(actionData?.arriveText!!)
                    } else if (actionData?.moveText.isNullOrEmpty() && arrayPoint == 1 && !viewModel!!.hasArrive) {
                        viewModel!!.hasArrive = true
                        MediaStatusManager.stopMediaPlay(false)
                        LogUtil.i("到点，并任务执行完毕")
                        RobotStatus.progress.value = 0
                        arriveSpeak(actionData?.arriveText!!)
                    } else if (progress == Universal.explainTextLength && arrayPoint != 1) {
                        LogUtil.i("未到点，但播报任务完毕")
                        MediaStatusManager.stopMediaPlay(false)
                    }
                }
            }
        }
        //定点任务观察
        RobotStatus.progress.observe(viewLifecycleOwner) {
            //定点任务
            when (actionData?.actionType) {
                1 -> {
                    LogUtil.i("day:${viewModel!!.hasArrive},${Universal.explainTextLength},${it}")
                    if (it == Universal.explainTextLength && !viewModel!!.hasArrive) {
                        LogUtil.i("定点任务执行完毕")
                        arrayPic()
                        MediaStatusManager.stopMediaPlay(false)
                        //定点任务完成倒计时
                        viewModel!!.countDownTimer!!.startCountDown()
                    }
                }
            }
        }

        //暂停
        binding.argPic.setOnClickListener {
            if (QuerySql.QueryBasic().businessInterrupt) {
                processClickDialog?.show()
                pause()
            }
        }
    }

    private fun status() {
        Stat.setOnChangeListener {
            if (Stat.getFlage() == 2) {
                //暂停
//                Universal.taskQueue.pause()
                MediaPlayerHelper.getInstance().pause()
                BaiduTTSHelper.getInstance().pause()
            } else if (Stat.getFlage() == 3) {
                //继续
//                Universal.taskQueue.resume()
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
        arrayPic()
        BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.replaceText(text = arriveText!!,pointName = actionData?.pointName!!, business = actionData?.name!!))
//        viewModel!!.splitTextByPunctuation(arriveText!!)
        if (arriveText!!.isEmpty() && viewModel!!.hasArrive) {
            LogUtil.i("到点，并任务执行完毕_返回")
            MediaStatusManager.stopMediaPlay(false)
            viewModel!!.countDownTimer!!.startCountDown()
        }
        RobotStatus.progress.observe(viewLifecycleOwner) {
            if (it == Universal.explainTextLength && viewModel!!.hasArrive) {
                LogUtil.i("到点，并任务执行完毕_返回")
                MediaStatusManager.stopMediaPlay(false)
                viewModel!!.countDownTimer!!.startCountDown()

            }
        }

    }

    private fun arrayPic() {
        //到点表情组
        if (actionData?.touchScreenConfig?.touch_type == 4) {
            Glide.with(this)
                .asGif()
                .load(actionData?.touchScreenConfig!!.touch_arrivePic)
                .placeholder(R.drawable.ic_warming) // 设置默认图片
                .into(binding.argPic)
        }
    }

    //暂停
    private fun pause() {
        processClickDialog?.otherBtn?.visibility = View.GONE //切换其他任务
        processClickDialog?.nextBtn?.visibility = View.GONE //下一个任务
        processClickDialog?.finishBtn?.text = "结束任务"
        processClickDialog?.continueBtn?.text = "继续任务"
        viewModel!!.countDownTimer!!.pause()
        processClickDialog?.finishBtn?.setOnClickListener {
            secondRecognition()
        }
        processClickDialog?.continueBtn?.setOnClickListener {
            processClickDialog?.dismiss()
            viewModel!!.countDownTimer!!.resume()
            UpdateReturn.resume()
        }
    }

    //二次确认
    private fun secondRecognition() {
        finishTaskDialog?.show()
        finishTaskDialog?.confirmBtn?.setOnClickListener {
            viewModel!!.countDownTimer!!.cancel()
            //中断导购上报
            ReportDataHelper.reportTaskDto(
                TaskModel(endTarget = "定点导购", taskId = taskId),
                TaskStageEnum.EarlyFinishBusinessTask,
                UpdateReturn.taskDto()
            )

            when (actionData!!.actionType) {
                2 -> {
                    processClickDialog?.dismiss()
                    finishTaskDialog?.dismiss()
                    if (arrayPoint.value != 1) {//如果到点点击结束
                    } else {
                        //中断提示
                        BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.replaceText(text = QuerySql.ShoppingConfig().interruptPrompt!!,pointName = actionData?.pointName!!, business = actionData?.name!!))
                    //                        viewModel!!.splitTextByPunctuation(QuerySql.ShoppingConfig().interruptPrompt!!)
                    }
                    viewModel!!.finishTask()
                }

                1 -> {
                    viewModel!!.pageJump(controller!!)
                    processClickDialog?.dismiss()
                    finishTaskDialog?.dismiss()
                    //导购结束上报
                    ReportDataHelper.reportTaskDto(
                        TaskModel(endTarget = "定点导购", taskId = taskId),
                        TaskStageEnum.FinishBusinessTask,
                        UpdateReturn.taskDto()
                    )

                }
            }
        }
        finishTaskDialog?.cancelBtn?.setOnClickListener {
            viewModel!!.countDownTimer!!.resume()
            finishTaskDialog?.dismiss()
        }
    }
}