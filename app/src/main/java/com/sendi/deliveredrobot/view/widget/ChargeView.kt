package com.sendi.deliveredrobot.view.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.sendi.deliveredrobot.utils.PxUtil

class ChargeView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    @SuppressLint("ObjectAnimatorBinding")
    var process: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    private val textPaint = Paint()
    private val circlePaint = Paint()

    companion object {
        const val TEXT_SIZE = 36f
    }

    init {
        textPaint.run {
            textSize = PxUtil.dp2px(context!!, TEXT_SIZE).toFloat()
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }

        circlePaint.run {
            setShadowLayer(120f, measuredWidth / 2f, measuredHeight / 2f, Color.GREEN)
            style = Paint.Style.STROKE
            color = Color.RED
            strokeWidth = 5f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, 100f, circlePaint)
        canvas?.drawText("$process%", measuredWidth / 2f, measuredHeight / 2f, textPaint)
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun startChargeAnim() {
        val animator = ObjectAnimator.ofInt(this, "process", 0, 100)
//        animator.repeatCount = ObjectAnimator.RESTART
        animator.duration = 5000
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }
}