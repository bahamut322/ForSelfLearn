package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.CommonSelListDataInfoAdapter
import com.sendi.deliveredrobot.databinding.FragmentRelocationLaserBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.CommonListDataModel
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.FixOperateEnum
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.MyCustomPopupWin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @describe 重定位激光页面
 */
class RelocationLaserFragment : Fragment() {
    private lateinit var binding: FragmentRelocationLaserBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
//    private var staticMap: List<Point32>? = null
    private var staticMap: ArrayList<FloatArray>? = null
    private val angelArray = arrayOf(1, 5, 10, 20, 50)
    /** 入口地图重定向的类型  */
    private var mapType: Int = 0
    private var mapId: Int = -1
    var mLaserMapAdapter: CommonSelListDataInfoAdapter? = null
    private var laserMapList: ArrayList<CommonListDataModel> = ArrayList()
    var selectSubMapId: Int = -1
    var selectSubMapName: String = ""
    var selectSubMapIndex = -1
    private lateinit var mSubMapListData: List<SubMap>

    override fun onStart() {
        super.onStart()
        SubManager.sub(ClientConstant.MAPPING_POSE)
        LaserObject.liveRobotPose.observe(viewLifecycleOwner){
            binding.laserPointsView.apply {
                if (it != null) {
                    setRobotPose(it)
                }
                invalidate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SubManager.unsub(ClientConstant.MAPPING_POSE)
        LaserObject.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_relocation_laser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        //判断地图入口类型
        mapType = arguments?.getInt(NAVIGATE_MAP_TYPE, 0) ?: 0
        mapId = arguments?.getInt(NAVIGATE_ID, -1) ?: -1


        //路径图和目标点入口
        if (mapType == ROUTE_MAP_TYPE || mapType == TARGET_MAP_TYPE || mapType == LIMIT_SPEED_TYPE || mapType == VIRTUAL_WALL_TYPE) {
            binding.groupSelectMap.visibility = View.VISIBLE
            binding.groupLaserMap.visibility = View.GONE
            when (mapType) {
                TARGET_MAP_TYPE -> {
                    binding.textView27.text = "目标点重定位页面"
                }
                LIMIT_SPEED_TYPE -> {
                    binding.textView27.text = "限速区重定位页面"
                }
                VIRTUAL_WALL_TYPE -> {
                    binding.textView27.text = "虚拟墙重定位页面"
                }
            }
        }

        MainScope().launch {
            withContext(Dispatchers.Default) {
                //路径图入口
                if (mapType == ROUTE_MAP_TYPE || mapType == TARGET_MAP_TYPE || mapType == LIMIT_SPEED_TYPE || mapType == VIRTUAL_WALL_TYPE) {

                    mLaserMapAdapter =
                        context?.let { CommonSelListDataInfoAdapter(it, laserMapList) }

                    val mDebugDao: DebugDao =
                        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDebug()
                    var subMapId = -1
                    //路径地图编辑 查询编辑路径地图信息
                    if ((mapType == ROUTE_MAP_TYPE || mapType == TARGET_MAP_TYPE || mapType == LIMIT_SPEED_TYPE || mapType == VIRTUAL_WALL_TYPE)&& mapId != -1) {
                        when(mapType){
                            ROUTE_MAP_TYPE -> {
                                val mRouteMap = mDebugDao.queryRouteId(mapId)
                                subMapId = mRouteMap?.subMapId ?: -1
                            }
                            TARGET_MAP_TYPE -> {
                                subMapId = mapId
                            }
                            LIMIT_SPEED_TYPE -> {
                                subMapId = mapId
                            }
                            VIRTUAL_WALL_TYPE -> {
                                subMapId = mapId
                            }
                        }

                    }
                    //查询所有激光地图
                    mSubMapListData = mDebugDao.queryMapSubList()
                    for ((index, item) in mSubMapListData.withIndex()) {
                        val mCommonListDataModel =
                            CommonListDataModel(item.id.toString(), item.name ?: "")
                        //路径地图编辑 对应的激光地图下标
                        if (subMapId != -1 && subMapId == item.id) {
                            selectSubMapIndex = index
                        }
                        laserMapList.add(mCommonListDataModel)
                    }
                    withContext(Dispatchers.Main) {
                        if (selectSubMapIndex != -1) {
                            //路径地图编辑 对应的激光地图名字，编辑的时候地图选择不可点击
                            mLaserMapAdapter?.setCurSel(selectSubMapIndex)
                            binding.rlSelMap.apply {
                                isClickable = false
                            }
                            binding.tvSelecedMap.text = laserMapList[selectSubMapIndex].value
                            selectSubMapId = laserMapList[selectSubMapIndex].key.toInt()
                            selectSubMapName = laserMapList[selectSubMapIndex].value
                        }
                        mLaserMapAdapter?.setData(laserMapList)
                    }
                }
                //获取重定位地图数据
                getMapData()
            }
        }


        binding.rlSelMap.apply {
            setOnClickListener {
                DialogHelper.setMyCommonPopupWinAndShow(
                    MyCustomPopupWin.Builder(context)
                        .contentView(R.layout.view_popupwin_commo_sel_list)
                        .dropDownView(binding.rlSelMap)
                        .yOff(10)
                        .width(binding.rlSelMap.width)
                        .height(binding.laserPointsView.height)
                        .handlerBuilder(object : MyCustomPopupWin.OnHandlerBuilderDelegate {
                            override fun handlerBuilder(view: View?) {
                                val recyclerInfo: RecyclerView =
                                    view!!.findViewById(R.id.recyclerv_data)
                                recyclerInfo.layoutManager = LinearLayoutManager(activity) //线性
                                recyclerInfo.adapter = mLaserMapAdapter
                                mLaserMapAdapter?.setOnSelChangeListener(object :
                                    CommonSelListDataInfoAdapter.OnSelChangeListener {
                                    override fun onSelChange(
                                        position: Int,
                                        selDataInfo: CommonListDataModel?
                                    ) {
                                        if (position != -1 && selDataInfo != null) {
                                            selectSubMapIndex = position
                                            selectSubMapId = selDataInfo.key.toInt()
                                            selectSubMapName = selDataInfo.value
                                            binding.tvSelecedMap.text = selDataInfo.value
                                            DialogHelper.hideCommonPopupWinAndDestroy()
                                            MainScope().launch {
                                                withContext(Dispatchers.Default) {
                                                    getMapData()
                                                }
                                            }
                                        }
                                    }
                                })
                            }
                        })
                )
            }
        }
//        DialogHelper.hideCommonPopupWinAndDestroy()
        binding.spinner.apply {
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_spinner_relocation_laser,
                R.id.textViewContent,
                angelArray
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_relocation_laser)
            }
            adapter = arrayAdapter
            setSelection(0)
        }
//        binding.viewLeft.apply {
        binding.viewRight.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.LEFT_MOVE,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
//        binding.viewRight.apply {
        binding.viewLeft.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.RIGHT_MOVE,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
//        binding.viewUp.apply {
        binding.viewDown.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.UP_MOVE,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
//        binding.viewDown.apply {
        binding.viewUp.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.DOWN_MOVE,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
        binding.viewLeftRotate.apply {
//        binding.viewRightRotate.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.TURN_LEFT,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
        binding.viewRightRotate.apply {
//        binding.viewLeftRotate.apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch {
                    val mapResult: MapResult?
                    withContext(Dispatchers.Default) {
                        mapResult = mapLaserServiceImpl.fix(
                            FixOperateEnum.TURN_RIGHT,
                            binding.spinner.selectedItem as Int
                        )
                    }
                    if (mapResult?.isFlag == true) {
                        try {
                            //val updateMap = mapResult.data["updateMap"] as List<Point32>
                            val updateMap = RosPointArrUtil.updateMap
                            binding.laserPointsView.apply {
                                //setLaserPoints(updateMap)
                                setUpdatePoints(updateMap)
                            }
                        } catch (e: Exception) {
                            LogUtil.e(e.toString())
                        }
                    }
                }
            }
        }
        binding.textViewScan.apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                //清除缓存
                LaserObject.clear()
                findNavController().navigate(R.id.laserScanFragment, Bundle().apply {
                    putString(KEY_NAVIGATE_FROM, NAVIGATE_TO_LASER_SCAN_EDIT)
                })
            }
        }
        binding.textViewFix.apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                //清除缓存
                LaserObject.clear()
                mapLaserServiceImpl.continueFix()
                findNavController().navigate(R.id.fixLaserMapFragment, Bundle().apply {
                    putString(KEY_NAVIGATE_FROM, NAVIGATE_FROM_RELOCATION)
                })
            }
        }
        binding.textViewGoback.apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                findNavController().popBackStack()
            }
        }

        binding.tvNext.apply {
            setOnClickListener {
                if (selectSubMapId != -1) {
                    //清除缓存
                    LaserObject.clear()
                    when (mapType) {
                        ROUTE_MAP_TYPE -> {
                            findNavController().navigate(R.id.createRouteMapFragment,
                                Bundle().apply {
                                    putInt(NAVIGATE_ID, mapId)
                                    putInt(NAVIGATE_MAP_ID, selectSubMapId)
                                    putString(NAVIGATE_MAP_NAME, selectSubMapName)
                                })
                        }
                        TARGET_MAP_TYPE -> {
                            findNavController().navigate(R.id.createTargetSortFragment,
                                Bundle().apply {
                                    putInt(NAVIGATE_ID, mapId)
                                    putInt(NAVIGATE_MAP_ID, selectSubMapId)
                                    putString(NAVIGATE_MAP_NAME, selectSubMapName)
                                })
                        }
                        LIMIT_SPEED_TYPE -> {
                            findNavController().navigate(R.id.limitSpeedHomeFragment,
                                Bundle().apply {
                                    putSerializable(SUB_MAP, mSubMapListData[selectSubMapIndex])
                                })
                        }
                        VIRTUAL_WALL_TYPE -> {
                            findNavController().navigate(R.id.virtualWallHomeFragment,
                                Bundle().apply {
                                    putSerializable(SUB_MAP, mSubMapListData[selectSubMapIndex])
                                })
                        }
                    }
                } else {
                    ToastUtil.show(getString(R.string.please_choose_laser))
                }
            }
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        SubManager.unsub(ClientConstant.MAPPING_POSE)
//        LaserObject.clear()
//    }

    suspend fun getMapData() {
        var mapResultRedirect: MapResult? = null
        DialogHelper.loadingDialog.show()
        if (mapType == ROUTE_MAP_TYPE || mapType == TARGET_MAP_TYPE || mapType == LIMIT_SPEED_TYPE || mapType == VIRTUAL_WALL_TYPE) {
            if (selectSubMapIndex != -1) {
                //路径地图、目标点重定位流程 开始获取激光地图数据
                mapResultRedirect =
                    mapLaserServiceImpl.redirect(2, mSubMapListData[selectSubMapIndex].path)
            }
        } else {
            //激光地图重定位流程 开始获取激光地图数据
            mapResultRedirect = mapLaserServiceImpl.redirect(1)
        }
        DialogHelper.loadingDialog.dismiss()
        if (mapResultRedirect != null && mapResultRedirect.isFlag) {
            //          staticMap = mapResultRedirect.data["staticMap"] as List<Point32>
            //          val updateMap = mapResultRedirect.data["updateMap"] as List<Point32>
            staticMap = RosPointArrUtil.staticMap
            val updateMap = RosPointArrUtil.updateMap
            if (staticMap != null) {
                withContext(Dispatchers.Main) {
                    binding.laserPointsView.apply {
                        setStaticPoints(staticMap!!)
                        //                          setLaserPoints(updateMap)
                        setUpdatePoints(updateMap)
                    }
                }
            }
        }
    }
}