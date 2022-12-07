package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.databinding.FragmentShowFixLaserMapBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.ros.constant.RosResultEnum
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  @describe 展示修复激光图
 */
class ShowFixLaserMapFragment : Fragment() {
    private lateinit var binding: FragmentShowFixLaserMapBinding
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_fix_laser_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        MainScope().launch {
            val mapResult: MapResult
            DialogHelper.loadingDialog.show()
            withContext(Dispatchers.Default){
                mapResult = mapLaserServiceImpl.showFixMap()
            }
            DialogHelper.loadingDialog.dismiss()
            if (mapResult.isFlag) {
//            val data = mapResult.data
//            val idInfo = data["idInfo"]
//            val map = data["map"]
                binding.laserPointsView.apply {
                    if (RosPointArrUtil.result == RosResultEnum.LASER_SUCCESS_RESULT.code) {
                        if(RosPointArrUtil.idInfo != null){
                            setIdsInfo(RosPointArrUtil.idInfo)
                        }
                        if(RosPointArrUtil.staticMap != null){
                            setStaticPoints(RosPointArrUtil.staticMap)
                        }
                        invalidate()
                    }
                }
            }
        }


    }
}