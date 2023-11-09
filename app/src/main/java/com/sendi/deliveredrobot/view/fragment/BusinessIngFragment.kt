package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentBusinessingBinding
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.SpeakHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import kotlinx.coroutines.CoroutineScope

/**
 * @Author Swn
 * @Data 2023/10/23
 * @describe 业务办理前往中
 */
class BusinessIngFragment : Fragment() {
    private lateinit var binding: FragmentBusinessingBinding
    private lateinit var mainScope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_businessing, container, false)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val bill = BillManager.currentBill()
        var pointName = bill?.endTarget()
        pointName = pointName?.toList()?.joinToString(" ")
        binding.businessName.text = String.format(getString(R.string.business_going), pointName)
        SpeakHelper.speak(
            String.format(
                getString(R.string.hello_we_are_going_to_please_follow_me_1),
                pointName
            )
        )
    }
}