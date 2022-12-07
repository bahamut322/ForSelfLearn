package com.sendi.deliveredrobot.view.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2021/10/11 11:51
 *   @describe:
 */
class LoadingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val loading = BitmapFactory.decodeResource(
        context?.resources,
        R.drawable.ic_loading_e2ebfe,
        BitmapFactory.Options().apply {
            outWidth = 240
            outHeight = 240
        })
    private val paint = Paint().apply {
        alpha = 80
    }
    private var process: Float = -1.0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        startLoadingAnim()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.rotate(process * 100,measuredWidth / 2f,measuredHeight / 2f)
        canvas?.drawBitmap(
            loading,
            measuredWidth / 2f - loading.width / 2f,
            measuredHeight / 2f - loading.height / 2f,
            paint
        )
    }

    private fun startLoadingAnim() {
        val animator = ObjectAnimator.ofFloat(this, "process", 0f, 3.6f)
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
}