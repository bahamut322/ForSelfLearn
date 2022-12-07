package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.databinding.FragmentCreateRouteMapBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.MapRouteServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.RouteCompleteDialog
import kotlinx.coroutines.*
import label_msgs.PathMapInfo

/**
 * @author lsz
 * @data 2021-09-29
 * @describe 创建路径图
 */
class CreateRouteMapFragment : Fragment() {
    private lateinit var binding: FragmentCreateRouteMapBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    /** 关联的激光地图id */
    private var selectSubMapId : Int = -1
    private var selectSubMapName : String = ""
    private var selectSubMapPath : String = ""
    lateinit var mMapRouteServiceImpl: MapRouteServiceImpl
    var isStart : Boolean = false
    /** 路径图id  -1:表示创建 */
    private var routeMapId : Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_route_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        mMapRouteServiceImpl = MapRouteServiceImpl.getInstance()
        routeMapId = arguments?.getInt(NAVIGATE_ID,-1)!!
        selectSubMapId = arguments?.getInt(NAVIGATE_MAP_ID,-1)!!
        selectSubMapName = arguments?.getString(NAVIGATE_MAP_NAME,"")?:""
        binding.tvSelecedMap.text = selectSubMapName


//        DialogHelper.loadingDialog.show()
//        MainScope().launch(Dispatchers.Default) {
//            if (!mapLaserServiceImpl.sendLaserMapManagerMsg(2).isFlag) {
//                ToastUtil.show("开始处理路径和目标点失败")
//            }
//            virtualTaskExecute(5)
//            DialogHelper.loadingDialog.dismiss()
//        }
        MainScope().launch(Dispatchers.Default) {
            DialogHelper.loadingDialog.show()
                if (!mapLaserServiceImpl.sendLaserMapManagerMsg(2).isFlag) {
                    ToastUtil.show("开始处理路径和目标点失败")
                }
                virtualTaskExecute(5)
//                DialogHelper.loadingDialog.dismiss()
                val mDebugDao: DebugDao =
                    DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDebug()
                selectSubMapPath = mDebugDao.selectLaserOriginalNameByName(selectSubMapName)?:""
                val oldRoutePoint:ArrayList<FloatArray> = ArrayList()
                if(routeMapId != -1){
                    //路径地图不为-1 表示路径编辑
                    val mRouteMap = mDebugDao.queryRouteId(routeMapId)
                    val mMapResult = mMapRouteServiceImpl.queryPathMap(mRouteMap.path)
                    if(mMapResult.isFlag){
                        val pathMapInfoList:List<PathMapInfo> = mMapResult.data["data"] as List<PathMapInfo>
                        for (item in pathMapInfoList){
                            oldRoutePoint.add(floatArrayOf(item.x.toFloat(),item.y.toFloat()))
                        }
                    }
                }
//                DialogHelper.loadingDialog.show()
                //显示激光地图
                val mapResult = mapLaserServiceImpl.showLaserMap(selectSubMapPath)
                DialogHelper.loadingDialog.dismiss()
                withContext(Dispatchers.Main){
                    if (mapResult.isFlag) {
                        val staticMap = RosPointArrUtil.staticMap
                        binding.laserPointsView.apply {
                            setStaticPoints(staticMap)
                            setRoutePoints(oldRoutePoint)
                        }
                    }
                    if (routeMapId != -1){
                        binding.tvStart.visibility = View.GONE
                    }
                }
        }

        binding.textViewGoback.apply {
            isClickable = true
            setOnClickListener {
//                isEnabled = false
//                if(isStart){
                    //结束处理路径和目标点
                    val mMapResult:MapResult = mapLaserServiceImpl.sendLaserMapManagerMsg(3)
                    if (!mMapResult.isFlag) {
                        LogUtil.e(mMapResult.msg)
                        ToastUtil.show(mMapResult.msg)
                    }
                if(isStart)mMapRouteServiceImpl.exitRoute()
//                }
                findNavController().popBackStack()
            }
        }

        binding.tvStart.apply {
            setOnClickListener{
                if (!isStart){
                    DialogHelper.loadingDialog.show()
                    MainScope().launch {
                        withContext(Dispatchers.Default) {
                            //开始处理路径和目标点
//                            var mMapResult:MapResult = mapLaserServiceImpl.sendLaserMapManagerMsg(2)
//                            if (!mMapResult.isFlag()) {
//                                LogUtil.e(mMapResult.msg)
//                                ToastUtil.show(mMapResult.msg)
//                            }
                            val startMapResult =  mMapRouteServiceImpl.startRoute(selectSubMapPath)
                            withContext(Dispatchers.Main) {
                                if (startMapResult.isFlag){
                                    //开始推路径
                                    binding.tvStart.text = "结束"
                                    isStart = true
                                }else{
                                    LogUtil.e(startMapResult.msg)
                                    ToastUtil.show(startMapResult.msg)
                                }
                            }
                            delay(10000L)
                            DialogHelper.loadingDialog.dismiss()
                        }
                    }
                }else{
                    RouteCompleteDialog(requireContext(),object :
                        RouteCompleteDialog.RouteCompleteDialogListener{
                        override fun confirm(mapName:String) {
                            MainScope().launch {
                                withContext(Dispatchers.Default) {
                                    DialogHelper.loadingDialog.show()
                                    val mMapResult = mMapRouteServiceImpl.saveRoute(mapName,selectSubMapId)

                                    //结束处理路径和目标点
                                    val mLaserMapResult:MapResult = mapLaserServiceImpl.sendLaserMapManagerMsg(3)
                                    if (!mLaserMapResult.isFlag) {
                                        LogUtil.e(mLaserMapResult.msg)
                                        ToastUtil.show(mLaserMapResult.msg)
                                    }
                                    DialogHelper.loadingDialog.dismiss()
                                    withContext(Dispatchers.Main) {
                                        if (mMapResult.isFlag){
                                            binding.tvStart.text = "开始"
                                            isStart = true
                                            findNavController().popBackStack(R.id.debuggingFragment,false)
                                        }else{
                                            ToastUtil.show(mMapResult.msg)
                                        }
                                    }

                                }
                            }
                        }

                        override fun cancel() {

                        }
                    }).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        DialogHelper.loadingDialog.show()
//        MainScope().launch(Dispatchers.Default) {
//            if (!mapLaserServiceImpl.sendLaserMapManagerMsg(2).isFlag) {
//                ToastUtil.show("开始处理路径和目标点失败")
//            }
//            virtualTaskExecute(5)
//            DialogHelper.loadingDialog.dismiss()
//        }
        LaserObject.routePoints.observe(viewLifecycleOwner) {
            binding.laserPointsView.apply {
                LogUtil.d("显示收到的路径点数据")
                setCurrentRoutePoints(it)
            }
        }
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
        val emptyRoutePoint:ArrayList<FloatArray> = ArrayList()
        binding.laserPointsView.apply {
            setRoutePoints(emptyRoutePoint)
        }
    }

}