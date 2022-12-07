package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.TargetSortAdapter
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_MAP_ID
import com.sendi.deliveredrobot.NAVIGATE_MAP_NAME
import com.sendi.deliveredrobot.databinding.FragmentCreateTargetSortBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.entity.PublicArea
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.AddAreaDialog
import com.sendi.deliveredrobot.view.widget.DeleteRouteConfirmDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class CreateTargetSortFragment:Fragment() {

    lateinit var binding: FragmentCreateTargetSortBinding
    lateinit var mAdapter: TargetSortAdapter
    var listData: ArrayList<PublicArea> = arrayListOf()
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    /** 关联的激光地图id */
    private var selectSubMapId : Int = -1
    /** 目标点图id  -1:表示创建 */
    private var targetMapId : Int = -1
    var selectSubMapName : String = ""
    lateinit var mAddAreaDialog:AddAreaDialog

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
        return inflater.inflate(R.layout.fragment_create_target_sort, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        targetMapId = arguments?.getInt(NAVIGATE_ID,-1)!!
        selectSubMapId = arguments?.getInt(NAVIGATE_MAP_ID,-1)!!
        selectSubMapName = arguments?.getString(NAVIGATE_MAP_NAME,"")?:""


        binding.tvTitle.setText("创建目标点分类")
        binding.rcyTargetSort.apply {
            mAdapter = TargetSortAdapter(context,null).apply {
                layoutManager = GridLayoutManager(requireContext(),4)
                setOnItemClickListener(object : TargetSortAdapter.OnItemClickCallback {
                    override fun onItemClick(data: PublicArea, position: Int) {
                        if(data.id == -1){
                            mAddAreaDialog = AddAreaDialog(requireContext(),object :
                                AddAreaDialog.AddAreaDialogListener{
                                override fun confirm(name:String) {
                                    MainScope().launch {
                                        withContext(Dispatchers.Default) {
                                            DialogHelper.loadingDialog.show()
                                            var mMapResult = mapTargetPointServiceImpl.addNewType(name)
                                            withContext(Dispatchers.Main) {
                                                ToastUtil.show(mMapResult.msg)
                                                if (mMapResult.isFlag){
                                                    var templistData = mMapResult.data.get("typeList") as ArrayList<PublicArea>
                                                    listData.clear()
                                                    listData.addAll(templistData)
                                                    //增加一个虚拟分类数据 作为加号显示
                                                    listData.add(PublicArea(-1,"+",-1))
                                                    mAdapter.setData(listData)
                                                    mAddAreaDialog.dismiss()
                                                }else{
                                                    mAddAreaDialog.setTips(mMapResult.msg)
                                                }
                                            }
                                            DialogHelper.loadingDialog.dismiss()
                                        }
                                    }
                                }

                                override fun cancel() {

                                }
                            })
                            mAddAreaDialog.show()
                        }

                    }

                    override fun onItemLongClick(data: PublicArea, position: Int) {


                    }

                    override fun onDeleteButtonClick(data: PublicArea, position: Int) {
                        DeleteRouteConfirmDialog(
                            requireContext(),
                            object :
                                DeleteRouteConfirmDialog.DeleteRouteDialogListener {
                                override fun confirm(dialog: DeleteRouteConfirmDialog) {
                                    MainScope().launch {
                                        withContext(Dispatchers.Default) {
                                            var mDeleteMapResult = mapTargetPointServiceImpl.deleteType(data.id)
                                            withContext(Dispatchers.Main) {
                                                ToastUtil.show(mDeleteMapResult.msg)
                                                if (mDeleteMapResult.isFlag){
                                                    listData.removeAt(position)
                                                    mAdapter.setData(listData)
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun cancel(dialog: DeleteRouteConfirmDialog) {
                                }
                            }, data.name?:"分类名"
                        ).show()
                    }
                })
            }
            adapter = mAdapter
        }

        binding.tvGoback.apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                DialogHelper.loadingDialog.show()
                MainScope().launch(Dispatchers.Default) {
                    if (!mapLaserServiceImpl.sendLaserMapManagerMsg(3).isFlag) {
                        ToastUtil.show("结束处理路径和目标点失败")
                        DialogHelper.loadingDialog.dismiss()
                        return@launch
                    }
                    DialogHelper.loadingDialog.dismiss()
                    findNavController().popBackStack()
                }
            }
        }

        binding.tvNext.apply {
            setOnClickListener{
                findNavController().navigate(R.id.createTargetPointFragment,
                    Bundle().apply {
                        putInt(NAVIGATE_ID, targetMapId)
                        putInt(NAVIGATE_MAP_ID, selectSubMapId)
                        putString(NAVIGATE_MAP_NAME, selectSubMapName)
                    })
            }
        }

        MainScope().launch {
            withContext(Dispatchers.Default) {
                listData.clear()
                var mMapResult = mapTargetPointServiceImpl.types
                var templistData = mMapResult.data.get("typeList") as ArrayList<PublicArea>
                listData.addAll(templistData)
                listData.add(PublicArea(-1,"+",-1))
                withContext(Dispatchers.Main) {
                    mAdapter.setData(listData)
                }
            }
        }
    }

}