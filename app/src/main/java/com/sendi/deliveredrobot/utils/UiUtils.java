package com.sendi.deliveredrobot.utils;

import android.content.Context;

import com.sendi.deliveredrobot.MyApplication;

import java.util.Objects;

public class UiUtils {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = Objects.requireNonNull(MyApplication.Companion.getInstance()).getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = Objects.requireNonNull(MyApplication.Companion.getInstance()).getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
