package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.sendi.deliveredrobot.R;

import java.text.NumberFormat;


public class MySeekBar extends BaseCustomView {
    private float seekMargin;
    private float signTextMargin;
    private float max = 100; // 最大值
    private float min = 0; // 最小值
    private float cur = 0; // 当前值
    private int digit = 0; // 小数保留多少位
    private Path seekbarPaht;
    private float bottom;
    private Bitmap seekbarDotBitmap; // 拖动点图片
    private int seekBarDotWidthHeight; // 拖动点宽高
    private Rect rectSrcSeekBarDot;
    private RectF rectDstSeekBarDot;
    private float seekX;
    private int[] cLocation;
    private SignView signView = new SignView(getContext());
    private View rootView;
    private int curTextSize = 22;
    private int curTextMargin = 12;
    private OnSeekBarChangeListener onSeekBarChangeListener;
    private boolean isSeeking = false;
    /** 是否显示当前值,默认显示 */
    private boolean isShowSign = true;
    private float w, h;
    private PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private NumberFormat nf = NumberFormat.getNumberInstance();

    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        baseContext = context;
    }

    public MySeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        baseContext = context;
    }

    public void setMax(float max) {
        this.max = max;
        invalidate();
    }

    public void setCur(float cur) {
        if (cur > this.max || cur < this.min) {
            return;
        }
        this.cur = cur;
        invalidate();
    }

    public void setRange(float min, float max) {
        this.min = min;
        this.max = max;
        this.cur = min;
        invalidate();
    }

    public void setRange(float min, float max, int digit) {
        this.min = min;
        this.max = max;
        this.cur = min;
        this.digit = digit;
        invalidate();
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public float getCur() {
        return cur;
    }

//    public float getCur(int digit) {
//        BigDecimal b = new BigDecimal(cur);
//        return b.setScale(digit, BigDecimal.ROUND_DOWN).floatValue();
//    }

    public String getCur(int digit) {
        if (digit < 1) {
            return String.valueOf((int) cur);
        }
        nf.setMaximumFractionDigits(digit);
//        LogUtil.i("robot sdk msgDto scale " + cur + " " + nf.format(cur));
        return nf.format(cur);
//        BigDecimal b = new BigDecimal(cur);
//        return String.valueOf(b.setScale(digit, BigDecimal.ROUND_DOWN).floatValue());
    }

    public int getCurInt() {
        return (int) cur;
    }

    @Override
    protected void initTool() {
        super.initTool();
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeCap(Paint.Cap.ROUND);
        basePaint.setStrokeWidth(ScreenUtil.dp2px(baseContext, 8));
        basePaint.setTextSize(ScreenUtil.sp2Px(baseContext, 26));
        basePaint.setTextAlign(Paint.Align.CENTER);
        basePaint.setColor(Color.parseColor("#0B1648"));
        basePaint.setAntiAlias(true);
        seekbarPaht = new Path();
//        seekbarDotBitmap = ToolsUtil.DrawableToBitmap(R.drawable.seekbar);
        seekbarDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar, null);
        rectSrcSeekBarDot = new Rect(0, 0, seekbarDotBitmap.getWidth(), seekbarDotBitmap.getHeight());
        rectDstSeekBarDot = new RectF(0, 0, 0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        seekBarDotWidthHeight = baseViewHeight;
        bottom = baseViewHeight / 2f; //baseViewHeight-seekbarDotBitmap.getHeight()/2-5; // baseViewHeight/2; //
        seekMargin = seekBarDotWidthHeight / 2f;
        signTextMargin = ScreenUtil.dp2px(baseContext, curTextMargin);
        rootView = getRootView();
        cLocation = new int[2];
//        getLocationOnScreen(cLocation);
        getLocationInWindow(cLocation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        LogUtil.d("seekbar cur " + cur+" seekbar cur int " + (int)cur);
//        canvas.translate(0, 100f);

//        seekbarDotBitmap = ToolsUtil.DrawableToBitmap(SkinCompatResources.getDrawable(baseContext, R.drawable.ic_seekbar_dot));
        seekbarDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar, null);
        rectSrcSeekBarDot = new Rect(0, 0, seekbarDotBitmap.getWidth(), seekbarDotBitmap.getHeight());
        rectDstSeekBarDot = new RectF(0, 0, 0, 0);

        seekX = (baseViewWidth - 2 * seekMargin) * ((cur - min) / (max - min)) + seekMargin;
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(ScreenUtil.dp2px(baseContext, 8));
        basePaint.setColor(Color.parseColor("#0B1648"));
        seekbarPaht.moveTo(seekMargin, bottom);
        seekbarPaht.lineTo(baseViewWidth - seekMargin, bottom);
        canvas.drawPath(seekbarPaht, basePaint);

        if (isSeeking) {
            basePaint.setColor(Color.parseColor("#4C7EDF"));
        } else {
            basePaint.setColor(Color.parseColor("#4C7EDF"));
        }
        basePaint.setStrokeWidth(ScreenUtil.dp2px(baseContext, 9));
        seekbarPaht.reset();
        seekbarPaht.moveTo(seekMargin, bottom);
        seekbarPaht.lineTo(seekX, bottom);
        canvas.drawPath(seekbarPaht, basePaint);

        rectDstSeekBarDot.left = seekX - seekBarDotWidthHeight / 2f + 0;
        rectDstSeekBarDot.top = baseViewHeight / 2f - seekBarDotWidthHeight / 2f + 0;
        rectDstSeekBarDot.right = seekX + seekBarDotWidthHeight / 2f - 0;
        rectDstSeekBarDot.bottom = baseViewHeight / 2f + seekBarDotWidthHeight / 2f - 0;

        canvas.setDrawFilter(paintFlagsDrawFilter);
        canvas.drawBitmap(seekbarDotBitmap, rectSrcSeekBarDot, rectDstSeekBarDot, basePaint);

        if (!isSeeking && isShowSign) {
            drawSign(canvas);
        }
    }

    /**
     * 绘制显示当前值的框框
     *
     * @param canvas
     */
    private void drawSign(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            basePaint.setColor(Color.parseColor("#BBD2FE"));
            basePaint.setStyle(Paint.Style.FILL);
            if (isSeeking) {
                w = ScreenUtil.dp2px(baseContext, 5);
                h = ScreenUtil.dp2px(baseContext, 7);
                curTextSize = 40;
                curTextMargin = 20;
            } else {
                w = ScreenUtil.dp2px(baseContext, 4);
                h = ScreenUtil.dp2px(baseContext, 5);
                curTextSize = 25;
                curTextMargin = 10;
            }
            basePaint.setTextSize(ScreenUtil.sp2Px(baseContext, curTextSize));
            signTextMargin = ScreenUtil.dp2px(baseContext, curTextMargin);

//            if (seekX + seekBarDotWidthHeight / 2f + w + signTextMargin * 2 + ScreenUtil.getFontWidth(basePaint, getCur(digit)) >= baseViewWidth) {
//                canvas.drawRoundRect(
//                        seekX - seekBarDotWidthHeight / 2f - w,
//                        bottom - ScreenUtil.getFontHeight(basePaint) / 2 - signTextMargin,
//                        seekX - seekBarDotWidthHeight / 2f - w - signTextMargin * 2 - ScreenUtil.getFontWidth(basePaint, getCur(digit)),
//                        bottom + ScreenUtil.getFontHeight(basePaint) / 2 + signTextMargin,
//                        10, 10, basePaint);
//
//                Paint.FontMetrics fontMetrics = basePaint.getFontMetrics();
//                float y = -fontMetrics.top - (fontMetrics.bottom - fontMetrics.top) / 2;
//                basePaint.setColor(Color.parseColor("#1F3280"));
//
//                canvas.drawText(getCur(digit),
//                        seekX - seekBarDotWidthHeight / 2f - w - signTextMargin - ScreenUtil.getFontWidth(basePaint, getCur(digit)) / 2,
//                        bottom + y,
//                        basePaint);
//
//                Path path = new Path();
//                path.moveTo(seekX - seekBarDotWidthHeight / 2f - w, bottom - h / 2);
//                path.lineTo(seekX - seekBarDotWidthHeight / 2f - w, bottom + h / 2);
//                path.lineTo(seekX - seekBarDotWidthHeight / 2f, bottom);
//                basePaint.setColor(Color.parseColor("#BBD2FE"));
//
//                canvas.drawPath(path, basePaint);
//            } else {
//                canvas.drawRoundRect(
//                        seekX + seekBarDotWidthHeight / 2f + w,
//                        bottom - ScreenUtil.getFontHeight(basePaint) / 2 - signTextMargin,
//                        seekX + seekBarDotWidthHeight / 2f + w + signTextMargin * 2 + ScreenUtil.getFontWidth(basePaint, getCur(digit)),
//                        bottom + ScreenUtil.getFontHeight(basePaint) / 2 + signTextMargin,
//                        10, 10, basePaint);
//
//                Paint.FontMetrics fontMetrics = basePaint.getFontMetrics();
//                float y = -fontMetrics.top - (fontMetrics.bottom - fontMetrics.top) / 2;
//                basePaint.setColor(Color.parseColor("#1F3280"));
//
//                canvas.drawText(getCur(digit),
//                        seekX + seekBarDotWidthHeight / 2f + w + signTextMargin + ScreenUtil.getFontWidth(basePaint, getCur(digit)) / 2,
//                        bottom + y,
//                        basePaint);
//
//                Path path = new Path();
//                path.moveTo(seekX + seekBarDotWidthHeight / 2f + w, bottom - h / 2);
//                path.lineTo(seekX + seekBarDotWidthHeight / 2f + w, bottom + h / 2);
//                path.lineTo(seekX + seekBarDotWidthHeight / 2f, bottom);
//                basePaint.setColor(Color.parseColor("#BBD2FE"));
//
//                canvas.drawPath(path, basePaint);
//            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        SignView signView = new SignView(getContext());
        getLocationInWindow(cLocation);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 按中拖动点的时候才能拖动，显示当前值 signView
                if (event.getX() >= (seekX - seekBarDotWidthHeight / 2f) && event.getX() <= (seekX + seekBarDotWidthHeight / 2f)) {
                    this.getParent().requestDisallowInterceptTouchEvent(true);
                    isSeeking = true;
                    // 把signView添加到根view，才能显示全
                    if (rootView instanceof ViewGroup) {
                        try {
                            ((ViewGroup) rootView).addView(signView);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    cur = (event.getX() - seekMargin) / (baseViewWidth - 2 * seekMargin) * (max - min) + min;
                    invalidate();
                } else {
                    return false;
                }
//                if (event.getX() >= seekMargin && event.getX() <= (baseViewWidth - seekMargin)
//                        && event.getY() >= (bottom - seekBarDotWidthHeight / 2f)
//                        && event.getY() <= (bottom + seekBarDotWidthHeight / 2f)) {
//                    this.getParent().requestDisallowInterceptTouchEvent(true);
//                    isSeeking = true;
//                    if (rootView instanceof ViewGroup) {
//                        try {
//                            ((ViewGroup) rootView).addView(signView);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                    cur = (event.getX() - seekMargin) / (baseViewWidth - 2 * seekMargin) * (max - min) + min;
//                    invalidate();
//                } else if (event.getX() > (baseViewWidth - seekMargin) && event.getX() <= ((baseViewWidth - seekMargin) + ScreenUtil.dp2px(baseContext, 10))) {
//                    this.getParent().requestDisallowInterceptTouchEvent(true);
//                    isSeeking = true;
//                    if (rootView instanceof ViewGroup) {
//                        try {
//                            ((ViewGroup) rootView).addView(signView);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                    cur = max;
//                    invalidate();
//                } else {
//                    return false;
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() >= seekMargin && event.getX() <= (baseViewWidth - seekMargin)) {
//                    cur = (event.getX() - seekMargin) / (baseViewWidth - 2 * seekMargin) * max;
                    cur = (event.getX() - seekMargin) / (baseViewWidth - 2 * seekMargin) * (max - min) + min;
                    invalidate();
                    signView.postInvalidate();
                } else if (event.getX() > (baseViewWidth - seekMargin)) {
                    cur = max;
                    invalidate();
                    signView.postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                isSeeking = false;
                ((ViewGroup) rootView).removeView(signView);
                invalidate();
                if (onSeekBarChangeListener != null) {
                    onSeekBarChangeListener.onChange(cur);
                }
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    /**
     * 拖动时显示当前值的view
     */
    private class SignView extends BaseCustomView {

        public SignView(Context context) {
            super(context);
        }

        public SignView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public SignView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void initTool() {
            super.initTool();
            basePaint.setStyle(Paint.Style.FILL);
            basePaint.setTextSize(ScreenUtil.sp2Px(baseContext, curTextSize));
            basePaint.setTextAlign(Paint.Align.CENTER);
            basePaint.setStrokeCap(Paint.Cap.ROUND);
            basePaint.setAntiAlias(true);
            w = ScreenUtil.dp2px(baseContext, 5);
            h = ScreenUtil.dp2px(baseContext, 7);
        }

        //3 2/ 10 5    // 5 7 22 12
        @Override
        protected void onDraw(Canvas canvas) {
            drawSign(canvas);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        private void drawSign(Canvas canvas) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                basePaint.setColor(Color.parseColor("#BBD2FE"));
                if (isSeeking) {
                    w = ScreenUtil.dp2px(baseContext, 5);
                    h = ScreenUtil.dp2px(baseContext, 7);
                    curTextSize = 33;
                    curTextMargin = 15;
                } else {
                    w = ScreenUtil.dp2px(baseContext, 3);
                    h = ScreenUtil.dp2px(baseContext, 2);
                    curTextSize = 10;
                    curTextMargin = 5;
                }
                basePaint.setTextSize(ScreenUtil.sp2Px(baseContext, curTextSize));
                signTextMargin = ScreenUtil.dp2px(baseContext, curTextMargin);
                canvas.drawRoundRect(cLocation[0] + seekX - ScreenUtil.getFontWidth(basePaint, getCur(digit)) / 2 - signTextMargin,
                        cLocation[1] + bottom - seekBarDotWidthHeight / 2f - ScreenUtil.getFontHeight(basePaint) - 2 * signTextMargin - h,
                        cLocation[0] + seekX + ScreenUtil.getFontWidth(basePaint, getCur(digit)) / 2 + signTextMargin,
                        cLocation[1] + bottom - seekBarDotWidthHeight / 2f - h,
                        10, 10, basePaint);

                basePaint.setColor(Color.parseColor("#1F3280"));
                canvas.drawText(getCur(digit), cLocation[0] + seekX, cLocation[1] + bottom - seekBarDotWidthHeight / 2f - signTextMargin - h, basePaint);

                Path path = new Path();
                path.moveTo(cLocation[0] + seekX - w, cLocation[1] + bottom - seekBarDotWidthHeight / 2f - h);
                path.lineTo(cLocation[0] + seekX, cLocation[1] + bottom - seekBarDotWidthHeight / 2f);
                path.lineTo(cLocation[0] + seekX + w, cLocation[1] + bottom - seekBarDotWidthHeight / 2f - h);
                basePaint.setColor(Color.parseColor("#BBD2FE"));
                canvas.drawPath(path, basePaint);
            }
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
    }

    public interface OnSeekBarChangeListener {
        void onChange(float current);
    }

    public boolean isShowSign() {
        return isShowSign;
    }

    public void setShowSign(boolean showSign) {
        isShowSign = showSign;
    }

    public boolean isSeeking() {
        return isSeeking;
    }

    public void setSeeking(boolean seeking) {
        isSeeking = seeking;
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec)+100, MeasureSpec.getMode(heightMeasureSpec));
//        setMeasuredDimension(widthMeasureSpec, height);
//    }
}

