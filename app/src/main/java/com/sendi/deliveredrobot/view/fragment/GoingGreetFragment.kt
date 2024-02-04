package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentGoingGreetBinding
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GoUsherPointTaskBill
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.service.Placeholder
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.view.widget.FinishTaskDialog
import com.sendi.deliveredrobot.view.widget.Order
import com.sendi.deliveredrobot.view.widget.ProcessClickDialog
import com.sendi.deliveredrobot.view.widget.TaskNext
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import com.sendi.deliveredrobot.viewmodel.GreetViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2024/1/16
 * @describe 迎宾前往中
 */
class GoingGreetFragment : Fragment() {

    private lateinit var binding : FragmentGoingGreetBinding
    private var viewModel: GreetViewModel? = null
    private var processClickDialog: ProcessClickDialog? = null
    private var finishTaskDialog: FinishTaskDialog? = null

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

        processClickDialog = ProcessClickDialog(requireActivity())
        finishTaskDialog = FinishTaskDialog(requireActivity())
        processClickDialog?.setCountdownTime(20)//打断任务时间

        viewModel = ViewModelProvider(this).get(GreetViewModel::class.java)

        val bill = BillManager.currentBill()
        if (bill is GoUsherPointTaskBill) {
            var pointName = bill.endTarget()
            pointName = pointName.toList().joinToString(" ")
            binding.goingPoint.text = String.format(getString(R.string.business_going), pointName)
        }
        BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = QuerySql.selectGreetConfig().firstPrompt!!, business = "礼仪迎宾", pointName = BillManager.currentBill()!!.endTarget().toList().joinToString(" ")))

        viewModel!!.greetBigScreenModel(QuerySql.selectGreetConfig()?.bigScreenConfig)
        //暂停
        binding.fragment.setOnClickListener {
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
        MainScope().launch {
            ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_PAUSE)
        }
        finishTaskDialog?.YesExit?.setOnClickListener {
            processClickDialog?.dismiss()
            finishTaskDialog?.dismiss()
            //返回
            MainScope().launch {
                for (iTaskBill in BillManager.billList()) {
                    iTaskBill.earlyFinish()
                }
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_STOP)
                TaskNext.setToDo("0")
                Order.setFlage("0")
                RobotStatus.ArrayPointExplan.postValue(0)
            }
            BaiduTTSHelper.getInstance().speaks(Placeholder.replaceText(text = QuerySql.selectGreetConfig().exitPrompt!!, business = "礼仪迎宾", pointName = BillManager.currentBill()!!.endTarget().toList().joinToString(" ")))
        }
        finishTaskDialog?.NoExit?.setOnClickListener {
            MainScope().launch {
                ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)
            }
            finishTaskDialog?.dismiss()
        }
    }

}