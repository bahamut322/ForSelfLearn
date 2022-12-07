package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sendi.deliveredrobot.*
import com.sendi.deliveredrobot.ACTION_NAVIGATE
import com.sendi.deliveredrobot.NAVIGATE_BUNDLE
import com.sendi.deliveredrobot.NAVIGATE_ID
import com.sendi.deliveredrobot.NAVIGATE_TO
import com.sendi.deliveredrobot.constants.InputPasswordFromType
import com.sendi.deliveredrobot.helpers.IdleGateDataHelper
import java.text.SimpleDateFormat
import java.util.*

class RemindDialog(
    message: String,
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
            .inflate(R.layout.dialog_robot_take_object, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        textViewDate = dialogView.findViewById(R.id.textViewDate)
        dialogView.findViewById<TextView>(R.id.textViewMessage).apply {
            text = message
        }
        dialogView.findViewById<TextView>(R.id.textViewGotoSetting).apply {
            setOnClickListener {
                IdleGateDataHelper.reportIdleGateCount(0)
                dismiss()
                MyApplication.instance!!.sendBroadcast(Intent().apply {
                    action = ACTION_NAVIGATE
//                    putExtra(NAVIGATE_ID, R.id.inputPasswordFragment)
                    putExtra(NAVIGATE_BUNDLE, Bundle().apply {
                        putString(InputPasswordFromType.INPUT_PASSWORD_FROM_TYPE, InputPasswordFromType.HOME_SETTING)
                    })
                })
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
        setDate()
        super.show()
    }
}