package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.LimitSpeedListAdapter
import com.sendi.deliveredrobot.LIMIT_SPEED_AREA_FALSE
import com.sendi.deliveredrobot.LIMIT_SPEED_TYPE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_MAP_TYPE
import com.sendi.deliveredrobot.databinding.FragmentLimitSpeedListBinding
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity
import com.sendi.deliveredrobot.room.entity.SubMap
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.HideNavigationBarDialog
import kotlinx.coroutines.*
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-02-21
 * @describe 限速区列表页面
 */
class LimitSpeedListFragment : Fragment() {
    private lateinit var binding: FragmentLimitSpeedListBinding
    private lateinit var limitSpeedListAdapter: LimitSpeedListAdapter
    private lateinit var data: List<QuerySubMapEntity>
    private val dao: DeliveredRobotDao =
        DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
    private lateinit var mainScope: CoroutineScope
    private var dataSize by Delegates.observable(0){
            _, _, newValue ->
        when (newValue) {
            0 -> {
                with(binding){
                    noDataView.visibility = View.VISIBLE
                    recyclerViewLimitSpeedList.visibility = View.GONE
                }
            }
            else -> {
                with(binding){
                    noDataView.visibility = View.GONE
                    recyclerViewLimitSpeedList.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        limitSpeedListAdapter = LimitSpeedListAdapter().apply {
            setOnItemClickListener(object :LimitSpeedListAdapter.OnItemClickCallback{
                override fun onItemClick(data: QuerySubMapEntity, position: Int) {
                    findNavController().navigate(R.id.relocationLaserFragment,
                        Bundle().apply {
                            putInt(NAVIGATE_ID, data.id)
                            putInt(NAVIGATE_MAP_TYPE, LIMIT_SPEED_TYPE)
                        })
                }

                override fun onDeleteButtonClick(data: QuerySubMapEntity, position: Int) {
                    getDeleteLimitSpeedDialog(data).show()
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
        return inflater.inflate(R.layout.fragment_limit_speed_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.recyclerViewLimitSpeedList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = limitSpeedListAdapter
        }
        binding.textViewCreateLimitSpeedArea.apply {
            isClickable = true
            setOnClickListener{
                isClickable = false
                findNavController().navigate(R.id.relocationLaserFragment,
                    Bundle().apply {
                        putInt(NAVIGATE_ID, -1)
                        putInt(NAVIGATE_MAP_TYPE, LIMIT_SPEED_TYPE)
                    })
            }
        }
    }

    /**
     * @describe 刷新列表
     */
    private suspend fun refreshLaserList() {
        withContext(Dispatchers.Default) {
            data = dao.queryLimitSpeedList()
        }
        limitSpeedListAdapter.data = data
        dataSize = data.size
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    private fun getDeleteLimitSpeedDialog(data: SubMap): Dialog {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialog =
            HideNavigationBarDialog(requireContext(), R.style.simpleDialogStyle)
        dialog.setCancelable(false)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete, null)
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
                mainScope.launch {
                    withContext(Dispatchers.Default){
                        if (!ROSHelper.deleteAllLimitSpeed(data.path!!)) {
                            ToastUtil.show("删除限速区失败")
                            return@withContext
                        }
                        //更新状态
                        dao.updateSubMap(data.apply {
                            limitSpeed = LIMIT_SPEED_AREA_FALSE
                        })
                    }
                    //删除该项
                    (limitSpeedListAdapter.data as ArrayList).remove(data)
                    //刷新列表
                    limitSpeedListAdapter.notifyDataSetChanged()
                    dataSize = limitSpeedListAdapter.data.size
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
}