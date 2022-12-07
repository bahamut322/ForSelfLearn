package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 *   @author: heky
 *   @date: 2021/9/21 16:08
 *   @describe: 十字键盘Layout
 */
class CrossKeyboardBackGroundView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val path = Path()
    private val lineColor = Color.parseColor("#A0BAEF")
    private val paint = Paint().apply {
        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.moveTo(measuredWidth / 3f,2f)
        path.lineTo(measuredWidth / 3f * 2, 2f)
        path.lineTo(measuredWidth / 3f * 2,measuredHeight / 3f)
        path.lineTo(measuredWidth -2f,measuredHeight / 3f)
        path.lineTo(measuredWidth -2f,measuredHeight / 3f * 2)
        path.lineTo(measuredWidth / 3f * 2,measuredHeight / 3f * 2)
        path.lineTo(measuredWidth / 3f * 2,measuredHeight -2f)
        path.lineTo(measuredWidth / 3f,measuredHeight - 2f)
        path.lineTo(measuredWidth / 3f,measuredHeight / 3f * 2)
        path.lineTo(2f,measuredHeight / 3f * 2)
        path.lineTo(2f,measuredHeight / 3f)
        path.lineTo(measuredWidth / 3f,measuredHeight / 3f)
        path.close()
        canvas?.drawPath(path,paint)
    }
}