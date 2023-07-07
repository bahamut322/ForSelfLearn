//package com.sendi.deliveredrobot.view.fragment
//
//import android.annotation.SuppressLint
//import android.app.Dialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.databinding.DataBindingUtil
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.sendi.deliveredrobot.R
//import com.sendi.deliveredrobot.adapter.SelectChargePointListAdapter
//import com.sendi.deliveredrobot.adapter.SelectMapListAdapter
//import com.sendi.deliveredrobot.databinding.FragmentMapSettingBinding
//import com.sendi.deliveredrobot.helpers.DialogHelper
//import com.sendi.deliveredrobot.helpers.ROSHelper
//import com.sendi.deliveredrobot.helpers.UploadMapHelper
//import com.sendi.deliveredrobot.navigationtask.RobotStatus
//import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
//import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
//import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
//import com.sendi.deliveredrobot.room.entity.MapConfig
//import com.sendi.deliveredrobot.utils.LogUtil
//import com.sendi.deliveredrobot.utils.ToastUtil
//import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
//import com.sendi.deliveredrobot.viewmodel.MapSettingViewModel
//import com.sendi.deliveredrobot.viewmodel.SendPlaceBin1ViewModel
//import com.sendi.deliveredrobot.viewmodel.SendPlaceBin2ViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.MainScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class MapSettingFragment : Fragment() {
//    private lateinit var binding: FragmentMapSettingBinding
//    lateinit var selectMapMapListAdapter: SelectMapListAdapter
//    lateinit var selectChargePointMapListAdapter: SelectChargePointListAdapter
//    lateinit var dao: DeliveredRobotDao
//    lateinit var selectMapDialog: Dialog
//    lateinit var selectChargePointDialog: Dialog
//    private val mapSettingViewModel by viewModels<MapSettingViewModel>()
//    private val sendPlaceBin1ViewModel by viewModels<SendPlaceBin1ViewModel>({ requireActivity() })
//    private val sendPlaceBin2ViewModel by viewModels<SendPlaceBin2ViewModel>({ requireActivity() })
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_map_setting, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding = DataBindingUtil.bind(view)!!
//        binding.data = mapSettingViewModel
//        binding.lifecycleOwner = this
//        MainScope().launch {
//            withContext(Dispatchers.Default) {
//                dao = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
//                val rootMapList = dao.queryRootMap()
//                val currentRootMapId = dao.queryMapConfig()
//                rootMapList?.map {
//                    it.selected = it.id == currentRootMapId
//                }
//                selectMapMapListAdapter = SelectMapListAdapter(rootMapList)
//                selectMapMapListAdapter.apply {
//                    setOnItemClickListener(object : SelectMapListAdapter.OnItemClickCallback {
//                        override fun onItemClick(position: Int) {
//                            data.map {
//                                it.selected = false
//                            }
//                            data[position].selected = true
//                            selectMapMapListAdapter.setData(data)
//                            mapSettingViewModel.currentMapName.value = data[position].name
//                            MainScope().launch {
//                                withContext(Dispatchers.Default) {
//                                    dao.updateMapConfig(MapConfig(1, data[position].id, null))
//                                }
//                            }
//                            mapSettingViewModel.currentChargePile.value = ""
//                            selectMapDialog.dismiss()
//                            sendPlaceBin1ViewModel.data.clear()
//                            sendPlaceBin2ViewModel.data.clear()
//                            RobotStatus.originalLocation = null
//                        }
//                    })
//                }
//                val chargePointList = dao.queryChargePointList()
//                val currentChargePoint = dao.queryChargePoint()
//                chargePointList?.map {
//                    it.selected = it.pointId == currentChargePoint?.pointId
//                }
//                selectChargePointMapListAdapter = SelectChargePointListAdapter(chargePointList)
//                selectChargePointMapListAdapter.apply {
//                    setOnItemClickListener(object :
//                        SelectChargePointListAdapter.OnItemClickCallback {
//                        override fun onItemClick(position: Int) {
//                            data.map {
//                                it.selected = false
//                            }
//                            data[position].selected = true
//                            selectChargePointMapListAdapter.setData(data)
//                            mapSettingViewModel.currentChargePile.value =
//                                data[position].pointName
//                            MainScope().launch {
//                                withContext(Dispatchers.Default) {
//                                    dao.updateMapConfig(
//                                        MapConfig(
//                                            1,
//                                            data[position].rootMapId,
//                                            data[position].pointId
//                                        )
//                                    )
//                                    val queryPoint =
//                                        dao.queryChargePoint()
//                                    RobotStatus.originalLocation = queryPoint
//                                    RobotStatus.currentLocation = RobotStatus.originalLocation
//                                    if (queryPoint != null) {
//                                        var retryTime = 10  // 设置地图次数
//                                        DialogHelper.loadingDialog.show()
//                                        //切换地图
//                                        var switchMapResult: Boolean
//                                        do {
//                                            switchMapResult = ROSHelper.setNavigationMap(
//                                                queryPoint.subPath ?: "",
//                                                queryPoint.routePath ?: ""
//                                            )
//                                            retryTime--
//                                        } while (!switchMapResult && retryTime > 0)
//                                        ROSHelper.setPoseClient(queryPoint)
//                                        //查看切换锚点是否成功
//                                        var result: Boolean
//                                        do {
//                                            result =
//                                                ROSHelper.getParam("/finish_update_pose") == "1"
//                                            if (!result) virtualTaskExecute(2, "设置页查看锚点")
//                                            retryTime--
//                                        } while (!result && retryTime > 0)
//                                        if (retryTime <= 0) {
//                                            ToastUtil.show("设置地图失败")
//                                        } else {
//                                            LogUtil.i("finish_update_pose成功")
//                                        }
//                                        UploadMapHelper.uploadMap()
//                                        DialogHelper.loadingDialog.dismiss()
//                                    }
//                                }
//                            }
//                            selectChargePointDialog.dismiss()
//                            sendPlaceBin1ViewModel.data.clear()
//                            sendPlaceBin2ViewModel.data.clear()
//                        }
//                    })
//                }
//                withContext(Dispatchers.Main) {
//                    val currentMapName: String
//                    withContext(Dispatchers.Default) {
//                        currentMapName = dao.queryCurrentMapName() ?: ""
//                    }
//                    mapSettingViewModel.currentMapName.value = currentMapName
//                    mapSettingViewModel.currentChargePile.value =
//                        currentChargePoint?.pointName ?: ""
//                    selectMapDialog = initSelectMapDialog()
//                    selectChargePointDialog = initSelectChargePointDialog()
//                    binding.textViewChooseMap.apply {
//                        setOnClickListener {
//                            selectMapDialog.show()
//                        }
//                    }
//                    binding.textViewChoosePlace.apply {
//                        setOnClickListener {
//                            MainScope().launch {
//                                withContext(Dispatchers.Default) {
//                                    val newChargePointList = dao.queryChargePointList()
//                                    val newCurrentChargePoint = dao.queryChargePoint()
//                                    newChargePointList?.map {
//                                        it.selected = it.pointId == newCurrentChargePoint?.pointId
//                                    }
//                                    withContext(Dispatchers.Main) {
//                                        selectChargePointMapListAdapter.setData(newChargePointList)
//                                    }
//                                }
//                                selectChargePointDialog.show()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("InflateParams")
//    private fun initSelectMapDialog(): Dialog {
//        val mWindowWidth: Int
//        val mWindowHeight: Int
//        val dialog = HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
//        val dialogView: View = LayoutInflater.from(requireContext())
//            .inflate(R.layout.dialog_select_list, null)
//        val displayMetrics = resources.displayMetrics
//        mWindowWidth = displayMetrics.widthPixels
//        mWindowHeight = displayMetrics.heightPixels
//        dialogView.findViewById<RecyclerView>(R.id.recyclerViewList).apply {
//            adapter = selectMapMapListAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//        }
//        dialogView.findViewById<ImageView>(R.id.imageViewDialogCancel).apply {
//            setOnClickListener {
//                dialog.dismiss()
//            }
//        }
//        dialog.setContentView(
//            dialogView, ViewGroup.MarginLayoutParams(
//                mWindowWidth,
//                mWindowHeight
//            )
//        )
//        return dialog
//    }
//
//    @SuppressLint("InflateParams")
//    private fun initSelectChargePointDialog(): Dialog {
//        val mWindowWidth: Int
//        val mWindowHeight: Int
//        val dialog = HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
//        val dialogView: View = LayoutInflater.from(requireContext())
//            .inflate(R.layout.dialog_select_list, null)
//        val displayMetrics = resources.displayMetrics
//        mWindowWidth = displayMetrics.widthPixels
//        mWindowHeight = displayMetrics.heightPixels
//        dialogView.findViewById<RecyclerView>(R.id.recyclerViewList).apply {
//            adapter = selectChargePointMapListAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//        }
//        dialogView.findViewById<ImageView>(R.id.imageViewDialogCancel).apply {
//            setOnClickListener {
//                dialog.dismiss()
//            }
//        }
//        dialog.setContentView(
//            dialogView, ViewGroup.MarginLayoutParams(
//                mWindowWidth,
//                mWindowHeight
//            )
//        )
//        return dialog
//    }
//}