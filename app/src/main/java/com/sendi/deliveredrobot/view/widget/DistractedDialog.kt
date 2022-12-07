package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
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

@SuppressLint("InflateParams")
class DistractedDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    needBlur: Boolean = true
) : HideNavigationBarDialog(context = context, themeResId = themeResId, needBlur = needBlur) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    private var textViewDate:TextView
    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_robot_in_lose, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<ImageView>(R.id.imageViewClose).apply {
            isClickable = true
            setOnClickListener {
                dismiss()
            }
        }
        textViewDate = dialogView.findViewById(R.id.textViewDate)
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