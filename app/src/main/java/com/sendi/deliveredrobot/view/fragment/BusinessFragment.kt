package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.BusinessAdapter
import com.sendi.deliveredrobot.databinding.FragmentBusinessBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.BusinessTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/10/18
 * @describe 业务办理页面
 */
class BusinessFragment : Fragment() {

    private lateinit var binding: FragmentBusinessBinding
    private var controller: NavController? = null
    private var queryFloorPoints: List<QueryPointEntity> = ArrayList()
    val mainScope = MainScope()
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //获取所有目标点
        queryFloorPoints = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            queryFloorPoints = dao.queryAllPoints()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        val toSettingDialog = FromeSettingDialog(context)

        if (FunctionSkip.selectFunction() == 4) {
            binding.firstFragment.visibility = View.GONE
            binding.llReturn.visibility = View.VISIBLE
        } else {
            binding.firstFragment.visibility = View.VISIBLE
            binding.llReturn.visibility = View.GONE
        }
        //返回按钮
        binding.llReturn.setOnClickListener {
            controller!!.navigate(R.id.action_businessFragment_to_homeFragment)
        }
        //设置按钮
        binding.imageViewSetting.setOnClickListener {
            toSettingDialog.show()
            RobotStatus.PassWordToSetting.observe(viewLifecycleOwner) {
                if (RobotStatus.PassWordToSetting.value == true) {
                    try {
                        controller!!.navigate(R.id.action_businessFragment_to_planSettingFragment)
                    }catch (_: Exception){}
                    toSettingDialog.dismiss()
                    RobotStatus.PassWordToSetting.postValue(false)
                }
            }
            Toast.makeText(context,"点击了：设置",Toast.LENGTH_SHORT).show()
        }

        //初始化适配器
        binding.businessGv.adapter = context?.let { BusinessAdapter(it, queryFloorPoints) }
        //item点击
        binding.businessGv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    LogUtil.i("点击了第${position}项,引领去往${queryFloorPoints[position].pointName}")
                    val endPoint = queryFloorPoints[position]
                    Log.d("TAG", "onViewCreated: "+endPoint.pointDirection)
                    val taskModel = TaskModel(location = endPoint)
                    val bill = BusinessTaskBillFactory.createBill(taskModel = taskModel)
                    BillManager.addAllAtIndex(bill, 0)
                    BillManager.currentBill()?.executeNextTask()
                }
            }

    }

}