package com.sendi.deliveredrobot.view.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sendi.deliveredrobot.MyApplication.Companion.instance
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_MAP_NAME
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.AllBindingFoldAdapter
import com.sendi.deliveredrobot.adapter.PointFoldAdapter
import com.sendi.deliveredrobot.adapter.RouteFoldAdapter
import com.sendi.deliveredrobot.adapter.SelLaserAdapter
import com.sendi.deliveredrobot.adapter.base.i.OnItemClickListener2
import com.sendi.deliveredrobot.databinding.FragmentCreateGeneralViewBinding
import com.sendi.deliveredrobot.entity.MapRevise
import com.sendi.deliveredrobot.model.AllMapRelationshipModel
import com.sendi.deliveredrobot.model.FoldSelMapModel
import com.sendi.deliveredrobot.model.SelectPointModel
import com.sendi.deliveredrobot.room.dao.DebugDao
import com.sendi.deliveredrobot.room.dao.DeliveredRobotDao
import com.sendi.deliveredrobot.room.database.DataBaseDeliveredRobotMap.Companion.getDatabase
import com.sendi.deliveredrobot.room.entity.*
import com.sendi.deliveredrobot.utils.LogUtil
import com.sendi.deliveredrobot.utils.NavigationBarUtil
import com.sendi.deliveredrobot.utils.ToastUtil
import com.sendi.deliveredrobot.viewmodel.CreateGeneralViewViewModel
import javassist.bytecode.stackmap.TypeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal.where
import java.util.*
import java.util.logging.Logger
import kotlin.math.log


/**
 * @author lsz
 * @describe 创建总图
 * @date 2021/9/6
 */
class CreateGeneralViewFragment : Fragment() {
    lateinit var binding: FragmentCreateGeneralViewBinding
    private val viewModel by viewModels<CreateGeneralViewViewModel>({ requireActivity() })
    lateinit var mAdapter: SelLaserAdapter
    lateinit var mRouteAdapter: RouteFoldAdapter
    lateinit var mPointAdapter: PointFoldAdapter
    lateinit var allBindingAdapter: AllBindingFoldAdapter
    lateinit var subMapData: List<SubMap>
    lateinit var routeData: List<RouteMap>
    lateinit var mDeliveredRobotDao: DeliveredRobotDao
    lateinit var debugDao: DebugDao
    var isCreate: Boolean = true

    /** 总图id */
    var myRootMapId: Int = -1
    var hasSelectCount: Int = 0
    var isCheck: Boolean = false

