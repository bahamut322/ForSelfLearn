package com.sendi.deliveredrobot.holder

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.model.SettingHomeModel

class SettingHomeListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.tv)
    var data: SettingHomeModel? = null
        set(data) {
            val model = data
            val selected = model?.selected
            if (selected!!) {
                textView.apply {
                    text = model.title
                    textSize = context.resources.getDimension(R.dimen.small_text_size)
                    background = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.shape_gradient_226ce9_0e40a4
                    )
                    setTextColor(ContextCompat.getColor(context!!, R.color.white))
                }
            } else {
                textView.apply {
                    text = model.title
                    textSize = context.resources.getDimension(R.dimen.small_small_text_size)
                    setBackgroundResource(R.color.transparency)
                    setTextColor(ContextCompat.getColor(context!!, R.color.color_4D6FBE))
                }
            }
            field = data
        }
}