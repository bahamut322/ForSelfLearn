package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.LaserListAdapter
import com.sendi.deliveredrobot.databinding.FragmentLaserListBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity
import com.sendi.deliveredrobot.ros.debug.MapIndexServiceImpl
import com.sendi.deliveredrobot.ros.debug.MapLaserServiceImpl
import com.sendi.deliveredrobot.ros.debug.dto.MapResult
import com.sendi.deliveredrobot.ros.debug.dto.MapTypeEnum
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.*
import kotlin.properties.Delegates

/**
 * @author heky
 * @describe 激光图列表页
 * @date 2021/09/02
 */
class LaserListFragment : Fragment() {
    private lateinit var binding: FragmentLaserListBinding
    private lateinit var recyclerViewLaserListAdapter: LaserListAdapter
    private lateinit var data: List<QuerySubMapEntity>
    private val mapLaserServiceImpl = MapLaserServiceImpl.getInstance()
    private val mapIndexServiceImpl = MapIndexServiceImpl()
    private val dao: DeliveredRobotDao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private lateinit var mainScope:CoroutineScope
    private var dataSize by Delegates.observable(0){
            _, _, newValue ->
        when (newValue) {
            0 -> {
                with(binding){
                    noDataView.visibility = View.VISIBLE
                    recyclerViewLaserList.visibility = View.GONE
                }
            }
            else -> {
                with(binding){
                    noDataView.visibility = View.GONE
                    recyclerViewLaserList.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerViewLaserListAdapter = LaserListAdapter().apply {
            setOnItemClickListener(object : LaserListAdapter.OnItemClickCallback {
                override fun onItemClick(data: QuerySubMapEntity, position: Int) {
                    findNavController().navigate(R.id.editLaserMapFragment, Bundle().apply {
                        putString(SUB_MAP_NAME, data.path)
                        putString(SUB_MAP_NAME_DATABASE, data.name)
                        putInt(SUB_MAP_ID,data.id)
                    })
                }

                override fun onExportButtonClick(data: QuerySubMapEntity, position: Int) {
                    mainScope.launch {
                        DialogHelper.loadingDialog.show()
                        val exportLaserMap: MapResult?
                        withContext(Dispatchers.IO) {
                            exportLaserMap = mapLaserServiceImpl.exportLaserMap("${data.path}@${data.name}&${data.floorName}")
                        }
                        DialogHelper.loadingDialog.dismiss()
                        if (exportLaserMap != null && exportLaserMap.isFlag) {
                            try {
                                val targetName = exportLaserMap.data["targetName"] as String
                                val pathName = data.name ?: ""
                                getExportLaserMapDialog(pathName, targetName).show()
                            } catch (e: ClassCastException) {
                                LogUtil.e(e.toString())
                            }
                        }else{
                            ToastUtil.show(getString(R.string.export_error_please_retry))
                        }
                    }
                }

                override fun onImportButtonClick(data: QuerySubMapEntity, position: Int) {
                    mainScope.launch {
                        DialogHelper.loadingDialog.show()
                        val importLaserMap: MapResult?
                        withContext(Dispatchers.IO) {
                            importLaserMap = mapLaserServiceImpl.importLaserMap("${data.path}@${data.name}&${data.floorName}")
                        }
                        DialogHelper.loadingDialog.dismiss()
                        if (importLaserMap != null && importLaserMap.isFlag) {
                            val pathName = data.name ?: ""
                            getImportLaserMapDialog(pathName).show()
                        }else{
                            ToastUtil.show(getString(R.string.import_error_please_retry))
                        }
                    }

                }

                override fun onDeleteButtonClick(data: QuerySubMapEntity, position: Int) {
                    getDeleteLaserMapDialog(data).show()
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        mainScope = MainScope()
        mainScope.launch {
            refreshLaserList()
        }
    }

    override fun onStop() {
        super.onStop()
        mainScope.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_laser_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.recyclerViewLaserList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recyclerViewLaserListAdapter
        }
        binding.textViewCreateLaserMap.apply {
            isClickable = true
            setOnClickListener {
                //跳转页面
                findNavController().navigate(R.id.laserScanFragment, Bundle().apply {
                    putString(KEY_NAVIGATE_FROM, NAVIGATE_TO_LASER_SCAN_CREATE)
                })
            }
        }
    }

    /**
     * @describe 刷新列表
     */
    private suspend fun refreshLaserList() {
        withContext(Dispatchers.Default) {
            data = dao.querySubMaps()
        }
        recyclerViewLaserListAdapter.setData(data)
        dataSize = data.size
    }

    /**
     * @describe 导出
     */
    @SuppressLint("InflateParams")
    private fun getExportLaserMapDialog(pathName: String, absolutePath: String): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_export_laser_map, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = CommonHelper.getExportImportTextSpan(
                pathName,
                "已成功导出到",
                absolutePath,
                ContextCompat.getColor(requireContext(), R.color.color_2170E7)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
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
     * @describe 导入
     */
    @SuppressLint("InflateParams")
    private fun getImportLaserMapDialog(pathName: String): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_export_laser_map, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = CommonHelper.getExportImportTextSpan(
                string1 = pathName,
                string2 = "已导入成功",
                color = ContextCompat.getColor(requireContext(), R.color.color_2170E7)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
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

    @SuppressLint("InflateParams")
    private fun getDeleteLaserMapDialog(data: QuerySubMapEntity): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete_laser_map, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = CommonHelper.getDeleteLaserMapTextSpan(
                string1 = "确认要删除",
                string2 = data.name?:"",
                string3 = "吗？",
                color = ContextCompat.getColor(requireContext(), R.color.color_4D6FBE)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            isClickable = true
            setOnClickListener {
                dialog.dismiss()
                var mapResult: MapResult?
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        // Id
                        mapResult =
                            mapIndexServiceImpl.deleteMapById(MapTypeEnum.LASER_MAP, data.id)
                    }
                    if (mapResult != null) {
                        if (mapResult!!.isFlag) {
                            //可删除
                            mainScope.launch {
                                refreshLaserList()
                            }
                        } else {
                            //不可删除，有总图绑定
                            val rootNameList = mapResult!!.data["rootNameList"] as List<String>
                            getCanDeleteLaserMapDialog(rootNameList).show()
                        }
                    }
                }
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
     * @describe 删除
     */
    @SuppressLint("InflateParams")
    private fun getCanDeleteLaserMapDialog(rootNameList: List<String>): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_can_not_delete_laser_map, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            val rootNames = rootNameList.joinToString()
            text = CommonHelper.getExportImportTextSpan(
                string1 = "该激光图已被",
                string2 = rootNames,
                string3 = "绑定，请先解绑后再执行删除操作",
                color = ContextCompat.getColor(requireContext(), R.color.color_4D6FBE)
            )
        }
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
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
}