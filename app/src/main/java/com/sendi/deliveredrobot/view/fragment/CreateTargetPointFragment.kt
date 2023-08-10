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
import com.sendi.deliveredrobot.adapter.TargetPointAdapter
import com.sendi.deliveredrobot.databinding.FragmentCreateTargetPointBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.PointType
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.Point
import com.sendi.deliveredrobot.room.entity.PublicArea
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.MapTargetPointServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.AddTargetPointDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTargetPointFragment : Fragment() {

    lateinit var binding: FragmentCreateTargetPointBinding
    lateinit var mAdapter: TargetPointAdapter
    var areaListData: ArrayList<PublicArea> = arrayListOf()
    private val mapTargetPointServiceImpl = MapTargetPointServiceImpl.getInstance()
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()

    /** 关联的激光地图id */
    private var selectSubMapId: Int = -1

    /** 目标点图id  -1:表示创建 */
    private var targetMapId: Int = -1
    var selectSubMapName: String = ""
    lateinit var mAddDialog: AddTargetPointDialog
    lateinit var mSubMapListData: List<SubMap>
    var targetPoint: ArrayList<Point> = ArrayList()
    var areaTargetPoint: ArrayList<Point> = ArrayList()

    /** 地图模式 0:默认为地图模式   1：列表模式*/
    var targetMode: Int = 0
    var currentAreaId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_target_point, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        targetMapId = arguments?.getInt(NAVIGATE_ID, -1)!!
        selectSubMapId = arguments?.getInt(NAVIGATE_MAP_ID, -1)!!
        selectSubMapName = arguments?.getString(NAVIGATE_MAP_NAME, "") ?: ""

        binding.tvSelecedMap.text = selectSubMapName
        binding.textView27.setText("创建目标点图")

        binding.tvGoback.apply {
            isClickable = true
            setOnClickListener {
                isEnabled = false
                findNavController().popBackStack()
            }
        }

        binding.tvSave.apply {
            setOnClickListener {
                MainScope().launch {
                    withContext(Dispatchers.Default) {
                        DialogHelper.loadingDialog.show()
                        var mMapResult: MapResult = mapTargetPointServiceImpl.save()
                        ToastUtil.show(mMapResult.msg)

                        //结束处理路径和目标点
                        var mLaserMapResult: MapResult =
                            mapLaserServiceImpl.sendLaserMapManagerMsg(3)
                        if (!mLaserMapResult.isFlag()) {
                            LogUtil.e(mLaserMapResult.msg)
                            ToastUtil.show(mLaserMapResult.msg)
                        }
                        DialogHelper.loadingDialog.dismiss()
                        withContext(Dispatchers.Main) {
                            if (mMapResult.isFlag) {
                                findNavController().popBackStack(R.id.debuggingFragment, false)
                            }
                        }
                    }
                }
            }
        }
        binding.tvModeSwitch.apply {
            setOnClickListener {
                if (targetMode == 0) {
                    targetMode = 1
                    binding.groupListMode.visibility = View.VISIBLE
                    binding.laserPointsView.visibility = View.GONE
                    binding.tvMark.visibility = View.GONE

                    MainScope().launch {
                        withContext(Dispatchers.Default) {
                            if (currentAreaId != -1) {
                                areaTargetPoint.clear()
                                var getPointMapResult = mapTargetPointServiceImpl.getTotalPoints(
                                    selectSubMapId,
                                    currentAreaId
                                )
                                var tempPointList =
                                    getPointMapResult.data["pointList"] as ArrayList<Point>
                                areaTargetPoint.clear()
                                if (tempPointList != null) {
                                    areaTargetPoint.addAll(tempPointList)
                                }
                                withContext(Dispatchers.Main) {
                                    mAdapter.setData(areaTargetPoint)
                                }
                            }
                        }
                    }
                } else {
                    targetMode = 0
                    binding.groupListMode.visibility = View.GONE
                    binding.laserPointsView.visibility = View.VISIBLE
                    binding.tvMark.visibility = View.VISIBLE
                }
            }
        }

        //标记新目标点
        binding.tvMark.apply {
            setOnClickListener {
                mAddDialog = AddTargetPointDialog(
                    requireContext(),
                    selectSubMapId,
                    areaListData,
                    0,
                    null,
                    object :
                        AddTargetPointDialog.AddTargetPointDialogListener {
                        override fun confirm(newPoint: Point, range: Double) {
                            MainScope().launch {
                                withContext(Dispatchers.Default) {
                                    if (newPoint.type == PointType.LIFT_AXIS) {
                                        //如果是电梯锚点，判断数据库中或当前队列中是否已存在电梯锚点
                                        when (dao.existAxisPointBySubMapId(newPoint.subMapId?:-1, newPoint.elevator)) {
                                            true -> {
                                                ToastUtil.show(resources.getString(R.string.mark_point_fail))
                                                return@withContext
                                            }
                                            false -> {
                                                //检测
                                                val exist = targetPoint.filter {
                                                    it.type == PointType.LIFT_AXIS
                                                }.any{
                                                    it.elevator == newPoint.elevator
                                                }
                                                if(exist){
                                                    ToastUtil.show(resources.getString(R.string.mark_point_fail))
                                                    return@withContext
                                                }
                                            }
                                        }
                                    }
                                    DialogHelper.loadingDialog.show()

                                    val mMapResult = mapTargetPointServiceImpl.signPoint(newPoint, range)
                                    withContext(Dispatchers.Main) {
                                        ToastUtil.show(mMapResult.msg)
                                        if (mMapResult.isFlag) {
                                            when (newPoint.type) {
                                                PointType.LIFT_OUTSIDE -> {
                                                    val inside = (mMapResult.data["inside"] as Point)
                                                    val outside = mMapResult.data["outside"] as Point
                                                    targetPoint.add(inside)
                                                    targetPoint.add(outside)
                                                }
                                                else -> {
                                                    val tempPoint = mMapResult.data["point"] as Point
                                                    targetPoint.add(tempPoint)
                                                }
                                            }
                                            binding.laserPointsView.setTargetPoints(targetPoint)
                                            mAddDialog.dismiss()
                                        } else {
                                            mAddDialog.setTips(mMapResult.msg)
                                        }
                                    }
                                    DialogHelper.loadingDialog.dismiss()
                                }
                            }
                        }

                        override fun delete(newPoint: Point) {

                        }

                        override fun cancel() {

                        }
                    })
                mAddDialog.show()
            }
        }
        binding.tablayTargetSort.apply {
            setOnTabChangeListener {
                currentAreaId = areaListData[it].id
                MainScope().launch {
                    withContext(Dispatchers.Default) {
                        var getPointMapResult =
                            mapTargetPointServiceImpl.getTotalPoints(selectSubMapId, currentAreaId)
                        var temp = getPointMapResult.data["pointList"] as ArrayList<Point>
                        areaTargetPoint.clear()
                        if (temp != null) {
                            areaTargetPoint.addAll(temp)
                        }
                        withContext(Dispatchers.Main) {
                            mAdapter.setData(areaTargetPoint)
                        }
                    }
                }
            }
        }

        binding.rcyTargetPoint.apply {
            mAdapter = TargetPointAdapter(context, null).apply {
                layoutManager = GridLayoutManager(requireContext(), 5)
                setOnItemClickListener(object : TargetPointAdapter.OnItemClickCallback {
                    override fun onItemClick(data: Point, position: Int) {

                    }

                    override fun onItemLongClick(data: Point, position: Int) {
                        mAddDialog = AddTargetPointDialog(
                            requireContext(),
                            selectSubMapId,
                            areaListData,
                            1,
                            data,
                            object :
                                AddTargetPointDialog.AddTargetPointDialogListener {
                                override fun confirm(mPoint: Point, range: Double) {
                                    MainScope().launch {
                                        withContext(Dispatchers.Default) {
                                            if (mPoint.type == PointType.LIFT_AXIS) {
                                                //如果是电梯锚点，判断数据库中或当前队列中是否已存在电梯锚点
                                                when (dao.existAxisPointBySubMapId(mPoint.subMapId?:-1, mPoint.elevator)) {
                                                    true -> {
                                                        LogUtil.i("heky:true")
                                                        val point = dao.queryPointByElevatorAndTypeAndSubMapId( PointType.LIFT_AXIS, mPoint.elevator,mPoint.subMapId?:-1)
                                                        if (point.pointId != mPoint.id) {
                                                            ToastUtil.show(resources.getString(R.string.mark_point_fail))
                                                            return@withContext
                                                        }
                                                    }
                                                    false -> {
                                                        LogUtil.i("heky:false")
                                                        val exist = targetPoint.filter {
                                                            it.type == PointType.LIFT_AXIS
                                                        }.any{
                                                            it.id == mPoint.id
                                                        }
                                                        if(exist){
                                                            val point = dao.queryPointByElevatorAndTypeAndSubMapId( PointType.LIFT_AXIS, mPoint.elevator,mPoint.subMapId?:-1)
                                                            if (point.elevator == mPoint.elevator) {
                                                                ToastUtil.show(resources.getString(R.string.mark_point_fail))
                                                                return@withContext
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            DialogHelper.loadingDialog.show()
                                            var mMapResult =
                                                mapTargetPointServiceImpl.resetPoint(mPoint)
                                            withContext(Dispatchers.Main) {
                                                ToastUtil.show(mMapResult.msg)
                                                if (mMapResult.isFlag) {
//                                                    mMapResult.data.get("point") as Point
                                                    areaTargetPoint.clear()
                                                    var getPointMapResult =
                                                        mapTargetPointServiceImpl.getTotalPoints(
                                                            selectSubMapId,
                                                            currentAreaId
                                                        )
                                                    var tempPointList =
                                                        getPointMapResult.data["pointList"] as ArrayList<Point>
                                                    areaTargetPoint.clear()
                                                    if (tempPointList != null) {
                                                        areaTargetPoint.addAll(tempPointList)
                                                    }
                                                    mAdapter.setData(areaTargetPoint)
                                                    mAddDialog.dismiss()
                                                } else {
                                                    mAddDialog.setTips(mMapResult.msg)
                                                }
                                            }
                                            DialogHelper.loadingDialog.dismiss()
                                        }
                                    }
                                }

                                override fun delete(editPoint: Point) {
                                    MainScope().launch {
                                        withContext(Dispatchers.Default) {
                                            DialogHelper.loadingDialog.show()
                                            var mMapResult =
                                                mapTargetPointServiceImpl.deletePoint(editPoint)
                                            withContext(Dispatchers.Main) {
                                                ToastUtil.show(mMapResult.msg)
                                                if (mMapResult.isFlag) {
                                                    targetPoint.remove(editPoint)
                                                    areaTargetPoint.remove(editPoint)
                                                    binding.laserPointsView.setTargetPoints(
                                                        targetPoint
                                                    )
                                                    mAdapter.setData(areaTargetPoint)
                                                    mAddDialog.dismiss()
                                                } else {
                                                    mAddDialog.setTips(mMapResult.msg)
                                                }
                                            }
                                            DialogHelper.loadingDialog.dismiss()
                                        }
                                    }
                                }

                                override fun cancel() {

                                }
                            })
                        mAddDialog.show()
                    }
                })
            }
            adapter = mAdapter
        }



        MainScope().launch {
            withContext(Dispatchers.Default) {
                areaListData.clear()
//                var mMapResult = mapTargetPointServiceImpl.types
//                var templistData = mMapResult.data.get("typeList") as ArrayList<PublicArea>
                val templistData = dao.queryPublicArea()
                areaListData.addAll(templistData)

                val mDebugDao: DebugDao =
                    DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDebug()
                var selectSubMapPath =
                    mDebugDao.selectLaserOriginalNameByName(selectSubMapName) ?: ""

                var getPointMapResult =
                    mapTargetPointServiceImpl.getOriginalPoints(selectSubMapId, -1)
                var temp = getPointMapResult.data["pointList"] as ArrayList<Point>
                if (temp != null) {
                    targetPoint.addAll(temp)
                }
                DialogHelper.loadingDialog.show()
                //显示激光地图
                val mapResult = mapLaserServiceImpl.showLaserMap(selectSubMapPath)
                DialogHelper.loadingDialog.dismiss()
                withContext(Dispatchers.Main) {
                    if (mapResult.isFlag) {
                        val staticMap = RosPointArrUtil.staticMap
                        val updateMap = RosPointArrUtil.updateMap
                        binding.laserPointsView.apply {
                            setStaticPoints(staticMap)
//                            setUpdatePoints(updateMap)
                            setTargetPoints(targetPoint)
                        }
                    }
                    val areaStrList = arrayOfNulls<String>(areaListData.size)
                    for (index in areaListData.indices) {
                        areaStrList[index] = areaListData[index].name
                    }
                    binding.tablayTargetSort.initTab(areaStrList, 0)
                }
                //开启激光地图
//                var mLaserMapResult:MapResult = mapLaserServiceImpl.sendLaserMapManagerMsg(2)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        SubManager.sub(ClientConstant.GLOBAL_LASER)
        LaserObject.clear()
        LaserObject.livePoints.observe(viewLifecycleOwner) {
            binding.laserPointsView.apply {
                if (it != null) {
                    val tempFloatArrayList = ArrayList<FloatArray>()
                    val tempFloatArray = FloatArray(it.size)
                    LogUtil.d("livePoints data:${it.size}")
                    for (index in it.indices) {
                        tempFloatArray[index] = it[index] / 100f
                    }
                    tempFloatArrayList.add(tempFloatArray)
                    setUpdatePoints(tempFloatArrayList)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SubManager.unsub(ClientConstant.GLOBAL_LASER)
        //结束处理路径和目标点
//        var mLaserMapResult:MapResult = mapLaserServiceImpl.sendLaserMapManagerMsg(3)
//        if (!mLaserMapResult.isFlag()) {
//            LogUtil.e(mLaserMapResult.msg)
//            ToastUtil.show(mLaserMapResult.msg)
//        }
        var emptyRoutePoint:ArrayList<FloatArray> = ArrayList()
        binding.laserPointsView.apply {
            setRoutePoints(emptyRoutePoint)
        }
    }

}