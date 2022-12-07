package com.sendi.deliveredrobot.view.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.helpers.CommonHelper
import com.sendi.deliveredrobot.utils.PxUtil

@SuppressLint("ResourceType")
class BottomAlarmTextView(context: Context?, attrs: AttributeSet?) :
    AppCompatTextView(context!!, attrs) {
    private val paint: Paint = Paint()
    private val bitmapPaint = Paint()
    private var upAnim = false
    private var downAnim = false
    var bottomAlarmText1: String = ""
    var bottomAlarmText2: String = ""
    private val textSizePx = PxUtil.dp2px(context!!, TEXT_SIZE_DP).toFloat()
    private var process: Float = -1.0f
        set(value) {
            field = value
            invalidate()
        }
    private lateinit var bitmapArrowUp: Bitmap
    private lateinit var bitmapArrowDown: Bitmap

    companion object {
        const val TEXT_SIZE_DP = 50f
    }

//    init {
//        val obtainStyledAttributes =
//            context?.obtainStyledAttributes(attrs, R.styleable.BottomAlarmTextView)
//        bottomAlarmText =
//            obtainStyledAttributes?.getString(R.styleable.BottomAlarmTextView_bottom_alarm_text)
//                ?: ""
//        obtainStyledAttributes?.recycle()
//
//    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmapArrowUp = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_arrow_up_blue,
        ).run {
            val text = bottomAlarmText1 + bottomAlarmText2
            val bounds = Rect()
            paint.textSize = textSizePx
            paint.getTextBounds(text, 0, text.length, bounds)
            val matrix = Matrix().apply {
                postScale(
                    (bounds.height() / height / 2).toFloat(),
                    (bounds.height() / height / 2).toFloat()
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }

        bitmapArrowDown = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_arrow_down_blue,
        ).run {
            val text = bottomAlarmText1 + bottomAlarmText2
            val bounds = Rect()
            paint.textSize = textSizePx
            paint.getTextBounds(text, 0, text.length, bounds)
            val matrix = Matrix().apply {
                postScale(
                    (bounds.height() / height / 2).toFloat(),
                    (bounds.height() / height / 2).toFloat()
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
    }

    @SuppressLint("DrawAllocation")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        //draw background
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        canvas?.drawArc(
            -50f,
            0f,
            measuredWidth.toFloat() + 50f,
            measuredHeight.toFloat() * 1.75f,
            0f,
            -180f,
            false,
            paint
        )
        canvas?.drawRect(
            0f,
            measuredHeight.toFloat() * 1.75f / 2 - 1,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            paint
        )
        super.onDraw(canvas)
        //drawText
        paint.color = ContextCompat.getColor(context, R.color.color_4D6FBE)
        paint.textSize = textSizePx
        paint.textAlign = Paint.Align.CENTER
        val text = bottomAlarmText1 + bottomAlarmText2
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        setTextColor(ContextCompat.getColor(context, R.color.color_4D6FBE))
        textSize = textSizePx
        gravity = Gravity.CENTER
        setText(CommonHelper.getBottomTextSpan(bottomAlarmText1,bottomAlarmText2,ContextCompat.getColor(context,R.color.color_216FE8)))
        if (upAnim) {
            canvas?.save()
            canvas?.clipRect(
                measuredWidth / 2f - bounds.width() / 2 - bitmapArrowUp.width - 16f,
                measuredHeight / 2f - bounds.height() / 2f,
                measuredWidth / 2f - bounds.width() / 2f,
                measuredHeight / 2f + bounds.height() / 2f
            )
            val arrowCount = bounds.height() / bitmapArrowUp.height
            for (index in 0..arrowCount * 2 + 1) {
                if (index % 2 == 0) {
                    bitmapPaint.alpha = 255
                } else {
                    bitmapPaint.alpha = 130
                }
                canvas?.drawBitmap(
                    bitmapArrowUp,
                    measuredWidth / 2f - bounds.width() / 2f - bitmapArrowUp.width - 16f,
                    measuredHeight / 2f - bounds.height() / 2f - 2 * bitmapArrowUp.height + (bounds.height() + bitmapArrowUp.height * 2) * (1f - process) + bitmapArrowUp.height * index - bounds.height(),
                    bitmapPaint
                )
            }
            canvas?.restore()
        }

        if (downAnim) {
            canvas?.save()
            canvas?.clipRect(
                measuredWidth / 2f - bounds.width() / 2 - bitmapArrowUp.width - 16f,
                measuredHeight / 2f - bounds.height() / 2f,
                measuredWidth / 2f - bounds.width() / 2f,
                measuredHeight / 2f + bounds.height() / 2f
            )
            val arrowCount = bounds.height() / bitmapArrowUp.height
            for (index in 0..arrowCount * 2 + 1) {
                if (index % 2 == 0) {
                    bitmapPaint.alpha = 255
                } else {
                    bitmapPaint.alpha = 130
                }
                canvas?.drawBitmap(
                    bitmapArrowDown,
                    measuredWidth / 2f - bounds.width() / 2f - bitmapArrowUp.width - 16f,
                    measuredHeight / 2f - bounds.height() / 2f - 2 * bitmapArrowUp.height + (bounds.height() + bitmapArrowUp.height * 2) * (1f - process) + bitmapArrowUp.height * index - bounds.height(),
                    bitmapPaint
                )
            }
            canvas?.restore()
        }
    }

    @SuppressLint("ObjectAnimatorBinding", "Recycle")
    fun startUpArrowAnim() {
        upAnim = true
        val animator = ObjectAnimator.ofFloat(this, "process", 0f, 1f)
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.duration = 3000
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    @SuppressLint("ObjectAnimatorBinding", "Recycle")
    fun startDownArrowAnim() {
        downAnim = true
        val animator = ObjectAnimator.ofFloat(this, "process", 1f, 0f)
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.duration = 3000
        animator.interpolator = LinearInterpolator()
        animator.start()
    }
}