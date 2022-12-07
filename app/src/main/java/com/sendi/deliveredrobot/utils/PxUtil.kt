package com.sendi.deliveredrobot.utils

import android.content.Context

object PxUtil{
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale: Float = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    fun px2dp(context: Context, pxValue: Int): Float {
        val scale: Float = context.resources.displayMetrics.density
        return (pxValue - 0.5f) / scale
    }
}
