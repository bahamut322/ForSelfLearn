package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R

class AllBindingFoldMainVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var mTvMainMenu: TextView
    var tvRouteMap: TextView
    var ivFoldExpand :ImageView
    var viewUnderline :View
    var rlRouteMain : RelativeLayout
    init {
        mTvMainMenu = itemView.findViewById(R.id.tv_laser_map)
        tvRouteMap = itemView.findViewById(R.id.tv_route_map)
        ivFoldExpand = itemView.findViewById(R.id.iv_fold_expand)
        viewUnderline = itemView.findViewById(R.id.view_underline)
        rlRouteMain = itemView.findViewById(R.id.rl_route_main_menu)
    }
}