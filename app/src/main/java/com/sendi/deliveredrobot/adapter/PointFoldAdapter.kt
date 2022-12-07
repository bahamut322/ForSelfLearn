package com.sendi.deliveredrobot.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Trace
import android.support.annotation.AnimRes
import android.support.annotation.ColorRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.PointFoldMainVH
import com.sendi.deliveredrobot.holder.PointFoldSubVH
import com.sendi.deliveredrobot.model.AllMapRelationshipModel
import com.sendi.deliveredrobot.model.FoldSelMapModel
import com.sendi.deliveredrobot.utils.LogUtil
import java.util.*

class PointFoldAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private val mTotalMenus: ArrayList<FoldSelMapModel> = ArrayList<FoldSelMapModel>() //总共的菜单

    private val mMainMenus: ArrayList<FoldSelMapModel> = ArrayList<FoldSelMapModel>() //主菜单

    private lateinit var mContext: Context

    private val ITEM_MAIN_TYPE = 0
    private val ITEM_SUB_TYPE = 1
    private var mGridLayoutManager: GridLayoutManager? = null
    private var mItemClickCallBack: ItemClickCallBack? = null
    var expandIndex = 0
    var clickIndex = 0

    constructor(context: Context): super() {
        mContext = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(mContext)
        lateinit var viewHolder: RecyclerView.ViewHolder
        when (viewType) {
            ITEM_MAIN_TYPE -> viewHolder =
                createMainMenuVH(inflater, parent)
            ITEM_SUB_TYPE -> viewHolder =
                createSubMenuVH(inflater, parent)
            else -> {
            }
        }
        return viewHolder
    }

    /**
     * 创建子vh
     */
    private fun createSubMenuVH(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RecyclerView.ViewHolder {
        val inflate = inflater.inflate(subMenuLayoutId(), parent, false)
        return PointFoldSubVH(inflate)
    }

    /**
     * 主菜单的数据
     *
     * @return 主菜单的布局
     */
    protected fun mainMenuLayoutId(): Int{
        return R.layout.item_point_main_menu
    }

    /**
     * 创建主VH
     */
    private fun createMainMenuVH(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RecyclerView.ViewHolder {
        val inflate = inflater.inflate(mainMenuLayoutId(), parent, false)
        return PointFoldMainVH(inflate)
    }

    /**
     * 子菜单的数据
     *
     * @return 子菜单的布局
     */
    protected fun subMenuLayoutId(): Int{
        return R.layout.item_binding_point
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewType = getItemViewType(position)

        when (itemViewType) {
            ITEM_MAIN_TYPE -> {
                val mainMenuVH: PointFoldMainVH = holder as PointFoldMainVH
                mainMenuVH.itemView.setOnClickListener(View.OnClickListener {
                    if (mItemClickCallBack != null) {
                        val adapterPosition = holder.adapterPosition
                        var clickItem = mTotalMenus[adapterPosition]
                        clickIndex = adapterPosition
                        clickMainMenu(holder, adapterPosition)
                        mItemClickCallBack!!.onClickMainMenuItem(
                            clickItem,
                            clickIndex
                        )
                    }
                })

                mainMenuVH.cbSelectAll.setOnClickListener(View.OnClickListener {
                    val adapterPosition = holder.adapterPosition
                    var clickItem = mTotalMenus[adapterPosition]
                    clickItem.isAllSelect = !clickItem.isAllSelect
                    clickIndex = adapterPosition
                    clickSelectAll(adapterPosition,clickItem.isAllSelect)
                    mItemClickCallBack!!.onClickSelect(
                        clickItem,
                        clickIndex,
                        clickItem.isAllSelect
                    )
                })

                bindMainMenuData(mainMenuVH, mTotalMenus[position])
            }
            ITEM_SUB_TYPE -> {
                val subMenuVH: PointFoldSubVH = holder as PointFoldSubVH
                subMenuVH.itemView.setOnClickListener(View.OnClickListener {
                    if (mItemClickCallBack != null) {
                        val adapterPosition = holder.adapterPosition
                        val t: FoldSelMapModel = clickSubMenu(adapterPosition)
                        val subIndex = getSubIndex(t)
                        mItemClickCallBack!!.onClickSubMenuItem(
                            t.parentIndex,
                            subIndex,
                            t,
                            adapterPosition
                        )
                    }
                })
                bindSubMenuData(subMenuVH, mTotalMenus[position])
            }
        }
        LogUtil.d("PointFoldAdapter onBindViewHolder clickIndex:"+clickIndex)
    }

    /**
     * 点击主菜单
     *
     * @param holder
     * @param adapterPosition 主position
     */
    private fun clickMainMenu(holder: RecyclerView.ViewHolder, adapterPosition: Int) {
        val t: FoldSelMapModel = mTotalMenus[adapterPosition]
        if (t.mColl) {
            if (expandAnimation() != 0) {
                holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(
                        mContext,
                        expandAnimation()
                    )
                )
            }
            expand(adapterPosition, t)
        } else {
            if (collAnimation() != 0) {
                holder.itemView.startAnimation(
                    AnimationUtils.loadAnimation(
                        mContext,
                        collAnimation()
                    )
                )
            }
            coll(adapterPosition, t)
        }
        t.mColl = !t.mColl
    }

    private fun clickSelectAll(adapterPosition: Int,isCheck : Boolean) {
        val mFoldSelMapModel: FoldSelMapModel = mTotalMenus[adapterPosition]
        if(mFoldSelMapModel.mSubMenu != null){
            for (item in mFoldSelMapModel.mSubMenu){
                item.mSelect = isCheck
            }
        }
        mFoldSelMapModel.isAllSelect = isCheck
        notifyDataSetChanged()
    }

    /**
     * @return 折叠动画
     */
    @AnimRes
    protected open fun collAnimation(): Int {
        return 0
    }

    /**
     * @return 展开的动画
     */
    @AnimRes
    protected open fun expandAnimation(): Int {
        return 0
    }

    /**
     * 展开
     *
     * @param adapterPosition a
     * @param t               t
     */
    private fun expand(adapterPosition: Int, t: FoldSelMapModel) {
        LogUtil.d("PointFoldAdapter expand adapterPosition:"+adapterPosition)
        val size: Int = t.mSubMenu.size
        var oldExpandt : FoldSelMapModel = mTotalMenus[expandIndex]
        val b = mTotalMenus.addAll(adapterPosition + 1, t.mSubMenu)
        if (b) {
            mItemClickCallBack!!.onExpandListener(t, true)
//            notifyItemRangeInserted(adapterPosition + 1, size)
        }
        if(expandIndex != adapterPosition){
            //如果展开新的主菜单有已展开的，则删除上一个展开的数据源。并处理好index下标。
            if(!oldExpandt.mColl){
                LogUtil.d("PointFoldAdapter删除上一个展开的数据")
                oldExpandt.mColl = true
                var res = mTotalMenus.removeAll(oldExpandt.mSubMenu)
                //当新的index点击展开的主菜单是上一次展开的后面，则需要处理expandIndex和clickIndex
                if(adapterPosition > expandIndex){
                    expandIndex = (adapterPosition - oldExpandt.mSubMenu.size)
                    clickIndex = adapterPosition - oldExpandt.mSubMenu.size
                }else{
                    expandIndex = adapterPosition
                }
            }else{
                expandIndex = adapterPosition
            }
        }else{
            expandIndex = adapterPosition
        }
        notifyDataSetChanged()
    }

    /**
     * 折叠
     */
    private fun coll(adapterPosition: Int, t: FoldSelMapModel) {
        LogUtil.d("PointFoldAdapter coll adapterPosition:"+adapterPosition)
        Trace.beginSection("coll")
        val b = mTotalMenus.removeAll(t.mSubMenu)
        val size: Int = t.mSubMenu.size
        if (b) {
            mItemClickCallBack!!.onExpandListener(t, false)
//            notifyItemRangeRemoved(adapterPosition + 1, size)
        }
        notifyDataSetChanged()
        Trace.endSection()
        if(adapterPosition < expandIndex && expandIndex>0){
            expandIndex -= size
        }
    }

    /**
     * 点击子菜单
     *
     * @param adapterPosition position
     * @return
     */
    private fun clickSubMenu(adapterPosition: Int): FoldSelMapModel {
        val mFoldSelMapModel: FoldSelMapModel = mTotalMenus[adapterPosition]
        mFoldSelMapModel.mSelect = !mFoldSelMapModel.mSelect
//        val mainIndex = subMenu2index(t)
        if (mFoldSelMapModel.parentIndex >= 0){
            val mainMenu: FoldSelMapModel = mMainMenus[mFoldSelMapModel.parentIndex]
            if (mainMenu.mMultipleSelect) { //菜单是多选
                //根据计算子级选中数量设置父级全选的状态
                var selSubNum = 0;
                for (item in mainMenu.mSubMenu){
                    if(item.mSelect){
                        selSubNum += 1;
                    }
                }
                mainMenu.isAllSelect = selSubNum > 0 && selSubNum == mainMenu.mSubMenu.size
                notifyDataSetChanged()
//                subMenuMultipleSelect(adapterPosition, mFoldSelMapModel)
            } else { //单选
                subMenuSingleSelect(mFoldSelMapModel, mainMenu)
            }
        }
        return mFoldSelMapModel
    }

    /**
     * 单选
     */
    private fun subMenuSingleSelect(mFoldSelMapModel: FoldSelMapModel, mainMenu: FoldSelMapModel) {
        for (subFoldSelMapModel in mainMenu.mSubMenu) {
            subFoldSelMapModel.mSelect = mFoldSelMapModel.id == subFoldSelMapModel.id
        }
        notifyDataSetChanged()
    }

//    /**
//     * 主菜单在整个集合的index
//     *
//     * @param mainMenu 主菜单
//     * @return
//     */
//    private fun mainMenu2Index(mainMenu: T): Int {
//        return mTotalMenus.indexOf(mainMenu)
//    }


    /**
     * 子菜单多选的逻辑
     */
    private fun subMenuMultipleSelect(adapterPosition: Int, t: FoldSelMapModel) {
        notifyItemChanged(adapterPosition)
    }

    /**
     * 选择不是 不限的
     *
     * @param adapterPosition
     * @param t
     */
    private fun selectNoUnLimited(adapterPosition: Int, t: FoldSelMapModel) {
//        val index = subMenu2IndexForMap(t)
        mTotalMenus[adapterPosition].mSelect = !mTotalMenus[adapterPosition].mSelect
        notifyItemChanged(adapterPosition)
    }

    /**
     * 子菜单对应主菜单在totalList的index
     *
     * @param t
     * @return
     */
//    private fun subMenu2IndexForMap(t: T): Int {
//        return mTotalMenus.indexOf(subMenu2MainMenuBean(t))
//    }

    /**
     * 子菜单对应的主菜单的实体
     *
     * @param t t
     * @return
     */
    private fun subMenu2MainMenuBean(t: FoldSelMapModel): FoldSelMapModel? {
//        for (i in 0 until mMainMap.size()) {
//            val valueAt: T = mMainMap.valueAt(i)
//            for (o in valueAt.mSubMenu) {
//                if (o === t) {
//                    return valueAt
//                }
//            }
//        }
        return null
    }

    /**
     * 子菜单对应原数据的index
     *
     */
    private fun getSubIndex(mFoldSelMapModel: FoldSelMapModel): Int {
        for (i in 0 until mMainMenus[mFoldSelMapModel.parentIndex].mSubMenu.size) {
            val valueAt: FoldSelMapModel = mMainMenus[mFoldSelMapModel.parentIndex].mSubMenu[i]
            if (mFoldSelMapModel.id == valueAt.id) {
                return i
            }
        }
        return -1
    }

    /**
     * 设置子菜单 有一个可以不限
     *
     * @return
     */
    protected open fun hasUnlimited(): Boolean {
        return true
    }

    /**
     * 点击哪个菜单全部为未选
     */
    protected open fun isAllSelectNor(t: FoldSelMapModel?): Boolean {
        return false
    }

    /**
     * 子菜单全选
     *
     * @param t t
     */
    private fun changeAllSub(t: FoldSelMapModel) {

    }

    /**
     * 子菜单 对应的 主菜单在的mMainMap的index
     *
     * @param t t
     * @return index
     */
    private fun subMenu2index(t: FoldSelMapModel): Int {
//        for (i in 0 until mMainMap.size()) {
//            val valueAt: T = mMainMap.valueAt(i)
//            for (o in valueAt.mSubMenu) {
//                if (o === t) {
//                    return i
//                }
//            }
//        }
        return -1
    }

    /**
     * bind子菜单的数据
     *
     * @param subMenuVH     子菜单 vh
     * @param subMenuManger 子菜单数据
     */
    protected open fun bindSubMenuData(subMenuVH: PointFoldSubVH, subMenuManger: FoldSelMapModel) {

        subMenuVH.cbSelect.apply {
            isChecked = subMenuManger.mSelect
        }

        subMenuVH.mSubMenuText.setText(subMenuManger.name)
    }

    /**
     * 普通状态下 颜色
     *
     * @return
     */
    @ColorRes
    protected fun getNorColor(): Int{
        return R.color.menu_nor
    }

    /**
     * 子菜单呗选择中 颜色
     *
     * @return
     */
    @ColorRes
    protected fun getSelectColor(): Int{
        return R.color.menu_select
    }

    protected open fun bindMainMenuData(mainMenuVH: PointFoldMainVH, mFoldSelMapModel: FoldSelMapModel) {
        mainMenuVH.mTvMainMenu.setText(mFoldSelMapModel.name)
        if(mFoldSelMapModel.mColl){
            mainMenuVH.ivFoldExpand.background = mContext!!.resources.getDrawable(R.drawable.ic_fold)
            mainMenuVH.viewUnderline.visibility = View.GONE
            mainMenuVH.rlRouteMain.background = mContext!!.resources.getDrawable(R.drawable.shape_bg_round_0e40a4)
        } else {
            mainMenuVH.ivFoldExpand.setBackground(mContext!!.resources.getDrawable(R.drawable.ic_expand))
            mainMenuVH.viewUnderline.visibility = View.VISIBLE
            mainMenuVH.rlRouteMain.background = mContext!!.resources.getDrawable(R.drawable.shape_bg_round_top_0e40a4)
        }
        mainMenuVH.cbSelectAll.isChecked = mFoldSelMapModel.isAllSelect

    }

    open fun setItemClickCallBack(itemClickCallBack: ItemClickCallBack) {
        mItemClickCallBack = itemClickCallBack
    }

    override fun getItemViewType(position: Int): Int {
        return if (mTotalMenus[position].parentIndex == -1) {
            ITEM_MAIN_TYPE
        } else {
            ITEM_SUB_TYPE
        }
    }

    override fun getItemCount(): Int {
        return mTotalMenus.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: ArrayList<AllMapRelationshipModel>) {
        mMainMenus.clear()
        mTotalMenus.clear()

        if (dataList != null) {
            for ((index,item) in dataList.withIndex()){
                var mSubDataList: ArrayList<FoldSelMapModel> = ArrayList<FoldSelMapModel>() //子菜单
                for (mSubItem in item.mPoint!!) {
                    var subData : FoldSelMapModel = FoldSelMapModel(mSubItem.id, mSubItem.name,index,mSubItem.selected)
                    mSubDataList.add(subData)
                }
                var selSubNum = 0
                var isAllSelect = false
                if(item.mPoint != null){
                    for (item in item.mPoint!!){
                        if(item.selected){
                            selSubNum += 1;
                        }
                    }
                    isAllSelect = selSubNum > 0 && selSubNum == item.mPoint!!.size
                }
                var mFoldSelMapModel : FoldSelMapModel = FoldSelMapModel(item.mSubMap.id, item.mSubMap.name!!, -1, item.selected, true,
                    true, mSubDataList,index,item.selectRouteId,item.selectRouteName,isAllSelect)
                mMainMenus.add(mFoldSelMapModel)
            }
        }
        var firstSubData :Boolean = true
        for (i in mMainMenus.indices) {
            val mainMenu: FoldSelMapModel = mMainMenus[i]
            if(mainMenu.mSelect){
                mTotalMenus.add(mainMenu)
                if (mainMenu.mSubMenu.size !== 0 && firstSubData) {
                    //解决第一个子数据为空，首次展开的不是第一个主菜单。然后点击第一个主菜单展开不会收缩其他的展开的问题
                    expandIndex = mTotalMenus.size - 1
                    mTotalMenus.addAll(mainMenu.mSubMenu)
                    mainMenu.mColl = false
                    firstSubData = false
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is GridLayoutManager) {
            mGridLayoutManager = recyclerView.layoutManager as GridLayoutManager
        }
        mGridLayoutManager?.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (getItemViewType(position) == ITEM_SUB_TYPE) 1 else mGridLayoutManager!!.getSpanCount()
            }
        })
    }


    interface ItemClickCallBack {
        fun onClickMainMenuItem(t: FoldSelMapModel, adapterPosition: Int)
        fun onClickSubMenuItem(parentIndex: Int,subIndex:Int, t: FoldSelMapModel, adapterPosition: Int)
        fun onExpandListener(t: FoldSelMapModel, isExpand: Boolean)
        fun onClickSelect(t: FoldSelMapModel, adapterPosition: Int,isChecked :Boolean)
    }

}