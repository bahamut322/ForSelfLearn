package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2022-04-08
 *   @describe: 重置工厂数据Dialog
 */
@SuppressLint("InflateParams")
class ResetFactoryDataDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
) : HideNavigationBarDialog(
    context,
    themeResId
) {
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_reset_factory_data, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        mWindowWidth = displayMetrics.widthPixels
        mWindowHeight = displayMetrics.heightPixels
        setContentView(
            dialogView, ViewGroup.MarginLayoutParams(
                mWindowWidth,
                mWindowHeight
            )
        )
    }
}