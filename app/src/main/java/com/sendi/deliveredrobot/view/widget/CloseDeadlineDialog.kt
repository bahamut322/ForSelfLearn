package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R

/**
 *   @author: heky
 *   @date: 2022-04-14
 *   @describe: 接近机器人使用期限到期Dialog
 */
@SuppressLint("InflateParams")
class CloseDeadlineDialog(context: Context, content: SpannableStringBuilder, themeResId: Int = R.style.simpleDialogStyle) :
    HideNavigationBarDialog(context, themeResId, needBlur = true) {
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_close_deadline_remind, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        val textViewContent = dialogView.findViewById<TextView>(R.id.textViewContent)
        textViewContent.apply {
            text = content
        }
        dialogView.findViewById<TextView>(R.id.textViewCommit).apply {
            isClickable = true
            setOnClickListener {
                dismiss()
            }
        }
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