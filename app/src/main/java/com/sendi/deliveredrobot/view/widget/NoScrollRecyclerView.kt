package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class NoScrollRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    @SuppressLint("Range")
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(Int.MAX_VALUE, MeasureSpec.AT_MOST))
    }
}