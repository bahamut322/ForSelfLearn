package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.TargetAdapter
import com.sendi.deliveredrobot.databinding.FragmentTargetMapListBinding
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.debug.IMapTargetPointService
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.DeleteRouteConfirmDialog
import com.sendi.deliveredrobot.view.widget.DeleteRouteErrorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.concurrent.thread

/**
 * @author lsz
 * @describe 目标点
 * @date 2021/9/29
 */
class TargetMapListFragment : Fragment() {
    lateinit var binding: FragmentTargetMapListBinding
    lateinit var mAdapter: TargetAdapter
    var listData: ArrayList<SubMap> = arrayListOf()
    lateinit var deleteTarget: MapTargetPointServiceImpl
    lateinit var debugDao: DebugDao
    private lateinit var mapTargetPointServiceImpl: IMapTargetPointService

    init {
        thread {
            mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_target_map_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deleteTarget = MapTargetPointServiceImpl()
        binding = DataBindingUtil.bind(view)!!
        binding.rcyTargetList.apply {
            mAdapter = TargetAdapter(context, null).apply {
                layoutManager = LinearLayoutManager(requireContext())
                setOnItemClickListener(object : TargetAdapter.OnItemClickCallback {
                    override fun onItemClick(data: SubMap, position: Int) {
                        findNavController().navigate(R.id.relocationLaserFragment,
                            Bundle().apply {
                                putInt(NAVIGATE_ID, data.id)
                                putInt(NAVIGATE_MAP_TYPE, TARGET_MAP_TYPE)
                            })
                    }

                    override fun onExportButtonClick(data: SubMap, position: Int) {
                    }

                    override fun onImportButtonClick(data: SubMap, position: Int) {
                    }

                    override fun onDeleteButtonClick(data: SubMap, position: Int) {
                        DeleteRouteConfirmDialog(
                            requireContext(),
                            object :
                                DeleteRouteConfirmDialog.DeleteRouteDialogListener {
                                override fun confirm(dialog: DeleteRouteConfirmDialog) {
                                    MainScope().launch {
                                        lateinit var mapResult: MapResult
                                        withContext(Dispatchers.Default) {
                                            mapResult =
                                                deleteTarget.deletePointMap(listData[position].id)
                                        }
                                        withContext(Dispatchers.Main) {
                                            if (mapResult.isFlag) {
                                                ToastUtil.show(mapResult.msg)
                                                listData.removeAt(position)
                                                mAdapter.setData(listData)
                                            } else {
                                                // 另一个对话框
                                                DeleteRouteErrorDialog(
                                                    requireContext(),
                                                    "目标点"
                                                ).show()
                                            }
                                        }
                                    }
                                }

                                override fun cancel(dialog: DeleteRouteConfirmDialog) {
                                }
                            }, data.name?:"目标点"
                        ).show()
                    }
                })
            }
            adapter = mAdapter
        }


        binding.tvCreateTargetMap.apply {
            isClickable = true
            setOnClickListener {
                findNavController().navigate(R.id.relocationLaserFragment,
                    Bundle().apply {
                        putInt(NAVIGATE_ID, -1)
                        putInt(NAVIGATE_MAP_TYPE, TARGET_MAP_TYPE)
                    })
            }
        }


        MainScope().launch {
            withContext(Dispatchers.Default) {
                debugDao = DataBaseDeliveredRobotMap.getDatabase(
                    Objects.requireNonNull(
                        MyApplication.instance
                    )!!
                ).getDebug()
//                listData = debugDao.queryTargetPointMap()!!
                var mMapResult = mapTargetPointServiceImpl.maps
                var tempListData = mMapResult.data["maps"] as ArrayList<SubMap>
                listData.clear()
                listData.addAll(tempListData)

                withContext(Dispatchers.Main) {
                    //   当数据为空时页面的显示
                //listData = emptyArray<SubMap>().toList()
                    if (listData.isEmpty()) {
                        listData.addAll(tempListData)
                        binding.clRoadMapEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvGeneralEmpty.setText("暂无目标点，请先创建...")
                        binding.clRoadMapEmpty.visibility = View.GONE
                    }
                    mAdapter.setData(listData)
                }
            }
        }
    }
}