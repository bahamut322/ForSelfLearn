package com.sendi.deliveredrobot.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * @author sos0707
 * @desc
 * @date 2020/11/13 14:27
 **/
public class ScreenUtil {
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2Px(Context context, int sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (scaledDensity * sp + 0.5f * (sp >= 0 ? 1 : -1));
    }

    public static float getFontWidth(Paint paint, String text) {
        return paint.measureText(text);
    }

    public static float getFontHeight(Paint paint) {
        //文字基准线的下部距离-文字基准线的上部距离 = 文字高度
//        return paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        Rect rect = new Rect();
        paint.getTextBounds("1Txt", 0, "1Txt".length(), rect);
        return rect.height();
    }

    public static float getTextBaseLineY(Paint.FontMetrics fontMetrics) {
        return -fontMetrics.top - (fontMetrics.bottom - fontMetrics.top) / 2;
    }

}
