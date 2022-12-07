package com.sendi.deliveredrobot.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

class RightAlarmView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        canvas?.drawArc(
            measuredWidth.toFloat() / 5 * 3,
            -50f,
            measuredWidth.toFloat() / 5 * 4,
            measuredHeight.toFloat() + 50f,
            -90f,
            -180f,
            true,
            paint
        )
        canvas?.drawRect(
            measuredWidth.toFloat() / 5 * 3.5f,
            0f,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            paint
        )
    }

//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        val count = childCount
//        var currentHeight = 0
//        for (i in 0 until count) {
//            val view: View = getChildAt(i)
//            val height: Int = view.measuredHeight
//            val width: Int = view.measuredWidth
//            view.layout(l, currentHeight, l + width, currentHeight + height)
//            currentHeight += height
//        }
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        measureChildren(widthMeasureSpec, heightMeasureSpec)
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
//        val width = MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
//        val height = MeasureSpec.getSize(heightMeasureSpec)
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            val groupWidth: Int = _getMaxWidth()
//            val groupHeight: Int = getTotalHeight()
//            setMeasuredDimension(groupWidth, groupHeight)
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(_getMaxWidth(), height)
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(width, getTotalHeight())
//        }
//    }
//
//    private fun _getMaxWidth(): Int {
//        val count = childCount
//        var maxWidth = 0
//        for (i in 0 until count) {
//            val currentWidth = getChildAt(i).measuredWidth
//            if (maxWidth < currentWidth) {
//                maxWidth = currentWidth
//            }
//        }
//        return maxWidth
//    }
//
//    private fun getTotalHeight(): Int {
//        val count = childCount
//        var totalHeight = 0
//        for (i in 0 until count) {
//            totalHeight += getChildAt(i).measuredHeight
//        }
//        return totalHeight
//    }
}