package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.room.entity.QueryPointEntity

class SelectChargePointListHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val frameLayoutContainer = itemView
    private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    private val imageViewSelect:ImageView = itemView.findViewById(R.id.imageViewSelect)
    var data: QueryPointEntity? = null
        set(value) {
        field = value
        val data = field
        val selected = data?.selected
        if (selected!!) {
            textViewName.apply {
                text = data.pointName
                setTextColor(ContextCompat.getColor(context!!, R.color.white))
            }
            imageViewSelect.apply {
                visibility = View.VISIBLE
            }
        } else {
            textViewName.apply {
                text = data.pointName
                setTextColor(ContextCompat.getColor(context!!, R.color.color_A0BAEF))
            }
            imageViewSelect.apply {
                visibility = View.GONE
            }
        }
    }
}