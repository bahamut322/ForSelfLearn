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
import com.sendi.deliveredrobot.entity.Table_Guide_Foundation
import com.sendi.deliveredrobot.entity.TouchScreenShow
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GuideTaskBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog
import com.sendi.deliveredrobot.view.widget.Order
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog
import com.sendi.deliveredrobot.view.widget.Stat
import com.sendi.deliveredrobot.viewmodel.BaseViewModel
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 引领前往中
 */
class GuidingFragment : Fragment() {
    private lateinit var binding: FragmentBusinessingBinding
    private var mainScope = CoroutineScope(Dispatchers.Default + Job())
    private var controller: NavController? = null
    private var viewModel: BusinessViewModel? = null
    private var baseViewModel: BaseViewModel? = null
    private var actionData: Table_Guide_Foundation? = Table_Guide_Foundation()
    private val mediatorLiveData =
        MediatorLiveData<Pair<Int, Int>>()//储存两个Int类型的值来观察，监听多个LiveData源的变化
    private var processClickDialog: ProcessClickDialog? = null
    private var finishTaskDialog: FinishTaskDialog? = null
    private val arrayPoint = RobotStatus.ArrayPointExplan
    private val progress = RobotStatus.progress
    var pointName = ""
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
        Universal.guideTask = true
        viewModel = ViewModelProvider(this).get(BusinessViewModel::class.java)
        baseViewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
        processClickDialog = ProcessClickDialog(requireActivity())
        finishTaskDialog = FinishTaskDialog(requireActivity())
        processClickDialog?.setCountdownTime(20)//打断任务时间

        val bill = BillManager.currentBill()
        if (bill is GuideTaskBill) {
            // 设置标志位为true，表示已经进入过该方法
            Universal.speakInt++
            pointName = bill.endTarget()
            pointName = pointName.toList().joinToString(" ")
            binding.bottomAlarmTextViewArrive.text = pointName
            binding.businessName.text = String.format(getString(R.string.business_going), pointName)
        }

        status()

        actionData = QuerySql.selectGuideFouConfig()
        LogUtil.i("数据内容：${JSONObject.toJSONString(actionData)}")

        controller = Navigation.findNavController(requireView())
        //副屏显示
        viewModel!!.secondGuideScreenModel(actionData)
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
            if (actionData?.movePrompt!!.isNotEmpty()) {
                BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = actionData?.movePrompt!!,pointName =pointName , business = "智能引领"))
            }
        }


        mediatorLiveData.addSource(arrayPoint) {
            updateMediatorValue()
        }

        mediatorLiveData.addSource(progress) {
            updateMediatorValue()
        }


        mediatorLiveData.observe(viewLifecycleOwner) { (arrayPointObserver, progressObserver) ->
            if (progressObserver == Universal.ExplainLength && arrayPointObserver == 1 && !viewModel!!.hasArrive) {
                LogUtil.i("到点，并任务执行完毕")
                RobotStatus.progress.value = 0
                Order.setFlage("0")
                RobotStatus.ready.postValue(0)
                viewModel!!.hasArrive = true
                arriveSpeak(actionData?.arrivePrompt!!)
            } else if (actionData?.movePrompt.isNullOrEmpty() && arrayPointObserver == 1 && !viewModel!!.hasArrive) {
                LogUtil.i("到点，并任务执行完毕")
                RobotStatus.progress.value = 0
                Order.setFlage("0")
                RobotStatus.ready.postValue(0)
                viewModel!!.hasArrive = true
                arriveSpeak(actionData?.arrivePrompt!!)
            } else if (progressObserver == Universal.ExplainLength && arrayPointObserver != 1) {
                LogUtil.i("未到点，但播报任务完毕")
                Order.setFlage("0")
            }
        }

        //暂停
        binding.argPic.setOnClickListener {
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

    private fun updateMediatorValue() {
        val arrayPointObserver = arrayPoint.value
        val progressObserver = progress.value
        if (arrayPointObserver != null && progressObserver != null) {
            mediatorLiveData.value = Pair(arrayPointObserver, progressObserver)
        }
    }

    /**
     * 到点任务
     */
    private fun arriveSpeak(arriveText: String?) {
        if (!viewModel!!.hasArrive) {
            return
        }
        mediatorLiveData.value = Pair(0, 0)
        //到点表情组
        if (actionData?.touchScreenConfig?.touch_type == 4) {
            Glide.with(this)
                .asGif()
                .load(actionData?.touchScreenConfig!!.touch_arrivePic)
                .placeholder(R.drawable.ic_warming) // 设置默认图片
                .into(binding.argPic)
        } else {
            binding.motionLayoutGuideArrive.visibility = View.VISIBLE
        }
        RobotStatus.progress.postValue(0)
        BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = arriveText!!,pointName =pointName , business = "智能引领"))
//        viewModel!!.splitTextByPunctuation(arriveText!!)
        if (arriveText.isEmpty() && viewModel!!.hasArrive) {
            LogUtil.i("到点，并任务执行完毕_返回")
            mainScope.launch {
                BillManager.currentBill()?.executeNextTask()
            }
            RobotStatus.progress.postValue(0)
            Order.setFlage("0")
            RobotStatus.ready.postValue(0)
            viewModel!!.hasArrive = false
        }
        RobotStatus.progress.observe(viewLifecycleOwner) {
            if (it == Universal.ExplainLength && viewModel!!.hasArrive) {
                LogUtil.i("到点，并任务执行完毕_返回")
                mainScope.launch {
                    BillManager.currentBill()?.executeNextTask()
                }
                RobotStatus.progress.postValue(0)
                Order.setFlage("0")
                RobotStatus.ready.postValue(0)
                viewModel!!.hasArrive = false
            }
        }
    }

    //暂停
    private fun pause() {
        processClickDialog?.otherBtn?.visibility = View.GONE //切换其他任务
        processClickDialog?.nextBtn?.visibility = View.GONE //下一个任务
        processClickDialog?.finishBtn?.text = "结束引领"
        processClickDialog?.continueBtn?.text = "继续引领"
        processClickDialog?.finishBtn?.setOnClickListener {
            secondRecognition()
        }
    }

    //二次确认
    private fun secondRecognition() {
        finishTaskDialog?.show()
        finishTaskDialog?.YesExit?.setOnClickListener {
            processClickDialog?.dismiss()
            finishTaskDialog?.dismiss()
            //中断提示
            if (QuerySql.selectGuideFouConfig().interruptPrompt!!.isNotEmpty()) {
                BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = QuerySql.ShoppingConfig().interruptPrompt!!,pointName =pointName , business = "智能引领"))
            }
            //返回
            viewModel!!.finishTask()
        }
        finishTaskDialog?.NoExit?.setOnClickListener { finishTaskDialog?.dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}