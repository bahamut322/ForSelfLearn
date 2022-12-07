package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity
import com.sendi.deliveredrobot.view.widget.SwipeMenuLayout

/**
 *   @author: heky
 *   @date: 2021/9/3 16:48
 *   @describe: 虚拟墙列表
 */
class VirtualWallListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val frameLayoutContainer: FrameLayout = itemView.findViewById(R.id.frameLayoutContainer)
    private val textViewMapName: TextView = itemView.findViewById(R.id.textViewMapName)
    val textViewDelete: TextView = itemView.findViewById(R.id.textViewDelete)
    private val swipeMenuLayout: SwipeMenuLayout = itemView.findViewById(R.id.swipeMenuLayout)

    var data: QuerySubMapEntity? = null
        set(value) {
            field = value
            textViewMapName.apply {
                isClickable = false
                val dataName = data?.name ?: ""
                val laserFile = resources.getString(R.string.limit_speed_file)
                val string = laserFile + dataName
                val color = ContextCompat.getColor(context,R.color.white)
                text = CommonHelper.getTipsSpan(laserFile.length, dataName, string, color, 1.07f)
            }
            textViewDelete.apply {
                isClickable = true
            }
            swipeMenuLayout.apply {
                isSwipeEnable = true
            }
        }
}