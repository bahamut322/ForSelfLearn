package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2022-04-11
 *   @describe: 重置工厂数据结果的Dialog
 */
@SuppressLint("InflateParams")
class ResetFactoryDataStatusDialog(
    context: Context,
    result: Int,
    themeResId: Int = R.style.simpleDialogStyle,
) : HideNavigationBarDialog(
    context,
    themeResId
) {
    companion object{
        const val RESET_TYPE_SUCCESS = 0x11
        const val RESET_TYPE_FAILURE = 0x22
    }
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_reset_factory_data_status, null)
        val textViewContent = dialogView.findViewById<TextView>(R.id.textViewContent)
        val viewStatus = dialogView.findViewById<View>(R.id.viewStatus)
        when (result) {
            RESET_TYPE_SUCCESS -> {
                textViewContent.apply {
                    text = context.resources.getText(R.string.factory_data_reset_already)
                }
                viewStatus.apply {
                    background = ContextCompat.getDrawable(context,R.drawable.ic_selected)
                }
            }
            RESET_TYPE_FAILURE -> {
                textViewContent.apply {
                    text = context.resources.getText(R.string.factory_data_reset_failure)
                }
                viewStatus.apply {
                    background = ContextCompat.getDrawable(context,R.drawable.ic_delect)
                }
            }
            else -> {}
        }
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