package com.sendi.deliveredrobot.helpers

import android.app.Activity
import android.view.WindowManager

/**
 * @describe 基础设置
 */

object BasicSettingHelper {
    /**
     * @describe 调节窗口亮度
     */
    fun setBrightness(activity: Activity, brightness: Int) {
        val lp: WindowManager.LayoutParams = activity.window.attributes
        lp.screenBrightness = brightness / 100f
        activity.window.attributes = lp
    }
}
