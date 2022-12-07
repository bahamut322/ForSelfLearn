package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.utils.PxUtil
import kotlin.properties.Delegates

class CustomIndicatorTextView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var unitWidth by Delegates.notNull<Int>()
    private var unitHeight by Delegates.notNull<Int>()
    private val inputs = arrayListOf<Char>()
    private val paint = Paint()
//    private val textSizePx = PxUtil.dp2px(context!!, TEXT_SIZE_DP).toFloat()
//    private var lineWidth = PxUtil.dp2px(context!!, LINE_WIDTH_DP).toFloat()
    private var spaceWidth by Delegates.notNull<Float>()
    private var textLength = 4


    companion object {
        const val TEXT_SIZE_DP = 50f
        const val LINE_STROKE_WIDTH_DP = 3f
//        const val LINE_WIDTH_DP = 50f
    }


    init {
        val typedArray = context!!.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomIndicatorTextView,
            0,
            0
        )
        val textSizePx: Float = typedArray.getFloat(R.styleable.CustomIndicatorTextView_text_size,TEXT_SIZE_DP)
        paint.apply {
            color = Color.WHITE
            textSize = textSizePx
            textAlign = Paint.Align.CENTER
            strokeWidth = PxUtil.dp2px(context!!, LINE_STROKE_WIDTH_DP).toFloat()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        unitWidth = measuredWidth / textLength
        unitHeight = measuredHeight / 2
        spaceWidth = unitWidth / 5f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val iterator = inputs.iterator()
        for (position in 0 until textLength) {
            canvas?.drawLine(
                (position * unitWidth).toFloat() + spaceWidth,
                measuredHeight.toFloat() - PxUtil.dp2px(context!!, LINE_STROKE_WIDTH_DP).toFloat(),
                ((position + 1) * unitWidth).toFloat() - spaceWidth,
                measuredHeight.toFloat() - PxUtil.dp2px(context!!, LINE_STROKE_WIDTH_DP).toFloat(),
                paint
            )
            if (!iterator.hasNext()) {
                continue
            }
            val text = iterator.next().toString()
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val offset = (bounds.top + bounds.bottom) / 2
            canvas?.drawText(
                text,
                ((2 * position + 1) * unitWidth).toFloat() / 2,
                unitHeight.toFloat() - offset,
                paint
            )
        }
    }

    fun setTextLength(length : Int){
        textLength = length
        invalidate()
    }

    fun addText(char: Char) {
        if (inputs.size !in 0 until textLength) return
        inputs.add(char)
        invalidate()
        if (inputs.size == textLength){
            indicatorTextViewCallback?.fullText(getText())
        }else if(inputs.size > 0){
            indicatorTextViewCallback?.hasText(getText())
        }else if(inputs.size == 0){
            indicatorTextViewCallback?.empty()
        }else if(inputs.size < textLength){
            indicatorTextViewCallback?.notFull()
        }
    }

    fun removeText() {
        if (inputs.size !in 1..textLength) return
        inputs.removeLast()
        indicatorTextViewCallback?.notFull()
        invalidate()
    }

    fun clearText() {
        if (inputs.size !in 1..textLength) return
        inputs.clear()
        indicatorTextViewCallback?.notFull()
        indicatorTextViewCallback?.empty()
        invalidate()
    }

    fun getText(): String {
        val stringBuilder = StringBuilder()
        for (input in inputs) {
            stringBuilder.append(input)
        }
        return stringBuilder.toString()
    }

    private var indicatorTextViewCallback:IndicatorTextViewCallback? = null
    fun setIndicatorTextViewListener(callback: IndicatorTextViewCallback){
        indicatorTextViewCallback = callback
    }

    interface IndicatorTextViewCallback{
        fun fullText(text:String)
        fun notFull()
        fun hasText(text:String)
        fun empty()

    }
}