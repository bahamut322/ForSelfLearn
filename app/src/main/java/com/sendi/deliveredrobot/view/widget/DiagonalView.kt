package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2021/8/4 10:52
 *   @describe: 对角线View
 */
class DiagonalView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(measuredWidth.toFloat(), 0f, 0f, measuredHeight.toFloat(), Paint().apply {
            color = ContextCompat.getColor(context,R.color.color_4D6FBE)
            strokeWidth = 1f
        })
    }
}