package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.adapter.base.i.BaseRecyclerViewHolder
import com.sendi.deliveredrobot.model.AllMapRelationshipModel

/**
 * @author lsz
 * @describe 选择激光图列表
 * @date 2021/9/9
 */
class SelLaserHolder(itemView: View) : BaseRecyclerViewHolder<AllMapRelationshipModel>(itemView) {
    val rlContainer: RelativeLayout = itemView.findViewById(R.id.rl_container)
    private val tvName: TextView = itemView.findViewById(R.id.tv_name)
    val cbSelSubmap: CheckBox = itemView.findViewById(R.id.cb_sel_submap)


    override fun bindData(entity: AllMapRelationshipModel) {
        tvName.text = entity.mSubMap.name
        cbSelSubmap.isChecked = entity.selected
    }
}