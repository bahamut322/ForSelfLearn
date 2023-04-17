package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.GuidePointAdapter
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentGuideBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GuideTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus.PassWordToSetting
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


/**
 * 智能引领X8
 * @author swn
 */
class GuideFragment : Fragment() {

    val mainScope = MainScope()
    private var controller: NavController? = null
    private lateinit var binding: FragmentGuideBinding
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private var queryFloorPoints: List<QueryPointEntity> = ArrayList()
    private var fromType: String? = null
    private lateinit var timer: Timer

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
        val view = inflater.inflate(R.layout.fragment_guide, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(view)
        timer = Timer()
//        Universal.Model="引领"
        ROSHelper.setSpeed(QuerySql.QueryBasic().leadingSpeed.toString())
//        NextTask.setNextTasK(true)
        val toSettingDialog = FromeSettingDialog(context)
        //是否是第一个页面
        if (FunctionSkip.selectFunction() == 4) {
            binding.firstFragment.visibility = View.GONE
            binding.llReturn.visibility = View.VISIBLE
        } else {
            binding.firstFragment.visibility = View.VISIBLE
            binding.llReturn.visibility = View.GONE
        }
        fromType = arguments?.getString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE)
        // 初始化适配器
        binding.GvGuideList.adapter = context?.let { GuidePointAdapter(it, queryFloorPoints) }

        LogUtil.i("this is map list:$queryFloorPoints")
        //item点击
        binding.GvGuideList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                LogUtil.i("点击了第${position}项,引领去往${queryFloorPoints[position].pointName}")
//                ROSHelper.navigateTo(location = queryFloorPoints[position])
//                GuideTaskBill(TaskModel(location = queryFloorPoints[position]))
                val endPoint = queryFloorPoints[position]
                val taskModel = TaskModel(location = endPoint)
                val bill = GuideTaskBillFactory.createBill(taskModel = taskModel)
                BillManager.addAllAtIndex(bill, 0)
                BillManager.currentBill()?.executeNextTask()
            }
        binding.llReturn.setOnClickListener {
            controller!!.navigate(R.id.action_guideFragment_to_homeFragment)
        }
        binding.imageViewSetting.setOnClickListener { v ->
            toSettingDialog.show()
            PassWordToSetting.observe(viewLifecycleOwner) {
                if (PassWordToSetting.value == true) {
                    controller!!.navigate(R.id.action_guideFragment_to_settingHomeFragment)
                    toSettingDialog.dismiss()
                    PassWordToSetting.postValue(false)
                }
            }
        }
    }
}