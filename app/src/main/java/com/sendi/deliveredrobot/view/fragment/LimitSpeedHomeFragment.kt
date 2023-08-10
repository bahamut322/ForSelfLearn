package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.LimitSpeedLineListAdapter
import com.sendi.deliveredrobot.LIMIT_SPEED_AREA_TRUE
import com.sendi.deliveredrobot.LINE_INFO_MODEL
import com.sendi.deliveredrobot.LINE_NAME
import com.sendi.deliveredrobot.SUB_MAP
import com.sendi.deliveredrobot.databinding.FragmentLimitSpeedHomeBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.utils.PxUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.inputfilter.NumRangeInputFilter
import com.sendi.deliveredrobot.view.widget.CustomSwitchTextView
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-02-22
 * @describe 限速区总览
 */
class LimitSpeedHomeFragment : Fragment() {
    private lateinit var binding:FragmentLimitSpeedHomeBinding
    private var lineListSize by Delegates.observable(0){
        _,_,newValue ->
        if(newValue > 0){
            binding.groupSetLimitSpeed.visibility = View.VISIBLE
            binding.groupNotSetLimitSpeed.visibility = View.GONE
        }else{
            binding.groupSetLimitSpeed.visibility = View.GONE
            binding.groupNotSetLimitSpeed.visibility = View.VISIBLE
        }
    }
    private var limitSpeedLineListAdapter: LimitSpeedLineListAdapter? = null
    private lateinit var subMap: SubMap
    private var firstLoad = true
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        limitSpeedLineListAdapter = LimitSpeedLineListAdapter()
        limitSpeedLineListAdapter?.setOnItemClickListener(object :LimitSpeedLineListAdapter.OnItemClickCallback{
            override fun onEditButtonClick(data: LineInfoModel, position: Int) {
                if (ROSHelper.editLineInfoParams(subMap.path!!,data)) {
                    getEditLimitSpeedDialog(subMap, data).show()
                }else{
                    ToastUtil.show("编辑限速区失败")
                }
            }

            override fun onDeleteButtonClick(data: LineInfoModel, position: Int) {
                getDeleteLimitSpeedDialog(subMap, data).show()
            }
        })
        subMap = arguments?.getSerializable(SUB_MAP) as SubMap
        if(!firstLoad){
            return
        }
        firstLoad = false
        if (!TextUtils.isEmpty(subMap.path)) {
            //获取line的名字列表
            val nameList = ROSHelper.getLimitSpeedLineNameList(subMap.path!!)
            if (nameList != null && nameList.isNotEmpty()) {
                //获取line的信息列表
                val lineInfoList = ROSHelper.getLimitSpeedLineInfoList(subMap.path!!,nameList)
                limitSpeedLineListAdapter?.data = lineInfoList
            }
        }
    }

    override fun onStart() {
        super.onStart()
        DialogHelper.loadingDialog.show()
        MainScope().launch(Dispatchers.Default) {
            if (!mapLaserServiceImpl.sendLaserMapManagerMsg(2).isFlag) {
                ToastUtil.show("开始处理路径和目标点失败")
            }
            virtualTaskExecute(5)
            DialogHelper.loadingDialog.dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_limit_speed_home, container, false)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.textViewCreateLimitSpeed1.apply {
            setOnClickListener {
                findNavController().navigate(R.id.createLimitSpeedFragment,
                    Bundle().apply {
                        putSerializable(SUB_MAP, subMap)
                        putString(LINE_NAME, getLineName(limitSpeedLineListAdapter?.data!!))
                    })
            }
        }
        binding.textViewCreateLimitSpeed2.apply {
            setOnClickListener {
                findNavController().navigate(R.id.createLimitSpeedFragment,
                    Bundle().apply {
                        putSerializable(SUB_MAP, subMap)
                        putString(LINE_NAME, getLineName(limitSpeedLineListAdapter?.data!!))
                    })
            }
        }
        binding.textViewPrevStep.apply {
            setOnClickListener {
                DialogHelper.loadingDialog.show()
                MainScope().launch(Dispatchers.Default) {
                    if (!mapLaserServiceImpl.sendLaserMapManagerMsg(3).isFlag) {
                        ToastUtil.show("结束处理路径和目标点失败")
                        DialogHelper.loadingDialog.dismiss()
                        return@launch
                    }
                    DialogHelper.loadingDialog.dismiss()
                    if (ROSHelper.notSaveLimitSpeed()) {
                        findNavController().popBackStack()
                    }else{
                        ToastUtil.show("不保存限速区失败")
                    }
                }
            }
        }
        binding.recyclerViewLine.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = limitSpeedLineListAdapter
        }
        binding.textViewSave.apply {
            setOnClickListener {
                DialogHelper.loadingDialog.show()
                MainScope().launch(Dispatchers.Default) {
                    if (!mapLaserServiceImpl.sendLaserMapManagerMsg(3).isFlag) {
                        ToastUtil.show("结束处理路径和目标点失败")
                        DialogHelper.loadingDialog.dismiss()
                        return@launch
                    }
                    DialogHelper.loadingDialog.dismiss()
                    if (ROSHelper.saveLimitSpeed(subMap.path!!)) {
                        val dao: DeliveredRobotDao =
                            DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                        //更新状态
                        dao.updateSubMap(subMap.apply {
                            limitSpeed = LIMIT_SPEED_AREA_TRUE
                        })
                        findNavController().popBackStack(R.id.debuggingFragment,false)
                    }else{
                        ToastUtil.show("保存限速区失败")
                    }
                }
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
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LineInfoModel>(
            LINE_INFO_MODEL
        )
            ?.observe(viewLifecycleOwner) {
                //创建后返回数据
                limitSpeedLineListAdapter?.addItem(it.copy())
            }
    }

    override fun onResume() {
        super.onResume()
        binding.textViewLaser.apply {
            text = subMap.name
        }
        binding.laserPointsView.setLineInfoModelList(limitSpeedLineListAdapter?.data!!)
        lineListSize = limitSpeedLineListAdapter?.data!!.size
    }

    private fun getLineName(list:List<LineInfoModel>): String{
        var num = 0
        for (s in list) {
            val temp = s.name.substring(4).toInt()
            if(temp > num){
                num = temp
            }
        }
        return "Line${++num}"
    }

    /**
     * @describe 删除限速区
     */
    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun getDeleteLimitSpeedDialog(data: SubMap, lineInfoModel: LineInfoModel): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = CommonHelper.getDeleteLaserMapTextSpan(
                string1 = "确认要删除",
                string2 = lineInfoModel.name,
                string3 = "吗？",
                color = ContextCompat.getColor(requireContext(), R.color.color_4D6FBE)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
                //请求底盘
                if (ROSHelper.deleteLimitSpeed(data.path!!,lineInfoModel.name)) {
                    //删除该项
                    limitSpeedLineListAdapter?.removeItem(lineInfoModel)
                    lineListSize = limitSpeedLineListAdapter?.data?.size ?: 0
                    //刷新绘图控件
                    binding.laserPointsView.invalidate()
                }else{
                    ToastUtil.show("删除限速区失败")
                }
            }
        }
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        dialog.setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
        return dialog
    }


    /**
     * 编辑限速区
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getEditLimitSpeedDialog(data: SubMap, lineInfoModel: LineInfoModel): Dialog{
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_limit_speed_line, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        val groupSlope = dialogView.findViewById<Group>(R.id.groupSlope)
        val customSwitchTextView = dialogView.findViewById<CustomSwitchTextView>(R.id.customIndicatorTextView)
        val viewBackGround = dialogView.findViewById<View>(R.id.viewBackGround)
        customSwitchTextView.apply {
            setOnToggleListener(object :CustomSwitchTextView.OnToggleListener{
                override fun onToggle(selected: Boolean) {
                    lineInfoModel.type = when (selected) {
                        false -> {
                            groupSlope.visibility = View.VISIBLE
                            with(viewBackGround){
                                layoutParams = layoutParams.apply {
                                    height = PxUtil.dp2px(context,507f)
                                }
                            }
                            0
                        }
                        true ->{
                            groupSlope.visibility = View.GONE
                            with(viewBackGround){
                                layoutParams = layoutParams.apply {
                                    height = PxUtil.dp2px(context,477f)
                                }
                            }
                            1
                        }
                    }
                }
            })
        }
        viewBackGround.apply {
            when (lineInfoModel.type) {
                0 -> {
                    customSwitchTextView.viewSelected = false
                }
                1 -> {
                    customSwitchTextView.viewSelected = true
                }
            }
        }
        dialogView.findViewById<EditText>(R.id.editTextVisibleRange).apply {
            setText("${lineInfoModel.visibleRange?:0f}")
            setSelection(text.length)
            addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        lineInfoModel.visibleRange = s.toString().toFloat()
                    }else{
                        lineInfoModel.visibleRange = 0f
                    }
                }
            })
        }
        dialogView.findViewById<EditText>(R.id.editTextSpeed).apply {
            setText("${lineInfoModel.speed}")
            setSelection(text.length)
            addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        lineInfoModel.speed = s.toString().toFloat()
                    }else{
                        lineInfoModel.speed = 0f
                    }
                }
            })
        }
        val editTextRadius = dialogView.findViewById<EditText>(R.id.editTextRadius).apply {
            setText("${lineInfoModel.radius}")
            val numRangeInputFilter = NumRangeInputFilter()
            filters = arrayOf(numRangeInputFilter)
            setSelection(text.length)
            addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s.toString())) {
                        lineInfoModel.radius = s.toString().toFloat()
                    }else{
                        lineInfoModel.radius = 0f
                    }
                }
            })
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.findViewById<View>(R.id.viewDismiss).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                //编辑限速区
                val radius = editTextRadius.text.toString().trim().toFloatOrNull()
                if(radius == null || radius < 0.5f || radius > 5f){
                    ToastUtil.show("请填入正确半径（0.5-5）")
                    return@setOnClickListener
                }
                if (ROSHelper.setLimitSpeedParam(
                        lineInfoModel.name,
                        lineInfoModel.type ?: 0,
                        lineInfoModel.radius ?: 0f,
                        lineInfoModel.speed ?: 0f,
                        visibleRange = lineInfoModel.visibleRange ?: 0f
                    )
                ) {
                    limitSpeedLineListAdapter?.setItem(lineInfoModel)
                    limitSpeedLineListAdapter?.notifyDataSetChanged()
                    dialog.dismiss()
                } else {
                    dialog.dismiss()
                    ToastUtil.show("编辑限速区失败")
                }
            }
        }
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        dialog.setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
        return dialog
    }
}