package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.adapter.GeneralViewListAdapter
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_MAP_NAME
import com.sendi.deliveredrobot.databinding.FragmentGeneralViewListBinding
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.MyRootMap
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.view.widget.DeleteRouteConfirmDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * @author lsz
 * @describe 总图列表页
 * @date 2021/9/6
 */
class GeneralViewListFragment : Fragment() {
    lateinit var binding: FragmentGeneralViewListBinding
    lateinit var mAdapter: GeneralViewListAdapter
    lateinit var listData:List<MyRootMap>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_view_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        binding.recyclerViewLaserList.apply {
            mAdapter = GeneralViewListAdapter(context,null).apply {
                layoutManager = LinearLayoutManager(requireContext())
                setOnItemClickListener(object : GeneralViewListAdapter.OnItemClickCallback {
                    override fun onItemClick(data: MyRootMap, position: Int) {
                        findNavController().navigate(R.id.createGeneralViewFragment,Bundle().apply {
                            putInt(NAVIGATE_ID, data.id)
                            putString(NAVIGATE_MAP_NAME, data.name)
                        })
                    }

                    override fun onExportButtonClick(data: MyRootMap, position: Int) {
                    }

                    override fun onImportButtonClick(data: MyRootMap, position: Int) {
                    }

                    override fun onDeleteButtonClick(data: MyRootMap, position: Int) {
                        DeleteRouteConfirmDialog(requireContext(),deleteRouteConfirmDialogListener = object :DeleteRouteConfirmDialog.DeleteRouteDialogListener{
                            override fun confirm(dialog: DeleteRouteConfirmDialog) {

                                MainScope().launch {
                                    withContext(Dispatchers.Default) {
                                        var debugDao = DataBaseDeliveredRobotMap.getDatabase(
                                            Objects.requireNonNull(
                                                MyApplication.instance
                                            )!!
                                        ).getDebug()
                                        debugDao.delteRelationshipPointByMapId(data.id)
                                        debugDao.delteMapRootByMapId(data.id)
                                        val mDeliveredRobotDao: DeliveredRobotDao =
                                            DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!)
                                                .getDao()
                                        listData = mDeliveredRobotDao.queryRootMap()!!
                                    }

                                    withContext(Dispatchers.Main) {
                                        mAdapter.setData(listData)
                                    }
                                    withContext(Dispatchers.Main){
                                        ToastUtil.show(getString(R.string.delete_success))
                                    }
                                }
                            }

                            override fun cancel(dialog: DeleteRouteConfirmDialog) {
                            }
                            },data.name?:"总图名称").show()
                    }
                })
            }
            adapter = mAdapter
        }
        binding.tvCreateGeneralView.apply {
            isClickable = true
            setOnClickListener(){
                findNavController().navigate(R.id.createGeneralViewFragment,Bundle().apply {
                    putInt(NAVIGATE_ID, -1)
                    putString(NAVIGATE_MAP_NAME, "")
                })
            }
        }
        MainScope().launch {
            val dao: DeliveredRobotDao =
                DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()
            withContext(Dispatchers.Default){
                listData = dao.queryRootMap()!!
            }
            //设置当前页面数据为空
            //listData = emptyArray<MyRootMap>().toList()
            withContext(Dispatchers.Main){
                if (listData.isEmpty()){
                    binding.clRoadMapEmpty.visibility = View.VISIBLE
                }else{
                    binding.tvGeneralEmpty.setText("暂无总图，请先创建...")
                    binding.clRoadMapEmpty.visibility = View.GONE
                }
            }
            mAdapter.setData(listData)
        }

    }
}