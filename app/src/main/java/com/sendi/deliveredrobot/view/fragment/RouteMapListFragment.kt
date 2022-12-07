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
import com.sendi.deliveredrobot.adapter.RoadMapAdapter
import com.sendi.deliveredrobot.databinding.FragmentRouteMapListBinding
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.RouteMap
import com.sendi.deliveredrobot.ros.debug.MapRouteServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.DeleteRouteConfirmDialog
import com.sendi.deliveredrobot.view.widget.DeleteRouteErrorDialog
import kotlinx.coroutines.*

/**
 * @author csw
 * @describe 路径图
 * @date 2021/9/26
 */
class RouteMapListFragment : Fragment() {
    lateinit var binding: FragmentRouteMapListBinding
    lateinit var mAdapter: RoadMapAdapter
    var listData: ArrayList<RouteMap> = arrayListOf()
    lateinit var deleteRoute: MapRouteServiceImpl
    lateinit var mDebugDao: DebugDao
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route_map_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deleteRoute = MapRouteServiceImpl()
        binding = DataBindingUtil.bind(view)!!
        binding.recyclerViewLaserList.apply {
            mAdapter = RoadMapAdapter(context, null).apply {
                layoutManager = LinearLayoutManager(requireContext())
                setOnItemClickListener(object : RoadMapAdapter.OnItemClickCallback {
                    override fun onItemClick(data: RouteMap, position: Int) {
//                        findNavController().navigate(R.id.relocationLaserFragment,
//                            Bundle().apply {
//                                putInt(NAVIGATE_ID, data.id)
//                                putInt(NAVIGATE_MAP_TYPE, ROUTE_MAP_TYPE)
//                            })

                        MainScope().launch {
                            withContext(Dispatchers.Default) {
                                var mRouteMap = mDebugDao.queryRouteId(data.id)
                                var selectSubMapId = mRouteMap?.subMapId?:-1
                                var selectSubMap = mDebugDao.querySubMap(selectSubMapId)
                                var selectSubMapName = selectSubMap?.name?:""
                                findNavController().navigate(R.id.createRouteMapFragment,
                                    Bundle().apply {
                                        putInt(NAVIGATE_ID, data.id)
                                        putInt(NAVIGATE_MAP_ID, selectSubMapId)
                                        putString(NAVIGATE_MAP_NAME, selectSubMapName)
                                    })
                            }
                        }


                    }

                    override fun onExportButtonClick(data: RouteMap, position: Int) {
                    }

                    override fun onImportButtonClick(data: RouteMap, position: Int) {
                    }

                    override fun onDeleteButtonClick(data: RouteMap, position: Int) {
                        DeleteRouteConfirmDialog(requireContext(),deleteRouteConfirmDialogListener = object :DeleteRouteConfirmDialog.DeleteRouteDialogListener{
                            override fun confirm(dialog: DeleteRouteConfirmDialog) {
                                MainScope().launch {
                                    lateinit var mapResult : MapResult
                                    withContext(Dispatchers.Default) {
                                        mapResult = deleteRoute.deleteRoute(listData[position].id)
                                    }
                                    withContext(Dispatchers.Main) {
                                        if (mapResult.isFlag) {
                                            ToastUtil.show(mapResult.msg)
                                            listData.removeAt(position)
                                            mAdapter.setData(listData)
                                        } else {
                                            // 另一个对话框
                                            DeleteRouteErrorDialog(requireContext(),"路径图").show()
                                        }
                                    }
                                }
                            }

                            override fun cancel(dialog: DeleteRouteConfirmDialog) {
                            }
                        }
                        ,data.name?:"路径图").show()
                    }
                })
            }
            adapter = mAdapter
        }
        binding.tvCreateMap.apply {
            setText("创建路径图")
            isClickable = true
            setOnClickListener {
                findNavController().navigate(R.id.relocationLaserFragment,
                    Bundle().apply {
                        putInt(NAVIGATE_ID, -1)
                        putInt(NAVIGATE_MAP_TYPE, ROUTE_MAP_TYPE)
                    })
            }
        }

        MainScope().launch {
            withContext(Dispatchers.Default) {
                mDebugDao =
                    DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDebug()
                var temp = mDebugDao.queryMapRoute() as ArrayList<RouteMap>
                if(temp != null){
                    listData.clear()
                    listData.addAll(temp)
                }
//              当数据为空时页面的显示
//              listData = emptyArray<RouteMap>().toList()
                withContext(Dispatchers.Main) {
                    if (listData.isEmpty()) {
                        binding.clRoadMapEmpty.visibility = View.VISIBLE
                    } else {
                        binding.clRoadMapEmpty.visibility = View.GONE
                    }
                }
            }
            mAdapter.setData(listData)
        }
    }
}