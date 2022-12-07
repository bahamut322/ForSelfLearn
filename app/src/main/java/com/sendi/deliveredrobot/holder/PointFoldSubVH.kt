package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R

class PointFoldSubVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var mSubMenuText : TextView
    var cbSelect : CheckBox
    var tvTargetPointTitle : TextView
    init {
        cbSelect = itemView.findViewById<View>(R.id.cb_select) as CheckBox
        mSubMenuText = itemView.findViewById<View>(R.id.tv_sub_name) as TextView
        tvTargetPointTitle = itemView.findViewById<View>(R.id.tv_target_point_title) as TextView
    }
}