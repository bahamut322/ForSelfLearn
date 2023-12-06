package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.sendi.deliveredrobot.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TroubleDialog(
    context: Context,
    themeResId: Int = R.style.Dialog,
    needBlur: Boolean = true
) : HideNavigationBarDialog(context = context, themeResId = themeResId, needBlur = needBlur) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    private var textViewDate:TextView
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_robot_in_trouble, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        textViewDate = dialogView.findViewById(R.id.textViewDate)
        dialogView.findViewById<TextView>(R.id.textViewManualHandling).apply {
            isEnabled = true
            isClickable = true
            setOnClickListener {
                isEnabled = false
                isClickable = false
                MyApplication.instance!!.sendBroadcast(
                    Intent().apply {
                        action = ACTION_NAVIGATE
                        putExtra(NAVIGATE_ID, R.id.commonHandleExceptionFragment)
                    }
                )
                dismiss()
                isEnabled = true
                isClickable = true
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

    private fun setDate(){
        textViewDate.text = sdf.format(Date())
    }

    override fun show() {
        MainScope().launch {
            setDate()
            super.show()
        }
    }
}