package com.sendi.deliveredrobot.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.util.Consumer
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.BusinessAdapter
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.databinding.FragmentBusinessBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.ShoppingActionDB
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.BusinessTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.TaskQueues
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import com.sendi.fooddeliveryrobot.BaseVoiceRecorder
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
class BusinessFragment : Fragment() {

    private lateinit var binding: FragmentBusinessBinding
    private var controller: NavController? = null
    private var shoppingActionList: List<ShoppingActionDB> = ArrayList()
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
        val taskConsumer =
            Consumer { task: String ->
                // 执行任务的代码
                if (BuildConfig.IS_SPEAK) {
                    BaiduTTSHelper.getInstance().speaks(task, "explanation")
                }
                LogUtil.i("Task: $task")
            }
        // 创建TaskQueue实例
        Universal.taskQueue = TaskQueues(taskConsumer)
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
        viewModel!!.splitTextByPunctuation(QuerySql.ShoppingConfig().firstPrompt!!)
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
            controller!!.navigate(R.id.action_businessFragment_to_homeFragment)
        }
        //设置按钮
        binding.imageViewSetting.setOnClickListener {
            toSettingDialog.show()
            RobotStatus.PassWordToSetting.observe(viewLifecycleOwner) {
                if (RobotStatus.PassWordToSetting.value == true) {
                    try {
                        BaiduTTSHelper.getInstance().stop()
                        controller!!.navigate(R.id.action_businessFragment_to_planSettingFragment)
                    } catch (_: Exception) {
                    }
                    toSettingDialog.dismiss()
                    RobotStatus.PassWordToSetting.postValue(false)
                }
            }
            Toast.makeText(context, "点击了：设置", Toast.LENGTH_SHORT).show()
        }


        //初始化适配器
        binding.businessGv.adapter = context?.let { BusinessAdapter(it, shoppingActionList) }
        //item点击
        binding.businessGv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                if (RobotStatus.batteryStateNumber.value == false || QuerySql.robotConfig().chargePointName.isNullOrEmpty() || QuerySql.robotConfig().waitingPointName.isNullOrEmpty()) {
                    DialogHelper.briefingDialog.show()
                }else {
                    LogUtil.i("点击了第：${position}项,引领去往：${shoppingActionList[position].pointName},当前点拟定名字为：${shoppingActionList[position].name}")

                    RobotStatus.shoppingName = shoppingActionList[position].pointName!!
                    BaiduTTSHelper.getInstance().stop()
                    if (shoppingActionList[position].actionType == 1) {
                        RobotStatus.shoppingType = 1
                        LogUtil.d("定点")
                        MyApplication.instance?.sendBroadcast(Intent().apply {
                            action = ACTION_NAVIGATE
                            putExtra(NAVIGATE_ID, R.id.businessIngFragment)
                        })
                    } else {
                        LogUtil.d("去某点${RobotStatus.shoppingName}")
                        RobotStatus.shoppingType = 2
                        thread {
                            val endPoint =
                                dao.queryPoint(RobotStatus.shoppingName)
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
            controller?.navigate(R.id.conversationFragment)
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
        RobotStatus.newUpdata.observe(viewLifecycleOwner) {
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

    override fun onResume() {
        super.onResume()
        BaseVoiceRecorder.getInstance()?.recordCallback = { _, pinyinString ->
            if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
                Log.i("AudioChannel", "包含${WakeupWordHelper.wakeupWord}")
                controller?.navigate(R.id.conversationFragment)
            }
        }
    }
}