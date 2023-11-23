package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.KEY_NAVIGATE_FROM
import com.sendi.deliveredrobot.NAVIGATE_FROM_FIX_LASER
import com.sendi.deliveredrobot.NAVIGATE_TO_LASER_SCAN_CREATE
import com.sendi.deliveredrobot.NAVIGATE_TO_LASER_SCAN_EDIT
import com.sendi.deliveredrobot.databinding.FragmentLaserScanBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.QueryElevatorListModel
import com.sendi.deliveredrobot.model.QueryFloorListModel
import com.sendi.deliveredrobot.navigationtask.virtualTaskExecute
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.service.CloudMqttService
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Date
import java.util.Timer

/**
 * @author heky
 * @describe 激光扫描
 * @date 2021/09/08
 */
class LaserScanFragment : Fragment() {
    lateinit var binding: FragmentLaserScanBinding
    private var scanning = false //扫描状态
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private lateinit var dialog: HideNavigationBarDialog
    private lateinit var dao: DeliveredRobotDao
    private val points = ArrayList<IntArray>() //本地缓存点集
    private val existPoints = ArrayList<String>() //已存在点集

    override fun onStart() {
        super.onStart()
        dao = DataBaseDeliveredRobotMap.getDatabase(requireContext()).getDao()
        LaserObject.clear()
        LaserObject.livePoints.observe(viewLifecycleOwner) {
            binding.laserPointsView.apply {
                if (LaserObject.robotPose != null) {
                    setRobotPose(LaserObject.robotPose!!)
                }
                if (LaserObject.status != null) {
                    setStatus(LaserObject.status!!)
                }
                if (it != null) {
                    LogUtil.i("heky:origin${it.size}")
                    val result = filterPoints(it)
                    LogUtil.i("heky:filter${result.size}")
                    points.add(0,result)
                    setLaserPointsArray(points)
                }
                invalidate()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_laser_scan, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val from = arguments?.get(KEY_NAVIGATE_FROM)
        binding = DataBindingUtil.bind(view)!!
        binding.buttonQuit.apply {
            setOnClickListener {
                MainScope().launch {
                    isClickable = false
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.quit()
                    }
                    findNavController().popBackStack()
                }

            }
        }
        dialog = initCreateLaserMapDialog()
        binding.buttonStartScan.apply {
            text = if (!scanning) {
                resources.getString(R.string.laser_start_scan)
            } else {
                resources.getString(R.string.laser_stop_scan)
            }
            setOnClickListener {
                MainScope().launch {
                    isEnabled = false
                    if (!scanning) {
                        //开始扫描
                        when (from) {
                            NAVIGATE_TO_LASER_SCAN_CREATE -> {
                                // 新建
                                if(!ElevatorObject.checkDataExist()){
                                    ToastUtil.show(resources.getString(R.string.elevator_list_retry))
                                    // 楼层
                                    CloudMqttService.publish(QueryFloorListModel().toString())
                                    // 电梯
                                    CloudMqttService.publish(QueryElevatorListModel().toString())
                                    DialogHelper.loadingDialog.show()
                                    virtualTaskExecute(1)
                                    DialogHelper.loadingDialog.dismiss()
                                    if(!ElevatorObject.checkDataExist()){
                                        ToastUtil.show(resources.getString(R.string.elevator_list_request_error))
                                        return@launch
                                    }
                                    dialog = initCreateLaserMapDialog()
                                }
                                dialog.show()
                            }
                            NAVIGATE_TO_LASER_SCAN_EDIT -> {
                                // 编辑
                                MainScope().launch {
                                    DialogHelper.loadingDialog.show()
                                    withContext(Dispatchers.Default) {
                                        mapLaserServiceImpl.continueScan()
                                        binding.laserPointsView.apply {
                                            setStaticPoints(RosPointArrUtil.staticMap)
                                            invalidate()
                                        }
                                    }
                                    DialogHelper.loadingDialog.dismiss()
                                    binding.buttonStartScan.apply {
                                        text = resources.getString(R.string.laser_stop_scan)
                                    }
                                    scanning = true
                                }
                            }
                        }
                    } else {
                        MainScope().launch {
                            //清除缓存
                            LaserObject.clear()
                            DialogHelper.loadingDialog.show()
                            withContext(Dispatchers.Default) {
                                //结束扫描
                                val result = mapLaserServiceImpl.stop()
                                if (result.data["result"] == 1) {
                                    val timer = Timer()
                                    var timeOver = true
                                    timer.schedule(object : java.util.TimerTask() {
                                        override fun run() {
                                            timeOver = false
                                            timer.cancel()
                                        }
                                    }, Date(), 1000 * 60 * 5)

                                    var saveMapFlagResult: Boolean
                                    do {
                                        saveMapFlagResult = ROSHelper.getParam("/map/saving_map_flag") == "1"
                                        delay(100L)
                                    }while (!saveMapFlagResult && timeOver)
                                    timer.cancel()
                                }
                            }
                            DialogHelper.loadingDialog.dismiss()
                            //跳转激光修正图页面
                            findNavController().navigate(R.id.fixLaserMapFragment, Bundle().apply {
                                putString(KEY_NAVIGATE_FROM, NAVIGATE_FROM_FIX_LASER)
                            })
                        }

                    }
                    virtualTaskExecute(2, "扫描按钮")
                    isEnabled = true
                }
            }
        }
        if(from == NAVIGATE_TO_LASER_SCAN_EDIT && !scanning){
            //如果是继续修正，则直接开始扫描
            binding.buttonStartScan.callOnClick()
        }
    }

    /**
     * @describe 创建激光图Dialog
     */
    @SuppressLint("InflateParams")
    private fun initCreateLaserMapDialog(): HideNavigationBarDialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.laser_pop_ups, null)
        val displayMetrics = resources.displayMetrics
        val popUpsEditText =
            dialogView.findViewById<EditText>(R.id.pop_ups_edittext)
//        val editTextFloorName = dialogView.findViewById<EditText>(R.id.editTextFloorName)
//        val editTextFloorCode = dialogView.findViewById<EditText>(R.id.editTextFloorCode)
        var floorNameArray = ElevatorObject.floorNameArray?: arrayOf()
        val spinnerFloorName = dialogView.findViewById<Spinner>(R.id.spinnerFloorName).apply {
            val arrayAdapter = ArrayAdapter(
                context,
                R.layout.item_spinner_sort,
                R.id.textViewContent,
                floorNameArray
            ).apply {
                setDropDownViewResource(R.layout.item_spinner_drop_down_sort)
            }
            adapter = arrayAdapter
            setSelection(0)
        }
        dialogView.findViewById<TextView>(R.id.laser_confirm_button).apply {
            isEnabled = true
            isClickable = true
            setOnClickListener {
//                isEnabled = false
//                isClickable = false
                MainScope().launch {
                    if(popUpsEditText.text.isNullOrEmpty()){
                        ToastUtil.show(getString(R.string.please_input_laser_name))
                        return@launch
                    }
                    if(popUpsEditText.text.contains(Regex("[@&]"))){
                        ToastUtil.show(getString(R.string.laser_name_can_not_contain))
                        return@launch
                    }
                    val queryLaserNameIdResult:Boolean
                    withContext(Dispatchers.Default){
                        queryLaserNameIdResult = dao.queryLaserNameId(popUpsEditText.text.toString()) > 0
                    }
                    if(queryLaserNameIdResult){
                        ToastUtil.show(getString(R.string.laser_name_existed))
                        return@launch
                    }
                    if (floorNameArray.isEmpty()) {
                        ToastUtil.show(getString(R.string.elevator_array_not_exist))
                        return@launch
                    }
                    DialogHelper.loadingDialog.show()
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.create()
                        mapLaserServiceImpl.startScan(
                            popUpsEditText.text.toString().trim(),
//                            floorCodeInt,
//                            editTextFloorName.text.toString()
                            floorNameArray[spinnerFloorName.selectedItemPosition]
                        )
                    }
                    DialogHelper.loadingDialog.dismiss()
                    dialog.dismiss()
                    binding.buttonStartScan.apply {
                        text = resources.getString(R.string.laser_stop_scan)
                    }
                    scanning = true
                }
            }
        }
        dialogView.findViewById<ImageView>(R.id.back_imageButton).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
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
     * @description 过滤点，
     */
    private fun filterPoints(origin: IntArray): IntArray{
        val result = ArrayList<Int>()
        for (index in origin.indices) {
            if (index > 0 && index % 2 == 1) {
                val originX = origin[index - 1]
                val originY = origin[index]
                val x = originX / 5
                val y = originY / 5
                val temp = "$x#$y"
                if(!existPoints.contains(temp)){
                    result.add(originX)
                    result.add(originY)
                    existPoints.add(temp)
                }
            }
        }
        return result.toIntArray()
    }
}