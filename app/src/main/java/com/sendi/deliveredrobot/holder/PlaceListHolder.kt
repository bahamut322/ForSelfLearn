package com.sendi.deliveredrobot.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.PlaceModel

@SuppressLint("ResourceAsColor")
class PlaceListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.tv)
    var data: ArrayList<PlaceModel>? = null
        set(data) {
            field = data
            val model = field?.get(adapterPosition)
            val selected = model?.selected
            if (selected!!) {
                textView.apply {
                    text = model.location.pointName
                    background = ContextCompat.getDrawable(context!!, R.drawable.shape_solid_2170e7_corners_10dp)
                    setTextColor(ContextCompat.getColor(context!!, R.color.white))
                }
            } else {
                textView.apply {
                    background = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.shape_stroke_a0baef_solid_transparency
                    )
                    text = model.location.pointName
                    setTextColor(ContextCompat.getColor(context!!, R.color.color_A0BAEF))
                }
            }
        }
}