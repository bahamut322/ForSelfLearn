package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.TYPE_STAND_STILL
import com.sendi.deliveredrobot.adapter.base.i.BusinessAdapter
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentBusinessBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.Table_Shopping_Action
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.ReportDataHelper.reportTaskDto
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.BusinessTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.service.PlaceholderEnum
import com.sendi.deliveredrobot.service.TaskIdGenerator
import com.sendi.deliveredrobot.service.TaskStageEnum
import com.sendi.deliveredrobot.service.TaskTypeEnum
import com.sendi.deliveredrobot.service.UpdateReturn
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

/**
 * @Author Swn
 * @Data 2023/10/18
 * @describe 业务办理页面
 */
class BusinessFragment : BaseFragment() {

    private lateinit var binding: FragmentBusinessBinding
    private var controller: NavController? = null
    private var shoppingActionList: List<Table_Shopping_Action> = ArrayList()
    val mainScope = MainScope()
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private var viewModel: BusinessViewModel?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //获取所有目标点
        shoppingActionList = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            shoppingActionList = QuerySql.SelectShoppingAction(QuerySql.robotConfig().mapName)
            LogUtil.d("列表长度："+shoppingActionList.size)
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
        viewModel = ViewModelProvider(this).get(BusinessViewModel::class.java)
        viewModel!!.restoreVideo(viewLifecycleOwner)
        //设置速度
        ROSHelper.setSpeed("${QuerySql.QueryBasic().goBusinessPoint}")
        updateDataAndRefreshList()
        //进入页面播报
        BaiduTTSHelper.getInstance().speaks(PlaceholderEnum.replaceText(QuerySql.ShoppingConfig().firstPrompt!!, business = QuerySql.ShoppingConfig().name!!))
//        viewModel!!.splitTextByPunctuation(QuerySql.ShoppingConfig().firstPrompt!!)
        if (FunctionSkip.selectFunction() == 4) {
            binding.firstFragment.visibility = View.GONE
            binding.llReturn.visibility = View.VISIBLE
        } else {
            binding.firstFragment.visibility = View.VISIBLE
            binding.llReturn.visibility = View.GONE
        }
        //设置列表最大高度
        binding.businessGv.setMaxHeight(520)
        binding.businessName.text = QuerySql.ShoppingConfig().name

        //返回按钮
        binding.llReturn.setOnClickListener {
            BaiduTTSHelper.getInstance().stop()
            navigateToFragment(R.id.action_businessFragment_to_homeFragment)
        }
        //设置按钮
        binding.imageViewSetting.setOnClickListener {
            toSettingDialog.show()
            RobotStatus.passWordToSetting.observe(viewLifecycleOwner) {
                if (RobotStatus.passWordToSetting.value == true) {
                    try {
                        BaiduTTSHelper.getInstance().stop()
                        navigateToFragment(R.id.action_businessFragment_to_planSettingFragment)
                    } catch (_: Exception) {
                    }
                    toSettingDialog.dismiss()
                    RobotStatus.passWordToSetting.postValue(false)
                }
            }
            Toast.makeText(context, "点击了：设置", Toast.LENGTH_SHORT).show()
        }


        //初始化适配器
        binding.businessGv.adapter = context?.let { BusinessAdapter(it, shoppingActionList) }
        //item点击
        binding.businessGv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if ((RobotStatus.chargeStatus.value == false && RobotStatus.currentStatus != TYPE_STAND_STILL) || QuerySql.robotConfig().chargePointName.isNullOrEmpty() || QuerySql.robotConfig().waitingPointName.isNullOrEmpty()) {
                    DialogHelper.briefingDialog.show()
                }else {
                    LogUtil.i("点击了第：${position}项,引领去往：${shoppingActionList[position].pointName},当前点拟定名字为：${shoppingActionList[position].name}")
                    Log.d("TAG", "onViewCreated1: "+Universal.shoppingName)
                    BaiduTTSHelper.getInstance().stop()
                    if (shoppingActionList[position].actionType == 1) {
                        Universal.shoppingName = shoppingActionList[position].name
                        Universal.shoppingType = 1
                        LogUtil.d("定点")
                        val taskId = TaskIdGenerator.getInstance().generateTaskId(TaskTypeEnum.BUSINESS)
                        val args: Bundle = Bundle().apply {
                            // 设置 Bundle 对象参数数据
                            this.putString("taskId", taskId)
                        }
                        navigateToFragment(R.id.businessIngFragment, args)
                        //上报定点任务开始
                        reportTaskDto(
                            TaskModel(endTarget = "定点导购",taskId = taskId),
                            TaskStageEnum.ALLStartTask,
                            UpdateReturn.taskDto()
                        )
                    } else {
                        Universal.shoppingName = shoppingActionList[position].name
                        LogUtil.d("去某点${shoppingActionList[position].pointName}")
                        Universal.shoppingType = 2
                        thread {
                            val endPoint =
                                dao.queryPoint(shoppingActionList[position].pointName.toString())
                            Log.d("TAG", "onViewCreated: " + endPoint!!.pointDirection)
                            val taskModel = TaskModel(location = endPoint)
                            val bill = BusinessTaskBillFactory.createBill(taskModel = taskModel)
                            BillManager.addAllAtIndex(bill, 0)
                            BillManager.currentBill()?.executeNextTask()
                        }
                    }
                }
            }
        binding.bubbleTv.setOnClickListener {
            navigateToFragment(R.id.conversationFragment)
        }
    }

    //刷新
    private fun updateDataAndRefreshList() {
        //更新了地图配置
        Universal.mapType.observe(viewLifecycleOwner) {
            if (it) {
                updateList()
            }
        }
        //更新了导购配置
        RobotStatus.newUpdate.observe(viewLifecycleOwner) {
            if (it == 1 || it == 2) {
                updateList()
            }
        }
        RobotStatus.robotConfig?.observe(viewLifecycleOwner){
            binding.bubbleTv.text = String.format(getString(R.string.ask), it.wakeUpWord)
        }
    }

    private fun updateList() {
        shoppingActionList = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            val newShoppingActionList = QuerySql.SelectShoppingAction(QuerySql.robotConfig().mapName)
            withContext(Dispatchers.Main) {
                shoppingActionList = newShoppingActionList
                // 设置新的适配器
                binding.businessGv.adapter = context?.let { BusinessAdapter(it, shoppingActionList) }
                // 通知Adapter数据已变更
                (binding.businessGv.adapter as? BusinessAdapter)?.notifyDataSetChanged()
                binding.businessName.text = RobotStatus.shoppingConfigList?.value?.name ?: QuerySql.ShoppingConfig()?.name
            }
        }
    }
}