package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGreetBinding
import com.sendi.deliveredrobot.entity.TouchScreenShow
import com.sendi.deliveredrobot.entity.Table_Greet_Config
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.view.widget.FaceRecognition

/**
 * @Author Swn
 * @Data 2024/1/16
 * @describe 迎宾到达进行
 */
class GreetFragment : Fragment() {

    private lateinit var binding: FragmentGreetBinding
    private val fastRecognition: FaceRecognition = FaceRecognition()
    private var actionData : Table_Greet_Config? = Table_Greet_Config()

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

        actionData = QuerySql.selectGreetConfig()

        RobotStatus.progress.observe(viewLifecycleOwner) {
            if (it == Universal.ExplainLength || Universal.ExplainLength != -1) {
                fastRecognition.suerFaceInit(
                    extractFeature = true,
                    surfaceView = binding.SurfaceView,
                    owner = this,
                    needEtiquette = true,
                )
            }
        }
        binding.end.apply {
            setOnClickListener {
                BillManager.currentBill()?.executeNextTask()
                BaiduTTSHelper.getInstance().speaks(QuerySql.selectGreetConfig().exitPrompt.replace("%唤醒词%", QuerySql.robotConfig().wakeUpWord))
            }
        }

        try {
            //正常图片&文字
            TouchScreenShow().layoutThis(
                binding.bgCon,
                binding.verticalTV,
                binding.horizontalTV,
                binding.pointImage,
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

    }

    override fun onStop() {
        //释放人脸识别资源
        fastRecognition.onDestroy()
        super.onStop()
    }
}