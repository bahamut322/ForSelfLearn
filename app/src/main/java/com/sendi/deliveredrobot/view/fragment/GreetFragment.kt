package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGreetBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.view.widget.FaceRecognition


class GreetFragment : Fragment() {

    private lateinit var binding: FragmentGreetBinding
    private val fastRecognition: FaceRecognition = FaceRecognition()


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
    }

    override fun onStop() {
        //释放人脸识别资源
        fastRecognition.onDestroy()
        super.onStop()
    }
}