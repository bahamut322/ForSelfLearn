package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author heky
 * @date 2021/11/24
 * @describe 送物失败弹窗
 */
class SendFailDialog(
    message: String,
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    needBlur: Boolean = true,
    sendFailListener: SendFailListener
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
                dismiss()
                sendFailListener.buttonPress(this@SendFailDialog)
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
    interface SendFailListener {
        fun buttonPress(dialog: SendFailDialog)
    }
}