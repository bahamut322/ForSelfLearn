package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * @author sos0707
 * @desc 基础view 打印生命周期
 * @date 2020/10/20 10:25
 **/
public abstract class BaseCustomView extends View {
    protected Paint basePaint;
    protected int baseViewWidth = 0, baseViewHeight = 0;
    protected Context baseContext;

    public BaseCustomView(Context context) {
        super(context);
//        LogUtil.d("customview");
        baseContext = context;
        initTool();
    }

    public BaseCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        LogUtil.d("customview");
        baseContext = context;
        initTool();
    }

    public BaseCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        LogUtil.d("customview");
        baseContext = context;
        initTool();
    }

    protected void initTool() {
        basePaint = new Paint();
        basePaint.setAntiAlias(true); //抗锯齿
//        LogUtil.d("customview");
    }

    public void startAnim() {
    }

    public void stopAnim() {
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        LogUtil.d("customview");
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
//        LogUtil.d("customview");
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        LogUtil.d("customview");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseViewWidth = w;
        baseViewHeight = h;
    }

    @Override
    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
//        LogUtil.d("customview");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        LogUtil.d("customview");
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
//        LogUtil.d("customview");
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
//        LogUtil.d("customview");
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
//        LogUtil.d("customview");
    }
}
