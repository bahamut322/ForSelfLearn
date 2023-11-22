package com.sendi.deliveredrobot.view.fragment

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
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.BuildConfig
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.GuidePointAdapter
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.databinding.FragmentGuideBinding
import com.sendi.deliveredrobot.entity.FunctionSkip
import com.sendi.deliveredrobot.entity.Universal
import com.sendi.deliveredrobot.entity.entitySql.QuerySql
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.helpers.WakeupWordHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GuideTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.navigationtask.RobotStatus.PassWordToSetting
import com.sendi.deliveredrobot.navigationtask.RobotStatus.pointItem
import com.sendi.deliveredrobot.navigationtask.RobotStatus.selectRoutMapItem
import com.sendi.deliveredrobot.navigationtask.TaskQueues
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.view.widget.FromeSettingDialog
import com.sendi.deliveredrobot.viewmodel.BusinessViewModel
import com.sendi.fooddeliveryrobot.VoiceRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer


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
    private val viewModel: BusinessViewModel by viewModels({ requireActivity() })
    private var fromType: String? = null
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //获取所有目标点
        queryFloorPoints = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            queryFloorPoints = dao.queryAllPoints()
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
        val view = inflater.inflate(R.layout.fragment_guide, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //回到主页面的时候初始化一下选择讲解点的值
        selectRoutMapItem!!.postValue(-1)
        pointItem!!.postValue(-1)
        updateDataAndRefreshList()
        viewModel.splitTextByPunctuation(QuerySql.selectGuideFouConfig().firstPrompt)
//        UpdateReturn().method()
        controller = Navigation.findNavController(requireView())
        timer = Timer()
//        Universal.Model="引领"
        binding.tvGuideName.text = QuerySql.QueryExplainConfig().slogan
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
                if (RobotStatus.batteryStateNumber.value == false) {
                    Toast.makeText(context, "请先对接充电桩", Toast.LENGTH_SHORT).show()
                    DialogHelper.briefingDialog.show()
                } else {
                    BaiduTTSHelper.getInstance().stop()
                    LogUtil.i("点击了第${position}项,引领去往${queryFloorPoints[position].pointName}")
                    val endPoint = queryFloorPoints[position]
                    Log.d("TAG", "onViewCreated: " + endPoint.pointDirection)
                    val taskModel = TaskModel(location = endPoint)
                    val bill = GuideTaskBillFactory.createBill(taskModel = taskModel)
                    BillManager.addAllAtIndex(bill, 0)
                    BillManager.currentBill()?.executeNextTask()
                }
            }
        binding.llReturn.setOnClickListener {
            controller!!.navigate(R.id.action_guideFragment_to_homeFragment)
        }
        binding.imageViewSetting.setOnClickListener {
            toSettingDialog.show()
            PassWordToSetting.observe(viewLifecycleOwner) {
                if (PassWordToSetting.value == true) {
                    try {
                        controller!!.navigate(R.id.action_guideFragment_to_settingHomeFragment)
                    } catch (_: Exception) {
                    }
                    toSettingDialog.dismiss()
                    PassWordToSetting.postValue(false)
                }
            }
        }
        binding.bubbleTv.setOnClickListener {
            controller?.navigate(R.id.conversationFragment)
        }
    }

    //刷新数据
    private fun updateDataAndRefreshList() {
        //更新了地图配置
        Universal.mapType.observe(viewLifecycleOwner) {
            if (it) {
                updateList()
            }
        }
        //更新了引领配置
        RobotStatus.newUpdata.observe(viewLifecycleOwner) {
            if (it == 1 || it ==2) {
                updateList()
            }
        }
        RobotStatus.robotConfig?.observe(viewLifecycleOwner){
            binding.bubbleTv.text = String.format(getString(R.string.ask), it.wakeUpWord)
        }
    }

    private fun updateList(){
        queryFloorPoints = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            // 异步查询数据库
            val newQueryFloorPoints = dao.queryAllPoints()
            withContext(Dispatchers.Main) {
                // 在主线程中更新UI
                queryFloorPoints = newQueryFloorPoints
                // 设置新的适配器
                binding.GvGuideList.adapter = context?.let { GuidePointAdapter(requireContext(), queryFloorPoints) }
                // 通知Adapter数据已变更
                (binding.GvGuideList.adapter as? GuidePointAdapter)?.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        VoiceRecorder.getInstance().recordCallback = { _, pinyinString ->
            if (pinyinString.contains(WakeupWordHelper.wakeupWordPinyin ?: "")) {
                Log.i("AudioChannel", "包含${WakeupWordHelper.wakeupWord}")
                controller?.navigate(R.id.conversationFragment)
            }
        }
    }
}