package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.room.entity.QuerySubMapEntity

/**
 *   @author: heky
 *   @date: 2022-04-26
 *   @describe: 确认更新Dialog
 */
@SuppressLint("InflateParams")
class UpdateConfirmDialog(context: Context, themeResId: Int = R.style.simpleDialogStyle) :
    HideNavigationBarDialog(context, themeResId, needBlur = false) {
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(context)
            .inflate(R.layout.dialog_update_comfirm, null)
        dialogView.findViewById<TextView>(R.id.textViewConfirm).apply {
            setOnClickListener {
                listener.onConfirmButtonClick(this@UpdateConfirmDialog)
            }
        }
        dialogView.findViewById<TextView>(R.id.textViewCancel).apply {
            setOnClickListener {
                listener.onCancelButtonClick(this@UpdateConfirmDialog)
            }
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

    private lateinit var listener: OnButtonClickCallback

    interface OnButtonClickCallback {
        fun onConfirmButtonClick(dialog: UpdateConfirmDialog)
        fun onCancelButtonClick(dialog: UpdateConfirmDialog)
    }

    fun setOnItemClickListener(listener: OnButtonClickCallback) {
        this.listener = listener
    }
}