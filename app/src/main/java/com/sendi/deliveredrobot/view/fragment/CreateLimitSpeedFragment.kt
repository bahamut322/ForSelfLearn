package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.databinding.FragmentCreateLimitSpeedBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.PointCompat
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.utils.NavigationBarUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.inputfilter.NumRangeInputFilter
import com.sendi.deliveredrobot.view.widget.CustomSwitchTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-02-23
 * @describe 创建限速区
 */
class CreateLimitSpeedFragment : Fragment() {
    companion object{
        const val STATUS_READY = 0
        const val STATUS_CREATING = 1
        const val STATUS_FINISH = 2
    }

    lateinit var binding: FragmentCreateLimitSpeedBinding
    private val numberArray = arrayOf("0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0")
    private var pose2dList: ArrayList<PointCompat>? = null
    private var status: Int by Delegates.observable(STATUS_READY){
            _, _, newValue ->
        when (newValue) {
            STATUS_READY -> {
                binding.textViewPrevStep.visibility = View.VISIBLE
                binding.textViewReset.apply {
                    visibility = View.GONE
                }
                binding.tvStart.apply {
                    text = "开始"
                }
            }
            STATUS_CREATING -> {
                binding.textViewPrevStep.visibility = View.GONE
                binding.textViewReset.apply {
                    visibility = View.GONE
                }
                binding.tvStart.apply {
                    text = "结束"
                }
            }
            STATUS_FINISH -> {
                binding.textViewPrevStep.visibility = View.GONE
                binding.textViewReset.visibility = View.VISIBLE
                binding.tvStart.apply {
                    text = "确认"
                }
            }
        }
    }
    private lateinit var subMap: SubMap
    private lateinit var lineName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //清除缓存
        LaserObject.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_limit_speed, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.textViewPrevStep.apply {
            isClickable = true
            setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.spinnerSpeed.apply {
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner_limit_speed,
                R.id.textViewContent,
                numberArray
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_limit_speed)
            }
            adapter = arrayAdapter
            setSelection(2)
        }

        binding.editTextRadius.apply {
            val inputFilter = NumRangeInputFilter()
            filters = arrayOf(inputFilter)
            setText("1")
        }


        binding.tvStart.apply {
            setOnClickListener {
                when(status){
                    STATUS_READY ->{
                        MainScope().launch {
                            withContext(Dispatchers.Main){
                                DialogHelper.loadingDialog.show()
                            }
                            withContext(Dispatchers.Default){
                                //开始创建限速区失败
//                                if (mapLaserServiceImpl.sendLaserMapManagerMsg(2).isFlag) {
                                //订阅激光点
                                SubManager.sub(ClientConstant.GLOBAL_LASER)
                                if (ROSHelper.startCreateLimitSpeed()) {
                                    withContext(Dispatchers.Main) {
                                        DialogHelper.loadingDialog.dismiss()
                                        status = STATUS_CREATING
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        DialogHelper.loadingDialog.dismiss()
                                    }
                                    ToastUtil.show("创建限速区失败")
                                }
//                                }else{
//                                    withContext(Dispatchers.Main){
//                                        DialogHelper.loadingDialog.dismiss()
//                                    }
//                                    ToastUtil.show("开始处理路径和目标点失败")
//                                }
                            }
                        }
                    }
                    STATUS_CREATING -> {
                        MainScope().launch(Dispatchers.Default) {
                            DialogHelper.loadingDialog.show()
                            //结束创建限速区
//                                if (mapLaserServiceImpl.sendLaserMapManagerMsg(3).isFlag) {
                            //解除订阅激光点
                            SubManager.unsub(ClientConstant.GLOBAL_LASER)
                            if (ROSHelper.endCreateLimitSpeed(lineName)) {
                                DialogHelper.loadingDialog.dismiss()
                                withContext(Dispatchers.Main) {
                                    status = STATUS_FINISH
                                }
                            } else {
                                DialogHelper.loadingDialog.dismiss()
                                ToastUtil.show("结束创建限速区失败")
                            }
//                                }else{
//                                    DialogHelper.loadingDialog.dismiss()
//                                    ToastUtil.show("退出处理路径和目标点失败")
//                                }
                        }
                    }
                    STATUS_FINISH -> {
                        val name = lineName
                        val type = when(binding.customSwitchTextView.viewSelected){
                            false -> 0
                            true -> 1
                        }
                        val radius = binding.editTextRadius.text.toString().trim().toFloatOrNull()
                        val speed = binding.spinnerSpeed.selectedItem.toString().trim().toFloatOrNull()
                        val visibleRange = binding.editTextVisibleRange.text.toString().trim().toFloatOrNull()
                        if(radius == null || radius < 0.5f || radius > 5f){
                            ToastUtil.show("请填入正确半径（0.5-5）")
                            return@setOnClickListener
                        }
                        if(speed == null){
                            ToastUtil.show("请填入速度")
                            return@setOnClickListener
                        }
                        if(!binding.customSwitchTextView.viewSelected){
                            if(visibleRange == null){
                                ToastUtil.show("请填入可视范围")
                                return@setOnClickListener
                            }
                        }
                        if(ROSHelper.setLimitSpeedParam(
                                name = name,
                                type = type,
                                radius = radius,
                                speed = speed,
                                visibleRange = visibleRange?:0f)
                        ){
                            status = STATUS_FINISH
                            //返回上层页面
                            val lineInfoModel = LineInfoModel(
                                pose = pose2dList,
                                type = type,
                                name = name,
                                radius = radius,
                                speed = speed,
                                visibleRange = visibleRange?:0f,
                                state = 0
                            )
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                LINE_INFO_MODEL, lineInfoModel)
                            findNavController().popBackStack()
                        }else{
                            ToastUtil.show("结束创建限速区失败")
                        }
                    }
                }
            }
        }

        binding.textViewReset.apply {
            visibility = View.GONE
            setOnClickListener {
                if (ROSHelper.resetLimitSpeed()){
                    binding.laserPointsView.clearLineInfo()
                    status = STATUS_READY
                }else{
                    ToastUtil.show("重置限速区失败")
                }
            }
        }

        binding.customSwitchTextView.apply {
            setOnToggleListener(object : CustomSwitchTextView.OnToggleListener{
                override fun onToggle(selected: Boolean) {
                    if(selected){
                        //其他
                        binding.editTextVisibleRange.visibility = View.GONE
                        binding.textViewVisibleRangeDescribe.visibility = View.GONE
                    }else{
                        //坡道
                        binding.editTextVisibleRange.visibility = View.VISIBLE
                        binding.textViewVisibleRangeDescribe.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.editTextVisibleRange.apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                val window = activity?.window?:return@addOnGlobalLayoutListener
                NavigationBarUtil.hideNavigationBar(window)
            }
        }
        binding.laserPointsView.apply {
            setStaticPoints(RosPointArrUtil.staticMap)
            invalidate()
        }
        binding.textViewGetDistance.apply {
            setOnClickListener {
                val distance = ROSHelper.getSlopeDistance()
                if (distance != null) {
                    binding.textViewDistance.apply {
                        text = "斜坡距离：${distance}m"
                    }
                }else{
                    ToastUtil.show("获取斜坡距离失败")
                }
            }
        }
        LaserObject.clear()
        LaserObject.tempObstacle.observe(viewLifecycleOwner){
            if (it == null) {
                return@observe
            }
            val list = ArrayList<PointCompat>()
            for (point32 in it) {
                val pointCompat = PointCompat()
                pointCompat.x = point32.x.toDouble()
                pointCompat.y = point32.y.toDouble()
                pointCompat.z = point32.z.toDouble()
                list.add(pointCompat)
            }
            pose2dList = list
//            LogUtil.d("tempObstacle:${list.toArray().contentToString()}")
            binding.laserPointsView.setLineInfo(LineInfoModel(pose = list, name = lineName))
        }
        LaserObject.livePoints.observe(viewLifecycleOwner){
            if (it == null) {
                return@observe
            }
            binding.laserPointsView.setLaserPoints(it)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        subMap = arguments?.getSerializable(SUB_MAP) as SubMap
        binding.textViewLaser.apply {
            text = subMap.name
        }
        lineName = arguments?.getString(LINE_NAME)?:""
        binding.textViewName.apply {
            text = "限速区：$lineName"
        }
    }
}