package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.LINE_INFO_MODEL
import com.sendi.deliveredrobot.LINE_NAME
import com.sendi.deliveredrobot.SUB_MAP
import com.sendi.deliveredrobot.databinding.FragmentCreateVirtualWallBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.PointCompat
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.ros.SubManager
import com.sendi.deliveredrobot.ros.constant.ClientConstant
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.RosPointArrUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2020-03-03
 * @describe 创建虚拟墙
 */
class CreateVirtualWallFragment : Fragment() {
    companion object {
        const val STATUS_READY = 0
        const val STATUS_CREATING = 1
        const val STATUS_FINISH = 2
    }

    lateinit var binding: FragmentCreateVirtualWallBinding
    private var pose2dList: List<PointCompat>? = null
    private var status: Int by Delegates.observable(STATUS_READY) { _, _, newValue ->
        when (newValue) {
            STATUS_READY -> {
                binding.textViewPrevStep.visibility = View.VISIBLE
                binding.textViewReset.apply {
                    visibility = View.GONE
                }
                binding.tvStart.apply {
                    text = "开始"
                }
            }
            STATUS_CREATING -> {
                binding.textViewPrevStep.visibility = View.GONE
                binding.textViewReset.apply {
                    visibility = View.GONE
                }
                binding.tvStart.apply {
                    text = "结束"
                }
            }
            STATUS_FINISH -> {
                binding.textViewPrevStep.visibility = View.GONE
                binding.textViewReset.visibility = View.VISIBLE
                binding.tvStart.apply {
                    text = "确认"
                }
            }
        }
    }
    private lateinit var subMap: SubMap
    private lateinit var lineName: String
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //清除缓存
        LaserObject.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_virtual_wall, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.textViewPrevStep.apply {
            isClickable = true
            setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.tvStart.apply {
            setOnClickListener {
                when (status) {
                    STATUS_READY -> {
                        MainScope().launch(Dispatchers.Default) {
                            DialogHelper.loadingDialog.show()
                            //开始创建限速区失败
                            //订阅激光点
                            SubManager.sub(ClientConstant.GLOBAL_LASER)
                            if (ROSHelper.startCreateVirtualWall()) {
                                DialogHelper.loadingDialog.dismiss()
                                withContext(Dispatchers.Main){
                                    status = STATUS_CREATING
                                }
                            } else {
                                DialogHelper.loadingDialog.dismiss()
                                ToastUtil.show("创建虚拟墙失败")
                            }
                        }
                    }
                    STATUS_CREATING -> {
                        MainScope().launch(Dispatchers.Default) {
                            DialogHelper.loadingDialog.show()
                            withContext(Dispatchers.Default) {
                                //结束创建限速区
                                //解除订阅激光点
                                SubManager.unsub(ClientConstant.GLOBAL_LASER)
                                if (ROSHelper.endCreateVirtualWall(lineName)) {
                                    DialogHelper.loadingDialog.dismiss()
                                    withContext(Dispatchers.Main){
                                        status = STATUS_FINISH
                                    }
                                } else {
                                    DialogHelper.loadingDialog.dismiss()
                                    ToastUtil.show("结束创建虚拟墙失败")
                                }
                            }
                        }
                    }
                    STATUS_FINISH -> {
                        MainScope().launch(Dispatchers.Default) {
                            val name = lineName
                            withContext(Dispatchers.Main) {
                                status = STATUS_FINISH
                            }
                            //返回上层页面
                            val lineInfoModel = LineInfoModel(
                                pose = pose2dList,
                                name = name,
                                state = 0
                            )
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                LINE_INFO_MODEL, lineInfoModel
                            )
                            findNavController().popBackStack()
                        }
                    }
                }
            }

            binding.textViewReset.apply {
                visibility = View.GONE
                setOnClickListener {
                    if (ROSHelper.resetVirtualWall()) {
                        binding.laserPointsView.clearLineInfo()
                        status = STATUS_READY
                    } else {
                        ToastUtil.show("重置虚拟墙失败")
                    }
                }
            }
        }

        binding.laserPointsView.apply {
            setStaticPoints(RosPointArrUtil.staticMap)
            invalidate()
        }
        LaserObject.clear()
        LaserObject.tempObstacle.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            val list = ArrayList<PointCompat>()
            for (point32 in it) {
                val pointCompat = PointCompat()
                pointCompat.x = point32.x.toDouble()
                pointCompat.y = point32.y.toDouble()
                pointCompat.z = point32.z.toDouble()
                list.add(pointCompat)
            }
            pose2dList = list
//            LogUtil.d("tempObstacle:${list.toArray().contentToString()}")
            binding.laserPointsView.setLineInfo(list)
        }
        LaserObject.livePoints.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            binding.laserPointsView.setLaserPoints(it)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        subMap = arguments?.getSerializable(SUB_MAP) as SubMap
        binding.textViewLaser.apply {
            text = subMap.name
        }
        lineName = arguments?.getString(LINE_NAME) ?: ""
        binding.textViewName.apply {
            text = "虚拟墙：$lineName"
        }
    }
}