package com.sendi.deliveredrobot.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.BusinessAdapter
import com.sendi.deliveredrobot.adapter.base.i.GuidePointAdapter
import com.sendi.deliveredrobot.databinding.FragmentBusinessBinding
import com.sendi.deliveredrobot.databinding.FragmentGuideBinding
import com.sendi.deliveredrobot.helpers.DialogHelper
import com.sendi.deliveredrobot.model.TaskModel
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.GuideTaskBillFactory
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap
import com.sendi.deliveredrobot.room.entity.QueryPointEntity
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @Author Swn
 * @Data 2023/10/18
 * @describe 业务办理页面
 */
class BusinessFragment : Fragment() {

    private lateinit var binding: FragmentBusinessBinding
    private var controller: NavController? = null
    private var queryFloorPoints: List<QueryPointEntity> = ArrayList()
    val mainScope = MainScope()
    val dao = DataBaseDeliveredRobotMap.getDatabase(MyApplication.instance!!).getDao()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //获取所有目标点
        queryFloorPoints = ArrayList()
        mainScope.launch(Dispatchers.Default) {
            queryFloorPoints = dao.queryAllPoints()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business, container, false)
        binding = DataBindingUtil.bind(view)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = Navigation.findNavController(requireView())
        //初始化适配器
        binding.businessGv.adapter = context?.let { BusinessAdapter(it, queryFloorPoints) }
        //item点击
        binding.businessGv.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
               Toast.makeText(context,"点击了：$position",Toast.LENGTH_SHORT).show()
            }

    }

}