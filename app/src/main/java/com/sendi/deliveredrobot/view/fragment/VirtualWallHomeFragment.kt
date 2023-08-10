package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.VirtualWallLineListAdapter
import com.sendi.deliveredrobot.LINE_INFO_MODEL
import com.sendi.deliveredrobot.LINE_NAME
import com.sendi.deliveredrobot.SUB_MAP
import com.sendi.deliveredrobot.VIRTUAL_WALL_TRUE
import com.sendi.deliveredrobot.databinding.FragmentVirtualWallHomeBinding
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
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-03-03
 * @describe 虚拟墙总览
 */
class VirtualWallHomeFragment : Fragment() {
    private lateinit var binding: FragmentVirtualWallHomeBinding
    private var lineListSize by Delegates.observable(0){
            _,_,newValue ->
        if(newValue > 0){
            binding.groupSetVirtualWall.visibility = View.VISIBLE
            binding.groupNotSetVirtualWall.visibility = View.GONE
        }else{
            binding.groupSetVirtualWall.visibility = View.GONE
            binding.groupNotSetVirtualWall.visibility = View.VISIBLE
        }
    }
    private var virtualWallLineListAdapter: VirtualWallLineListAdapter? = null
    private lateinit var subMap: SubMap
    private var firstLoad = true
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_virtual_wall_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        virtualWallLineListAdapter = VirtualWallLineListAdapter()
        virtualWallLineListAdapter?.setOnItemClickListener(object :VirtualWallLineListAdapter.OnItemClickCallback{
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
            val nameList = ROSHelper.getVirtualWallLineNameList(subMap.path!!)
            if (nameList != null && nameList.isNotEmpty()) {
                //获取line的信息列表
                val lineInfoList = ROSHelper.getVirtualWallLineList(subMap.path!!,nameList)
                virtualWallLineListAdapter?.data = lineInfoList
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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.textViewCreateVirtualWall1.apply {
            setOnClickListener {
                findNavController().navigate(R.id.createVirtualWallFragment,
                    Bundle().apply {
                        putSerializable(SUB_MAP, subMap)
                        putString(LINE_NAME, getLineName(virtualWallLineListAdapter?.data!!))
                    })
            }
        }
        binding.textViewCreateVirtualWall2.apply {
            setOnClickListener {
                findNavController().navigate(R.id.createVirtualWallFragment,
                    Bundle().apply {
                        putSerializable(SUB_MAP, subMap)
                        putString(LINE_NAME, getLineName(virtualWallLineListAdapter?.data!!))
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
                    if (ROSHelper.notSaveVirtualWall()) {
                        findNavController().popBackStack()
                    }else{
                        ToastUtil.show("不保存虚拟墙失败")
                    }
                }
            }
        }
        binding.recyclerViewLine.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = virtualWallLineListAdapter
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
                    if (ROSHelper.saveVirtualWall(subMap.path!!)) {
                        val dao: DeliveredRobotDao =
                            DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
                        //更新状态
                        dao.updateSubMap(subMap.apply {
                            virtualWall = VIRTUAL_WALL_TRUE
                        })
                        findNavController().popBackStack(R.id.debuggingFragment, false)
                    }else{
                        ToastUtil.show("保存虚拟墙失败")
                    }
                }
            }
        }
        binding.laserPointsView.apply {
            setStaticPoints(RosPointArrUtil.staticMap)
            invalidate()
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LineInfoModel>(
            LINE_INFO_MODEL
        )
            ?.observe(viewLifecycleOwner) {
                //创建后返回数据
                virtualWallLineListAdapter?.addItem(it.copy())
            }
    }

    override fun onResume() {
        super.onResume()
        binding.textViewLaser.apply {
            text = subMap.name
        }
        binding.laserPointsView.setLineInfoModelList(virtualWallLineListAdapter?.data!!)
        lineListSize = virtualWallLineListAdapter?.data!!.size
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
    private fun getDeleteLimitSpeedDialog(data: SubMap, virtualWallLineInfoModel: LineInfoModel): Dialog {
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
                string2 = virtualWallLineInfoModel.name,
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
                if (ROSHelper.deleteVirtualWall(data.path!!,virtualWallLineInfoModel.name)) {
                    //删除该项
                    virtualWallLineListAdapter?.removeItem(virtualWallLineInfoModel)
                    lineListSize = virtualWallLineListAdapter?.data?.size ?: 0
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
}