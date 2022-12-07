package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.FIRST_MAP_ID
import com.sendi.deliveredrobot.KEY_NAVIGATE_FROM
import com.sendi.deliveredrobot.NAVIGATE_FROM_FIX_LASER
import com.sendi.deliveredrobot.SECOND_MAP_ID
import com.sendi.deliveredrobot.databinding.FragmentAdjustFixMapBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.FixOperateEnum
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @describe 调整激光图页面
 */
class AdjustFixMapFragment : Fragment() {
    private lateinit var binding: FragmentAdjustFixMapBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()

    //    private var staticMap: List<Point32>? = null
    private var staticMap: ArrayList<FloatArray>? = null
    private val angelArray = arrayOf(1, 5, 10, 20, 50)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_adjust_fix_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val firstMapId = arguments?.getInt(FIRST_MAP_ID)
        val secondMapId = arguments?.getInt(SECOND_MAP_ID)
        if (firstMapId != null && secondMapId != null) {
            MainScope().launch {
                DialogHelper.loadingDialog.show()
                val mapResult: MapResult
                withContext(Dispatchers.IO) {
                    mapResult = mapLaserServiceImpl.chooseFilesToFix(firstMapId, secondMapId)
                }
                DialogHelper.loadingDialog.dismiss()
                if (mapResult.isFlag) {
                    try {
//                    staticMap = mapResult.data["staticMap"] as List<Point32>
//                    val updateMap = mapResult.data["updateMap"] as List<Point32>
                        staticMap = RosPointArrUtil.staticMap
                        val updateMap: ArrayList<FloatArray>? = RosPointArrUtil.updateMap
                        binding.laserPointsView.apply {
                            if (staticMap != null) {
                                setStaticPoints(staticMap!!)
//                            setLaserPoints(updateMap)
                            }
                            if (updateMap != null) {
                                setUpdatePoints(updateMap)
                            }
                            invalidate()
                        }
                    } catch (e: Exception) {
                        LogUtil.e(e.toString())
                    }
                }
            }

        }
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
        binding.textViewDone.apply {
            isClickable = true
            isEnabled = true
            setOnClickListener {
                isClickable = false
                isEnabled = false
                MainScope().launch {
                    DialogHelper.loadingDialog.show()
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.saveFix(1)
                    }
                    DialogHelper.loadingDialog.dismiss()
                    findNavController().navigate(
                        R.id.fixLaserMapFragment,
                        Bundle().apply {
                            putString(KEY_NAVIGATE_FROM, NAVIGATE_FROM_FIX_LASER)
                        },
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }
        }
        binding.textViewGiveUp.apply {
            isClickable = true
            isEnabled = true
            setOnClickListener {
                isClickable = false
                isEnabled = false
                MainScope().launch {
                    DialogHelper.loadingDialog.show()
                    withContext(Dispatchers.Default) {
                        mapLaserServiceImpl.saveFix(-1)
                    }
                    DialogHelper.loadingDialog.dismiss()
                    findNavController().navigate(
                        R.id.fixLaserMapFragment,
                        Bundle().apply {
                            putString(KEY_NAVIGATE_FROM, NAVIGATE_FROM_FIX_LASER)
                        },
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }
        }
    }
}