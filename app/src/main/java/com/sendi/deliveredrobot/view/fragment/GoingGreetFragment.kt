package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGoingGreetBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoUsherPointTaskBill
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import com.sendi.deliveredrobot.viewmodel.GreetViewModel

/**
 * @Author Swn
 * @Data 2024/1/16
 * @describe 迎宾前往中
 */
class GoingGreetFragment : Fragment() {

    private lateinit var binding : FragmentGoingGreetBinding
    private var viewModel: GreetViewModel? = null


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

        viewModel = ViewModelProvider(this).get(GreetViewModel::class.java)

        val bill = BillManager.currentBill()
        if (bill is GoUsherPointTaskBill) {
            var pointName = bill.endTarget()
            pointName = pointName.toList().joinToString(" ")
            binding.goingPoint.text = String.format(getString(R.string.business_going), pointName)
        }
        BaiduTTSHelper.getInstance().speaks(QuerySql.selectGreetConfig().firstPrompt.replace("%唤醒词%", QuerySql.robotConfig().wakeUpWord))

        viewModel!!.greetBigScreenModel(QuerySql.selectGreetConfig()?.bigScreenConfig)
    }

}