    /** 编辑的总图名字 */
    var myRootMapName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_general_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = DataBindingUtil.bind(view)!!
        //判断是创建总图还是编辑总图
        if (arguments?.get(NAVIGATE_ID) != -1) {
            isCreate = false
            myRootMapId = arguments?.getInt(NAVIGATE_ID)!!
            myRootMapName = arguments?.getString(NAVIGATE_MAP_NAME) ?: ""
            binding.edtGeneralViewName.setText(myRootMapName)
        }
        initView(view)
        initData()
    }

    // 计算选中个数
    fun calcueSelect() {
        hasSelectCount = 0
        viewModel.relationshipData.forEach { item ->
            if (item.selected) {
                hasSelectCount++
            }
        }
        if (hasSelectCount != viewModel.relationshipData.size) {
            // 取消全选
            binding.cbCheckAll.apply {
                isChecked = false
            }
            isCheck = false
        } else {
            binding.cbCheckAll.apply {
                isChecked = true
            }
            isCheck = true
        }
        binding.tvSelecedCount.setText("已选:  " + hasSelectCount + " 个 ")
    }

    fun initData() {
        viewModel.stepIndex.value = 1;

        MainScope().launch {
            withContext(Dispatchers.Default) {
                debugDao = getDatabase(Objects.requireNonNull(instance)!!).getDebug()
                subMapData = debugDao.queryMapSubList()
                withContext(Dispatchers.Main) {
                    if (subMapData.size == 0) {
                        binding.clEmpty.visibility = View.VISIBLE
                        binding.tvGeneralNew.visibility = View.VISIBLE
                        binding.rlCheckAll.visibility = View.GONE
                        binding.rcvLaserItem.visibility = View.GONE
                    } else {
                        binding.clEmpty.visibility = View.GONE
                    }
                }

                routeData = debugDao.queryMapRoute()

                //编辑地图时，存储已选的子图  key 子图id，value路径id
                val select_sub_map_data = hashMapOf<Int, Int>()
                //编辑地图时，存储已选的子图  路径图图id，value路径name
                val select_route_name = hashMapOf<Int, String>()
                //编辑地图时，存储已选的目标点  key 目标点id，value子图id
                val select_point_data = hashMapOf<Int, Int>()
                if (!isCreate) {
                    //如果是编辑需要查询旧的关联
                    var mRelationshipPointList =
                        debugDao.selectRelationshipPointByMapId(myRootMapId)
                    for (item in mRelationshipPointList) {
                        select_sub_map_data[item.subMapId!!] = item.routeId!!
                        if (item.routeName != null) {
                            select_route_name[item.routeId!!] = item.routeName!!
                        } else {
                            select_route_name[item.routeId!!] = ""
                        }
                        select_point_data[item.pointId!!] = item.subMapId!!
                    }
                }
                if (subMapData == null) {

                }

                if (subMapData != null) {
                    viewModel.relationshipData.clear()
                    for (subMap in subMapData) {
                        var pointListData: List<Point> = debugDao.queryPointsBySubMapId(subMap.id)
                        var myPointListData = ArrayList<SelectPointModel>()
                        for (mItem in pointListData!!) {
                            var myPointData: SelectPointModel = SelectPointModel(
                                mItem.id, mItem.name!!, mItem.direction, mItem.x, mItem.y,
                                mItem.w, mItem.subMapId, mItem.type, false
                            )
                            if (!isCreate) {
                                //编辑地图恢复已选目标点的选中状态
                                var tempSubMapId = select_point_data[myPointData.id]
                                if (tempSubMapId != null) {
                                    if (tempSubMapId == subMap.id) {
                                        myPointData.selected = true
                                    }
                                }
                            }
                            myPointListData.add(myPointData)
                        }

                        var mAllMapRelationship =
                            AllMapRelationshipModel(
                                -1,
                                "",
                                subMap,
                                -1,
                                "",
                                routeData!!,
                                myPointListData
                            )
                        if (!isCreate) {
                            //编辑地图恢复已选子图的路径选中状态
                            var tempRouteId = select_sub_map_data[subMap.id]
                            if (tempRouteId != null) {
                                mAllMapRelationship.selected = true
                                mAllMapRelationship.selectRouteId = tempRouteId
                                mAllMapRelationship.selectRouteName =
                                    select_route_name[tempRouteId] ?: ""
                            }
                        }

                        viewModel.relationshipData.add(mAllMapRelationship)
                    }
                }
                withContext(Dispatchers.Main) {
                    calcueSelect()
                    mAdapter.bindData(viewModel.relationshipData)
                    mRouteAdapter.setData(viewModel.relationshipData)
                }
            }
        }
    }

    fun initView(view: View) {

        mAdapter = SelLaserAdapter().apply {
            setOnRecyclerViewListener(object : OnItemClickListener2<AllMapRelationshipModel> {
                @SuppressLint("InflateParams")
                override fun onItemClick(
                    t: AllMapRelationshipModel?,
                    last: Int,
                    current: Int,
                    view: View?
                ): Boolean {
                    viewModel.relationshipData[current].selected =
                        !viewModel.relationshipData[current].selected
                    val cbSelSubmap: CheckBox = view!!.findViewById(R.id.cb_sel_submap)
                    cbSelSubmap.apply {
                        isChecked = viewModel.relationshipData[current].selected
                    }
                    calcueSelect()
                    return false
                }
            })
        }


        binding.rcvLaserMap.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = mAdapter
        }
        binding.tvReturn.apply {
            setOnClickListener {
                findNavController().navigateUp()
            }
        }

        binding.tvLastStep.apply {
            setOnClickListener {
                var stepIndex = viewModel.stepIndex.value!!.minus(1)
                setStepIndex(stepIndex)
            }
        }
        binding.tvNext.apply {
            setOnClickListener {
                if (hasSelectCount <= 0) {
                    ToastUtil.show(getString(R.string.please_choose_laser))
                } else {
                    var stepIndex = viewModel.stepIndex.value!!.plus(1)
                    setStepIndex(stepIndex)
                }
            }
        }
        binding.cbCheckAll.apply {
            setOnClickListener {
                if (isChecked) {
                    isCheck = true
                    for (mItem in viewModel.relationshipData) {
                        mItem.selected = true
                    }
                } else {
                    isCheck = false
                    for (mItem in viewModel.relationshipData) {
                        mItem.selected = false
                    }
                }
                mAdapter.bindData(viewModel.relationshipData)
                calcueSelect()
            }
        }
        //保存全部绑定关系
        binding.tvComplete.apply {
            setOnClickListener {
                completeBinding()
            }
        }
        //全选
        binding.tvCheckAll.apply {
            setOnClickListener {
                if (!isCheck) {
                    isCheck = true
                    binding.cbCheckAll.isChecked = true
                    for (mItem in viewModel.relationshipData) {
                        mItem.selected = true
                    }
                } else {
                    isCheck = false
                    binding.cbCheckAll.isChecked = false
                    for (mItem in viewModel.relationshipData) {
                        mItem.selected = false
                    }
                }
                mAdapter.bindData(viewModel.relationshipData)
                calcueSelect()
            }
        }

        //路径选择
        binding.rcvSelRouteMap.apply {
            mRouteAdapter = RouteFoldAdapter(context).apply {
                setItemClickCallBack(object :
                    RouteFoldAdapter.ItemClickCallBack {
                    override fun onClickMainMenuItem(
                        t: FoldSelMapModel,
                        adapterPosition: Int
                    ) {

                    }

                    override fun onClickSubMenuItem(
                        parentIndex: Int,
                        subIndex: Int,
                        item: FoldSelMapModel,
                        adapterPosition: Int
                    ) {
                        viewModel.relationshipData[parentIndex].selectRouteId = item.id
                        viewModel.relationshipData[parentIndex].selectRouteName = item.name
                    }

                    override fun onExpandListener(t: FoldSelMapModel, isExpand: Boolean) {

                    }

                })
            }
            layoutManager = LinearLayoutManager(context)
            adapter = mRouteAdapter
        }

        //目标点选择
        binding.rcvSelTargetMap.apply {
            mPointAdapter = PointFoldAdapter(context).apply {
                setItemClickCallBack(object :
                    PointFoldAdapter.ItemClickCallBack {
                    override fun onClickMainMenuItem(
                        t: FoldSelMapModel,
                        adapterPosition: Int
                    ) {

                    }

                    override fun onClickSubMenuItem(

                        parentIndex: Int,
                        subIndex: Int,
                        item: FoldSelMapModel,
                        adapterPosition: Int
                    ) {
                        viewModel.relationshipData[parentIndex].mPoint!![subIndex].selected =
                            item.mSelect
                    }

                    override fun onExpandListener(t: FoldSelMapModel, isExpand: Boolean) {

                    }

                    override fun onClickSelect(
                        onClickSelect: FoldSelMapModel,
                        adapterPosition: Int,
                        isChecked: Boolean
                    ) {
                        var mPointList = viewModel.relationshipData[onClickSelect.index].mPoint
                        if (mPointList != null) {
                            for (item in mPointList) {
                                item.selected = isChecked
                            }
                        }
                    }
                })
            }
            layoutManager = GridLayoutManager(context, 4)
            adapter = mPointAdapter
        }
        //最后选择结果
        binding.rcvSelVerifyMap.apply {
            allBindingAdapter = AllBindingFoldAdapter(context).apply {
                setItemClickCallBack(object :
                    AllBindingFoldAdapter.ItemClickCallBack {
                    override fun onClickMainMenuItem(
                        t: FoldSelMapModel,
                        adapterPosition: Int
                    ) {

                    }

                    override fun onClickSubMenuItem(
                        parentIndex: Int,
                        subIndex: Int,
                        item: FoldSelMapModel,
                        adapterPosition: Int
                    ) {

                    }

                    override fun onExpandListener(t: FoldSelMapModel, isExpand: Boolean) {

                    }
                })
            }
            layoutManager = GridLayoutManager(context, 4)
            adapter = allBindingAdapter
        }
        binding.tvStepOneText.setTextColor(Color.parseColor("#E2EBFE"))


        binding.clCreateGeneralView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //监听到了就注销监听
