package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R

class PointFoldMainVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var mTvMainMenu: TextView
    var ivFoldExpand :ImageView
    var viewUnderline :View
    var cbSelectAll : CheckBox
    var rlRouteMain : RelativeLayout
    init {
        mTvMainMenu = itemView.findViewById(R.id.tv_main_menu)
        ivFoldExpand = itemView.findViewById(R.id.iv_fold_expand)
        viewUnderline = itemView.findViewById(R.id.view_underline)
        cbSelectAll = itemView.findViewById(R.id.cb_select_all)
        rlRouteMain = itemView.findViewById(R.id.rl_route_main_menu)
    }
}