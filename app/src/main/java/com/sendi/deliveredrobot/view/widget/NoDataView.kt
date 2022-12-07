package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.utils.PxUtil

/**
 * @author heky
 * @date 2022-04-07
 * @describe 没有列表数据的缺省view
 */
@SuppressLint("UseCompatLoadingForDrawables")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NoDataView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var centerIcon: Bitmap? = null //图标
    private var centerMessage: String? = null //内容
    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#A0BAEF")
        textSize = 24f
    }
    private val bounds = Rect() //字的范围

    init {
        val typedArray = context!!.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NoDataView,
            0,
            0
        )
        val centerIconId = typedArray.getResourceId(R.styleable.NoDataView_center_icon, -1)
        val centerIconWidth = typedArray.getFloat(R.styleable.NoDataView_center_icon_width, 0f)
        val centerIconHeight = typedArray.getFloat(R.styleable.NoDataView_center_icon_height, 0f)
        centerIcon = context.resources.getDrawable(centerIconId,null).toBitmap(PxUtil.dp2px(context,centerIconWidth),PxUtil.dp2px(context,centerIconHeight))
        centerMessage = typedArray.getString(R.styleable.NoDataView_center_message)
        if (centerMessage != null) {
            paint.getTextBounds(centerMessage, 0, centerMessage!!.length, bounds)
        }
        typedArray.recycle()
        setBackgroundResource(R.drawable.shape_solid_e2ebfe_corners_10dp)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (centerIcon != null) {
            canvas?.drawBitmap(
                centerIcon!!,
                (measuredWidth - centerIcon?.width!!) / 2f ,
                (measuredHeight - centerIcon?.height!!) / 2f - measuredHeight / 10,
                paint
            )
        }
        if(!TextUtils.isEmpty(centerMessage)){
            canvas?.drawText(
                centerMessage!!,
                measuredWidth / 2f - bounds.width() / 2,
                measuredHeight / 2f - bounds.exactCenterY() + measuredHeight / 10,
                paint
            )
        }
    }
}