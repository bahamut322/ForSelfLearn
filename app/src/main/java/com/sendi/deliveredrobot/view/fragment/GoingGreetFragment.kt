package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGoingGreetBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoUsherPointTaskBill

class GoingGreetFragment : Fragment() {

    private lateinit var binding : FragmentGoingGreetBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_going_greet, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding  = DataBindingUtil.bind(view)!!

        val bill = BillManager.currentBill()
        if (bill is GoUsherPointTaskBill) {
            var pointName = bill.endTarget()
            pointName = pointName.toList().joinToString(" ")
            binding.goingPoint.text = String.format(getString(R.string.business_going), pointName)
        }
        BaiduTTSHelper.getInstance().speaks(QuerySql.selectGreetConfig().firstPrompt.replace("%唤醒词%", QuerySql.robotConfig().wakeUpWord))
    }

    /**
     * 小屏幕
     */
    private fun touchScreen(){

    }

    /**
     * 大屏幕
     */
    private fun bigScreen(){

    }

}