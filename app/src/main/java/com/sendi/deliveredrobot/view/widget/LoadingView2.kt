package com.sendi.deliveredrobot.view.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2021/10/11 11:51
 *   @describe: 后期做成可配置属性，与loading合并
 */
class LoadingView2(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var loading: Bitmap? = null
    private val paint = Paint()
    private var process: Float = -1.0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        startLoadingAnim()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        loading = BitmapFactory.decodeResource(
            context?.resources,
            R.drawable.ic_loading_2170e7,
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    (1f * measuredWidth / width),
                    (1f * measuredHeight / height)
                )
            }
            Bitmap.createBitmap(this,0,0,width, height, matrix,true)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.rotate(process * 100,measuredWidth / 2f,measuredHeight / 2f)
        if (loading != null) {
            canvas?.drawBitmap(
                loading!!,
                measuredWidth / 2f - loading!!.width / 2f,
                measuredHeight / 2f - loading!!.height / 2f,
                paint
            )
        }
    }

    private fun startLoadingAnim() {
        val animator = ObjectAnimator.ofFloat(this, "process", 0f, 3.6f)
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
}