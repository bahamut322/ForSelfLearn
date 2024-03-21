package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGreetBinding
import com.sendi.deliveredrobot.entity.Table_Greet_Config
import com.sendi.deliveredrobot.entity.TouchScreenShow
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.view.widget.FaceRecognition
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog

/**
 * @Author Swn
 * @Data 2024/1/16
 * @describe 迎宾到达进行
 */
class GreetFragment : Fragment() {

    private lateinit var binding: FragmentGreetBinding
    private var actionData : Table_Greet_Config? = Table_Greet_Config()
    private var processClickDialog: ProcessClickDialog? = null
    private var finishTaskDialog: FinishTaskDialog? = null
    private var arrayFacePoint = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_greet, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding  = DataBindingUtil.bind(view)!!

        processClickDialog = ProcessClickDialog(requireActivity())
        finishTaskDialog = FinishTaskDialog(requireActivity())
        processClickDialog?.setCountdownTime(20)//打断任务时间

        actionData = QuerySql.selectGreetConfig()

        RobotStatus.progress.observe(viewLifecycleOwner) {
            if (it == Universal.ExplainLength || Universal.ExplainLength != -1) {
                Log.d("tag", "onViewCreated:  迎宾到达进行")
                if (arrayFacePoint == 0) {
                    FaceRecognition.suerFaceInit(
                        extractFeature = true,
                        needEtiquette = true,
                    )
                }
                arrayFacePoint++
            }
        }

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
        } catch (_: Exception) {}
        binding.argPic.setOnClickListener {
            processClickDialog?.show()
            pause()
        }
    }

    private fun pause() {
        processClickDialog?.otherBtn?.visibility = View.GONE //切换其他任务
        processClickDialog?.nextBtn?.visibility = View.GONE //下一个任务
        processClickDialog?.finishBtn?.text = "结束迎宾"
        processClickDialog?.continueBtn?.text = "继续迎宾"
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
            //返回
            BillManager.currentBill()?.executeNextTask()
            BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = QuerySql.selectGreetConfig().exitPrompt?:"", business = "礼仪迎宾", pointName = BillManager.currentBill()?.endTarget()?.toList()?.joinToString(" ")?:""))

        }
        finishTaskDialog?.NoExit?.setOnClickListener { finishTaskDialog?.dismiss() }
    }
    override fun onStop() {
        //释放人脸识别资源
        arrayFacePoint = 0
        FaceRecognition.onDestroy()
        super.onStop()
    }
}