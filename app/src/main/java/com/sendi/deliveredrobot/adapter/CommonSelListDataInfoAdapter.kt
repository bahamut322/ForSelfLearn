package com.sendi.deliveredrobot.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.holder.BaseRecyclerHolder
import com.sendi.deliveredrobot.model.CommonListDataModel

/**
 * @author lsz
 * @desc 通用的选项列表 adapter
 * @date 2021/9/29 
 **/
class CommonSelListDataInfoAdapter : BaseRecyclerViewAdapter<CommonListDataModel>{

    private var curSel = -1
    private var lastSel = -1
    private var onSelChangeListener: OnSelChangeListener? = null
    private var mustSel = false

    constructor(context: Context, data: List<CommonListDataModel>?):super(context, data) {

    }

    override fun bindView(holder: BaseRecyclerHolder, position: Int) {
        holder.setText(R.id.tv_value, datas[position].value)
        var tvValue : TextView = holder.getView(R.id.tv_value)
        var ivSelect : ImageView =  holder.getView(R.id.imgv_select)
        if (curSel == position) {
            tvValue.setSelected(true)
            ivSelect.setVisibility(View.VISIBLE)
        } else {
            tvValue.setSelected(false)
            ivSelect.setVisibility(View.GONE)
        }
        tvValue.setOnClickListener(View.OnClickListener {
            if (curSel == position && !mustSel) {
                curSel = -1
                notifyItemChanged(position, 0)
            } else {
                lastSel = curSel
                curSel = position
                notifyItemChanged(position, 0)
                if (lastSel != -1) {
                    notifyItemChanged(lastSel, 0)
                }
            }
            if (onSelChangeListener != null) {
                onSelChangeListener!!.onSelChange(curSel, if (curSel == -1) null else datas[curSel])
            }
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.recycler_item_sel_list_data_info
    }

    fun getCurSel(): Int {
        return curSel
    }

    fun getCurSelData(): CommonListDataModel? {
        return if (curSel == -1) null else datas[curSel]
    }

    fun getCurSelDataNoNull(): CommonListDataModel {
        return if (curSel == -1) CommonListDataModel() else datas[curSel]
    }

    fun setMustSel(mustSel: Boolean) {
        this.mustSel = mustSel
    }

    fun setCurSel(curSel: Int) {
        this.curSel = curSel
        notifyDataSetChanged()
    }

    fun setCurSel(value: String?) {
        if (datas != null) {
            for (i in 0 until datas.size) {
                if (datas[i].value.equals(value)) {
                    setCurSel(i)
                    break
                }
            }
        }
    }

    fun setOnSelChangeListener(onSelChangeListener: OnSelChangeListener?) {
        this.onSelChangeListener = onSelChangeListener
    }

    interface OnSelChangeListener {
        fun onSelChange(position: Int, selDataInfo: CommonListDataModel?)
    }
}