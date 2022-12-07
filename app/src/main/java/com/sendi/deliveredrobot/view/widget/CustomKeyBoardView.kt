package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.utils.PxUtil
import kotlin.math.max
import kotlin.properties.Delegates


class CustomKeyBoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var unitWidth by Delegates.notNull<Int>()
    private var unitHeight by Delegates.notNull<Int>()
    private var numberTextSize = PxUtil.dp2px(context!!, TEXT_SIZE_DP).toFloat()
    private val paint = Paint()
    private val textPaint = Paint()
    private var downRow = -1
    private var downCol = -1
    private val data = Array(ROW) { Array(COL) { ' ' } }
    private lateinit var bitmapClearWhite: Bitmap
    private lateinit var bitmapDeleteWhite: Bitmap
    private lateinit var bitmapClearBlue: Bitmap
    private lateinit var bitmapDeleteBlue: Bitmap
    private lateinit var bitmapConfirm: Bitmap

    private var keyboardType: Int

    companion object {
        const val COL = 3
        const val ROW = 4
        const val STROKE_WIDTH = 2f
        const val ROUND_CORNER = 25f
        const val SQUARE_RADIUS_SCALE_TIMES = 3.25f
        const val ROUND_RADIUS_SCALE_TIMES = 3f
        const val BITMAP_SCALE_TIMES = 4.25f
        const val TEXT_SIZE_DP = 36f
        private val TYPE_CLEAR_REMOVE = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', 'c', '0', 'r')
        private val TYPE_REMOVE_OK = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', 'r', '0', 'o')
        private var numTexts = TYPE_CLEAR_REMOVE
    }

    init {
        val typedArray = context!!.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomKeyBoardView,
            0,
            0
        )
        keyboardType = typedArray.getInt(R.styleable.CustomKeyBoardView_keyboard_type, 0)
        numTexts = when (keyboardType) {
            0 -> TYPE_CLEAR_REMOVE
            1 -> TYPE_REMOVE_OK
            else -> TYPE_CLEAR_REMOVE
        }
        paint.apply {
            color = ContextCompat.getColor(context, R.color.color_A0BAEF)
            isAntiAlias = true
        }
        textPaint.apply {
            textSize = numberTextSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        fillData()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        unitWidth = measuredWidth / COL
        unitHeight = measuredHeight / ROW
        bitmapClearWhite = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_clear_white,
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    unitHeight / height / BITMAP_SCALE_TIMES,
                    unitHeight / height / BITMAP_SCALE_TIMES
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
        bitmapDeleteWhite = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_delete_white,
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    unitHeight / height / BITMAP_SCALE_TIMES,
                    unitHeight / height / BITMAP_SCALE_TIMES
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }

        bitmapClearBlue = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_clear_blue,
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    unitHeight / height / BITMAP_SCALE_TIMES,
                    unitHeight / height / BITMAP_SCALE_TIMES
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
        bitmapDeleteBlue = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_delete_blue,
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    unitHeight / height / BITMAP_SCALE_TIMES,
                    unitHeight / height / BITMAP_SCALE_TIMES
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
        bitmapConfirm = getBitmap(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_baseline_check_36
            ) as VectorDrawable
        ).run {
            val matrix = Matrix().apply {
                postScale(
                    unitHeight / height / BITMAP_SCALE_TIMES,
                    unitHeight / height / BITMAP_SCALE_TIMES
                )
            }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    @SuppressLint("DrawAllocation")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = STROKE_WIDTH
        var i = 1
        var j = 1
        val iterator = numTexts.iterator()
        while (i <= ROW) {
            while (j <= COL) {
                var bitmapClear: Bitmap = bitmapClearWhite
                var bitmapDelete: Bitmap = bitmapDeleteWhite
                when (keyboardType) {
                    0 -> {
                        when {
                            i == downRow && j == downCol -> {
                                paint.apply {
                                    style = Paint.Style.FILL
                                    color = ContextCompat.getColor(context, R.color.color_2170E7)
                                }
                                textPaint.apply {
                                    color = Color.WHITE
                                }
                                bitmapClear = bitmapClearWhite
                                bitmapDelete = bitmapDeleteWhite

                            }
                            else -> {
                                paint.apply {
                                    style = Paint.Style.STROKE
                                    color = ContextCompat.getColor(context, R.color.color_A0BAEF)
                                }
                                textPaint.apply {
                                    color = ContextCompat.getColor(context, R.color.color_A0BAEF)
                                }
                                bitmapClear = bitmapClearBlue
                                bitmapDelete = bitmapDeleteBlue
                            }
                        }
                    }
                    1 -> {
                        when {
                            i == ROW && j == 1 -> {
                                paint.apply {
                                    style = Paint.Style.FILL
                                    color = ContextCompat.getColor(context, R.color.color_FF8282)
                                    shader = null
                                }
                            }
                            i == ROW && j == COL -> {
                                paint.apply {
                                    style = Paint.Style.FILL
                                    shader = LinearGradient(
                                        (j * unitWidth - unitWidth / 2).toFloat() - max(
                                            unitWidth,
                                            unitWidth
                                        ) / SQUARE_RADIUS_SCALE_TIMES,
                                        (i * unitHeight - unitHeight / 2).toFloat() - max(
                                            unitWidth,
                                            unitWidth
                                        ) / SQUARE_RADIUS_SCALE_TIMES,
                                        (j * unitWidth - unitWidth / 2).toFloat() - max(
                                            unitWidth,
                                            unitWidth
                                        ) / SQUARE_RADIUS_SCALE_TIMES,
                                        (i * unitHeight - unitHeight / 2).toFloat() + max(
                                            unitWidth,
                                            unitWidth
                                        ) / SQUARE_RADIUS_SCALE_TIMES,
                                        ContextCompat.getColor(context, R.color.color_27E7FC),
                                        ContextCompat.getColor(context, R.color.color_226CE9),
                                        Shader.TileMode.CLAMP
                                    )
                                }
                            }
                            i == downRow && j == downCol -> {
                                paint.apply {
                                    style = Paint.Style.FILL
                                    color = ContextCompat.getColor(context, R.color.color_2170E7)
                                    shader = null
                                }
                                textPaint.apply {
                                    color = Color.WHITE
                                }
                            }
                            else -> {
                                paint.apply {
                                    style = Paint.Style.STROKE
                                    color = ContextCompat.getColor(context, R.color.color_A0BAEF)
                                    shader = null
                                }
                                textPaint.apply {
                                    color = ContextCompat.getColor(context, R.color.color_A0BAEF)
                                }
                            }
                        }
                        bitmapClear = bitmapClearWhite
                        bitmapDelete = bitmapDeleteWhite
                    }
                }

                when {
                    (i == ROW && j == 1) || (i == ROW && j == COL) -> {
                        canvas?.drawRoundRect(
                            (j * unitWidth - unitWidth / 2).toFloat() - max(
                                unitWidth,
                                unitWidth
                            ) / SQUARE_RADIUS_SCALE_TIMES,
                            (i * unitHeight - unitHeight / 2).toFloat() - max(
                                unitWidth,
                                unitWidth
                            ) / SQUARE_RADIUS_SCALE_TIMES,
                            (j * unitWidth - unitWidth / 2).toFloat() + max(
                                unitWidth,
                                unitWidth
                            ) / SQUARE_RADIUS_SCALE_TIMES,
                            (i * unitHeight - unitHeight / 2).toFloat() + max(
                                unitWidth,
                                unitWidth
                            ) / SQUARE_RADIUS_SCALE_TIMES,
                            ROUND_CORNER,
                            ROUND_CORNER,
                            paint
                        )
                        when (iterator.next()) {
                            'c' -> {
                                canvas?.drawBitmap(
                                    bitmapClear,
                                    (j * unitWidth - unitWidth / 2).toFloat() - bitmapClear.width / 2,
                                    (i * unitHeight - unitHeight / 2).toFloat() - bitmapClear.height / 2,
                                    paint
                                )
                            }
                            'r' -> {
                                canvas?.drawBitmap(
                                    bitmapDelete,
                                    (j * unitWidth - unitWidth / 2).toFloat() - bitmapDelete.width / 2,
                                    (i * unitHeight - unitHeight / 2).toFloat() - bitmapDelete.height / 2,
                                    paint
                                )
                            }
                            'o' -> {
                                canvas?.drawBitmap(
                                    bitmapConfirm,
                                    (j * unitWidth - unitWidth / 2).toFloat() - bitmapClear.width / 2,
                                    (i * unitHeight - unitHeight / 2).toFloat() - bitmapClear.height / 2,
                                    paint
                                )
                            }
                        }
                    }
                    else -> {
                        canvas?.drawCircle(
                            (j * unitWidth - unitWidth / 2).toFloat(),
                            (i * unitHeight - unitHeight / 2).toFloat(),
                            max(unitWidth, unitWidth).toFloat() / ROUND_RADIUS_SCALE_TIMES,
                            paint
                        )
                        val bounds = Rect()
                        val text = iterator.next().toString()
                        textPaint.getTextBounds(text, 0, text.length, bounds)
                        val offset = (bounds.top + bounds.bottom) / 2
                        canvas?.drawText(
                            text,
                            (j * unitWidth - unitWidth / 2).toFloat(),
                            (i * unitHeight - unitHeight / 2).toFloat() - offset,
                            textPaint
                        )
                    }
                }
                j++
            }
            j = 1
            i++
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                downCol = (event.x / unitWidth).toInt() + 1
                downRow = (event.y / unitHeight).toInt() + 1
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                when (val text =
                    findData((event.y / unitHeight).toInt(), (event.x / unitWidth).toInt())) {
                    'c' -> callback?.onClearText()
                    'r' -> callback?.onRemoveText()
                    'o' -> callback?.onConfirm()
                    else -> {
                        if(text != ' '){
                            callback?.onPushText(text)
                        }
                    }
                }
                downRow = -1
                downCol = -1
                invalidate()
            }
        }
        return true
    }

    private fun fillData() {
        val iterator = numTexts.iterator()
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                data[i][j] = iterator.next()
            }
        }
    }

    private fun findData(row: Int, col: Int): Char {
        if (row !in 0 until ROW || col !in 0 until COL) return ' '
        return data[row][col]
    }

    private var callback: KeyBoardCallback? = null
    fun setKeyBoardListener(keyBoardCallback: KeyBoardCallback) {
        callback = keyBoardCallback
    }

    abstract class KeyBoardCallback {
        abstract fun onPushText(char: Char)
        open fun onRemoveText(){}
        open fun onClearText(){}
        open fun onConfirm(){}
    }
}
