package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.model.VirtualWallLineInfoModel
import com.sendi.deliveredrobot.view.widget.SwipeMenuLayout

/**
 *   @author: heky
 *   @date: 2022-03-03
 *   @describe: 虚拟墙Line列表
 */
class VirtualWallLineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    val textViewDelete: TextView = itemView.findViewById(R.id.textViewDelete)
    private val swipeMenuLayout: SwipeMenuLayout = itemView.findViewById(R.id.swipeMenuLayout)

    var data: LineInfoModel? = null
        set(value) {
            field = value
            textViewName.apply {
                text = data?.name?:""
            }
            textViewDelete.apply {
                isClickable = true
            }
            swipeMenuLayout.apply {
                isSwipeEnable = true
            }
        }
}