//                binding.llayoutLogin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                var rect: Rect = Rect()
                binding.clCreateGeneralView.getWindowVisibleDisplayFrame(rect)
                var rootInvisibleHeight: Int =
                    binding.clCreateGeneralView.getRootView().getHeight() - rect.bottom;
//                LogUtil.d( "getRootView().getHeight()=" + binding.clCreateGeneralView.getRootView().getHeight() + ",rect.bottom=" + rect.bottom + ",rootInvisibleHeight=" + rootInvisibleHeight);
                if (rootInvisibleHeight <= 100) {
                    //软键盘隐藏啦

                } else {
                    //软键盘弹出啦  隐藏状态栏
                    val window: Window = activity?.window!!
                    if (window != null) {
                        NavigationBarUtil.hideNavigationBar(window)
                    }
                }
            }
        })
    }

    //设置总图绑定的当前流程
    fun setStepIndex(index: Int) {
        when (index) {
            1 -> {
                binding.rcvLaserMap.apply { visibility = View.VISIBLE }
                binding.rcvSelRouteMap.apply { visibility = View.GONE }
                binding.rcvSelTargetMap.apply { visibility = View.GONE }
                binding.rcvSelVerifyMap.apply { visibility = View.GONE }
                binding.tvLastStep.apply { visibility = View.GONE }  //上一步
                binding.tvNext.apply { visibility = View.VISIBLE }   //下一步
                binding.tvComplete.apply { visibility = View.GONE }  //完成
                // 文字隐藏
                binding.tvStepOne.apply { visibility = View.VISIBLE }
                binding.tvStepTwo.apply { visibility = View.VISIBLE }
                binding.tvStepThree.apply { visibility = View.VISIBLE }
                binding.tvStepFour.apply { visibility = View.VISIBLE }
                // 图片
                binding.tvStepOneSuccess.apply { visibility = View.GONE }
                binding.tvStepTwoSuccess.apply { visibility = View.GONE }
                binding.tvStepThreeSuccess.apply { visibility = View.GONE }
                binding.tvStepFourSuccess.apply { visibility = View.GONE }
                // 字体颜色
                binding.tvStepOneText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepTwoText.setTextColor(Color.parseColor("#A0BAEF"))
                binding.tvStepThreeText.setTextColor(Color.parseColor("#A0BAEF"))
                binding.tvStepFourText.setTextColor(Color.parseColor("#A0BAEF"))

                binding.tvStepTwo.setBackgroundResource(R.drawable.selector_bg_circle)
                binding.tvStepThree.setBackgroundResource(R.drawable.selector_bg_circle)
                binding.tvStepFour.setBackgroundResource(R.drawable.selector_bg_circle)
            }
            2 -> {
                mRouteAdapter.setData(viewModel.relationshipData)
                binding.rcvLaserMap.apply { visibility = View.GONE }
                binding.rcvSelRouteMap.apply { visibility = View.VISIBLE }
                binding.rcvSelTargetMap.apply { visibility = View.GONE }
                binding.rcvSelVerifyMap.apply { visibility = View.GONE }
                binding.tvLastStep.apply { visibility = View.VISIBLE } //上一步
                binding.tvNext.apply { visibility = View.VISIBLE }   //下一步
                binding.tvComplete.apply { visibility = View.GONE }  //完成
                // 文字隐藏
                binding.tvStepOne.apply { visibility = View.INVISIBLE }
                binding.tvStepTwo.apply { visibility = View.VISIBLE }
                binding.tvStepThree.apply { visibility = View.VISIBLE }
                binding.tvStepFour.apply { visibility = View.VISIBLE }
                // 图片
                binding.tvStepOneSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepTwoSuccess.apply { visibility = View.INVISIBLE }
                binding.tvStepThreeSuccess.apply { visibility = View.INVISIBLE }
                binding.tvStepFourSuccess.apply { visibility = View.INVISIBLE }
                // 字体颜色
                binding.tvStepOneText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepTwoText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepThreeText.setTextColor(Color.parseColor("#A0BAEF"))
                binding.tvStepFourText.setTextColor(Color.parseColor("#A0BAEF"))

                binding.tvStepTwo.setBackgroundResource(R.drawable.ic_switch_thumb_checked)
                binding.tvStepThree.setBackgroundResource(R.drawable.selector_bg_circle)
                binding.tvStepFour.setBackgroundResource(R.drawable.selector_bg_circle)
            }
            3 -> {
                mPointAdapter.setData(viewModel.relationshipData)
                binding.rcvLaserMap.apply { visibility = View.GONE }
                binding.rcvSelRouteMap.apply { visibility = View.GONE }
                binding.rcvSelTargetMap.apply { visibility = View.VISIBLE }
                binding.rcvSelVerifyMap.apply { visibility = View.GONE }
                binding.tvLastStep.apply { visibility = View.VISIBLE } //上一步
                binding.tvNext.apply { visibility = View.VISIBLE }   //下一步
                binding.tvComplete.apply { visibility = View.GONE }  //完成
                // 文字隐藏
                binding.tvStepOne.apply { visibility = View.INVISIBLE }
                binding.tvStepTwo.apply { visibility = View.INVISIBLE }
                binding.tvStepThree.apply { visibility = View.VISIBLE }
                binding.tvStepFour.apply { visibility = View.VISIBLE }
                // 图片
                binding.tvStepOneSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepTwoSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepThreeSuccess.apply { visibility = View.INVISIBLE }
                binding.tvStepFourSuccess.apply { visibility = View.INVISIBLE }
                // 字体颜色
                binding.tvStepOneText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepTwoText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepThreeText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepFourText.setTextColor(Color.parseColor("#A0BAEF"))

                binding.tvStepThree.setBackgroundResource(R.drawable.ic_switch_thumb_checked)
                binding.tvStepFour.setBackgroundResource(R.drawable.selector_bg_circle)
            }
            4 -> {
                allBindingAdapter.setData(viewModel.relationshipData)
                binding.rcvLaserMap.apply { visibility = View.GONE }
                binding.rcvSelRouteMap.apply { visibility = View.GONE }
                binding.rcvSelTargetMap.apply { visibility = View.GONE }
                binding.rcvSelVerifyMap.apply { visibility = View.VISIBLE }
                binding.tvLastStep.apply { visibility = View.VISIBLE } //上一步
                binding.tvNext.apply { visibility = View.INVISIBLE }   //下一步
                binding.tvComplete.apply { visibility = View.VISIBLE }  //完成
                // 文字隐藏
                binding.tvStepOne.apply { visibility = View.INVISIBLE }
                binding.tvStepTwo.apply { visibility = View.INVISIBLE }
                binding.tvStepThree.apply { visibility = View.INVISIBLE }
                binding.tvStepFour.apply { visibility = View.VISIBLE }
                // 图片
                binding.tvStepOneSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepTwoSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepThreeSuccess.apply { visibility = View.VISIBLE }
                binding.tvStepFourSuccess.apply { visibility = View.INVISIBLE }
                // 字体颜色
                binding.tvStepOneText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepTwoText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepThreeText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepFourText.setTextColor(Color.parseColor("#E2EBFE"))
                binding.tvStepFour.setBackgroundResource(R.drawable.ic_switch_thumb_checked)
            }
            else -> LogUtil.d("设置步骤错误")
        }
        viewModel.stepIndex.value = index
        //全选栏 布局
        binding.rlCheckAll.apply {
            visibility = if (index == 1 || index == 4) View.VISIBLE else View.GONE
        }
        //进度条
        binding.viewStep1.apply {
            visibility = if (index == 1) View.VISIBLE else View.INVISIBLE
        }
        binding.viewStep2.apply {
            visibility = if (index == 2) View.VISIBLE else View.INVISIBLE
        }
        binding.viewStep3.apply {
            visibility = if (index == 3) View.VISIBLE else View.INVISIBLE
        }
        binding.viewStep4.apply {
            visibility = if (index == 4) View.VISIBLE else View.INVISIBLE
        }
        binding.cbCheckAll.apply {
            visibility = if (index == 1) View.VISIBLE else View.GONE
        }
        binding.tvCheckAll.apply {
            visibility = if (index == 1) View.VISIBLE else View.GONE
        }
        binding.tvSelecedCount.apply {
            visibility = if (index == 1) View.VISIBLE else View.GONE
        }
        binding.tvFileNameTitle.apply {
            visibility = if (index == 4) View.VISIBLE else View.GONE
        }
        binding.edtGeneralViewName.apply {
            visibility = if (index == 4) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("NewApi")
    fun completeBinding() {
        var generalViewName = binding.edtGeneralViewName.getText().toString().trim { it <= ' ' }
        if ("" == generalViewName) {
            ToastUtil.show(resources.getString(R.string.str_general_view_name_input_tip))
            return
        }

        MainScope().launch {
            withContext(Dispatchers.Default) {
                var mMyRootMap = debugDao.searchMapRootName(generalViewName)
                if (mMyRootMap.size > 0 && !(myRootMapId != -1 && myRootMapName.equals(
                        generalViewName
                    ))
                ) {
                    ToastUtil.show(resources.getString(R.string.str_map_name_repetition_tip))
                } else {
                    if (isCreate) {
                        var mRootMap: RootMap = RootMap(0, generalViewName)
                        myRootMapId = debugDao.insertMapRoot(mRootMap).toInt()
                        for (item in viewModel.relationshipData) {
                            if (item.mPoint != null && item.selected) {
                                for (itemPoint in item.mPoint!!) {
                                    if (itemPoint.selected) {
                                        var mRelationshipPoint: RelationshipPoint =
                                            RelationshipPoint(
                                                0,
                                                myRootMapId,
                                                item.mSubMap.id,
                                                item.selectRouteId,
                                                itemPoint.id
                                            )
                                        debugDao.insertRelationshipPoint(mRelationshipPoint)
                                    }
                                }
                            }
                        }
                    } else {
                        debugDao.delteRelationshipPointByMapId(myRootMapId)
                        debugDao.updateMapRoot(myRootMapId, generalViewName)


                        Log.d("修改过的总图", "名字: ${generalViewName},修改时间: ${Date().time}")
                        val isExist = where("mapName = ?", generalViewName).count(MapRevise::class.java) > 0
                        if (!isExist){
                            var newMapRevise = MapRevise()
                            newMapRevise.mapName = generalViewName;
                            newMapRevise.time = Date().time
                            newMapRevise.save()
                        } else {
                            var mapRevise: MapRevise = where("mapName = ?", generalViewName)
                                .findFirst(MapRevise::class.java)
                            LogUtil.d("原数据: $mapRevise")
                            mapRevise.time = Date().time //更新isMale字段为false
//                            val isUpdateSucc: Boolean = mapRevise.save() //改成用save()保存字段
//                            LogUtil.d("更新time是否成功：$isUpdateSucc")
//                            mapRevise = where("mapName = ?", "mapRevise")
//                                .findFirst(MapRevise::class.java)
                            LogUtil.d("新数据: $mapRevise")
//                            mapRevise.save()
                        }

                        for (item in viewModel.relationshipData) {
                            if (item.mPoint != null && item.selected) {
                                for (itemPoint in item.mPoint!!) {
                                    if (itemPoint.selected) {
                                        var mRelationshipPoint: RelationshipPoint =
                                            RelationshipPoint(
                                                0,
                                                myRootMapId,
                                                item.mSubMap.id,
                                                item.selectRouteId,
                                                itemPoint.id
                                            )
                                        debugDao.insertRelationshipPoint(mRelationshipPoint)
                                    }
                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }
}