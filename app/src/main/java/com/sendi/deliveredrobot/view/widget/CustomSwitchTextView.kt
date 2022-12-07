package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.utils.PxUtil
import kotlin.properties.Delegates

/**
 * @author heky
 * @date 2022-02-23
 * @
 */
@SuppressLint("ResourceType", "UseCompatLoadingForDrawables")
class CustomSwitchTextView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var defaultTextColor: Int = -1
    private var selectTextColor: Int = -1
    var viewSelected: Boolean by Delegates.observable(false){_,_,newValue ->
        listener?.onToggle(newValue)
    }
    private var leftText: String = ""
    private var rightText: String = ""
    private var leftSelectBackground: Int = -1
    private var rightSelectBackground: Int = -1
    private var leftBackground: Bitmap? = null
    private var rightBackground: Bitmap? = null
    private var viewTextSize: Float = 1f
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
    }
    private val bounds = Rect()

    init {
        val typedArray = context!!.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomSwitchTextView,
            0,
            0
        )
        val defaultTextColorId: Int = typedArray.getResourceId(R.styleable.CustomSwitchTextView_default_text_color, R.color.white)
        val selectTextColorId = typedArray.getResourceId(R.styleable.CustomSwitchTextView_select_text_color, R.color.white)
        viewSelected = typedArray.getBoolean(R.styleable.CustomSwitchTextView_view_selected, false)
        val viewBackGround:Int = typedArray.getResourceId(R.styleable.CustomSwitchTextView_view_background,-1)
        leftText = typedArray.getString(R.styleable.CustomSwitchTextView_left_text)?:""
        rightText = typedArray.getString(R.styleable.CustomSwitchTextView_right_text)?:""
        leftSelectBackground = typedArray.getResourceId(R.styleable.CustomSwitchTextView_left_select_background, -1)
        rightSelectBackground = typedArray.getResourceId(R.styleable.CustomSwitchTextView_right_select_background, -1)
        val viewTextSizeDp = typedArray.getFloat(R.styleable.CustomSwitchTextView_view_text_size, 1f)
        typedArray.recycle()
        setBackgroundResource(viewBackGround)
        defaultTextColor = ContextCompat.getColor(context, defaultTextColorId)
        selectTextColor = ContextCompat.getColor(context, selectTextColorId)
        viewTextSize = PxUtil.dp2px(context, viewTextSizeDp).toFloat()
        paint.apply {
            textSize = viewTextSize
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        leftBackground = context.resources.getDrawable(leftSelectBackground,null).toBitmap(measuredWidth / 2,measuredHeight)
        rightBackground = context.resources.getDrawable(rightSelectBackground,null).toBitmap(measuredWidth / 2,measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        when(viewSelected){
            false -> {
                //left
                //左边背景
                if (leftBackground != null){
                    canvas?.drawBitmap(leftBackground!!, 0f, 0f,paint)
                }
                //左边文字
                paint.getTextBounds(leftText, 0, leftText.length, bounds)
                canvas?.drawText(
                    leftText,
                    measuredWidth / 4f - bounds.width() / 2,
                    measuredHeight / 2f - bounds.exactCenterY(),
                    paint.apply {
                        color = selectTextColor
                    }
                )
                //右边文字
                paint.getTextBounds(rightText, 0, rightText.length, bounds)
                canvas?.drawText(
                    rightText,
                    measuredWidth / 4f * 3 - bounds.width() / 2,
                    measuredHeight / 2f - bounds.exactCenterY(),
                    paint.apply {
                        color = defaultTextColor
                    }
                )
            }
            true -> {
                //right
                //左边背景
                if (rightBackground != null){
                    canvas?.drawBitmap(rightBackground!!, measuredWidth / 2f, 0f,paint)
                }
                //左边文字
                paint.getTextBounds(leftText, 0, leftText.length, bounds)
                canvas?.drawText(
                    leftText,
                    measuredWidth / 4f - bounds.width() / 2,
                    measuredHeight / 2f - bounds.exactCenterY(),
                    paint.apply {
                        color = defaultTextColor
                    }
                )
                //右边文字
                paint.getTextBounds(rightText, 0, rightText.length, bounds)
                canvas?.drawText(
                    rightText,
                    measuredWidth / 4f * 3 - bounds.width() / 2,
                    measuredHeight / 2f - bounds.exactCenterY(),
                    paint.apply {
                        color = selectTextColor
                    }
                )
            }
        }

    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                viewSelected = !viewSelected
                invalidate()
                listener?.onToggle(viewSelected)
            }
        }
        return super.onTouchEvent(event)
    }

    interface OnToggleListener {
        fun onToggle(selected: Boolean)
    }

    private var listener: OnToggleListener? = null
    fun setOnToggleListener(onToggleListener: OnToggleListener) {
        listener = onToggleListener
    }
}

