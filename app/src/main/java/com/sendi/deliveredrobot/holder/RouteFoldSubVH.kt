package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R

class RouteFoldSubVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var mSubMenuText: TextView
    var ivSelected: ImageView
    var viewRouteUnderline : View
    init {
        ivSelected = itemView.findViewById<View>(R.id.iv_selected) as ImageView
        mSubMenuText = itemView.findViewById<View>(R.id.tv_sub_name) as TextView
        viewRouteUnderline = itemView.findViewById<View>(R.id.view_route_underline) as View
    }
}