package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.LineInfoModel
import com.sendi.deliveredrobot.view.widget.SwipeMenuLayout

/**
 *   @author: heky
 *   @date: 2021/9/3 16:48
 *   @describe: 限速区Line列表
 */
class LimitSpeedLineListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    val textViewEdit: TextView = itemView.findViewById(R.id.textViewEdit)
    val textViewDelete: TextView = itemView.findViewById(R.id.textViewDelete)
    private val swipeMenuLayout: SwipeMenuLayout = itemView.findViewById(R.id.swipeMenuLayout)

    var data: LineInfoModel? = null
        set(value) {
            field = value
            textViewName.apply {
                text = data?.name?:""
            }
            textViewEdit.apply {
                isClickable = true
            }
            textViewDelete.apply {
                isClickable = true
            }
            swipeMenuLayout.apply {
                isSwipeEnable = true
            }
        }
}