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
 *   @author: heky
 *   @date: 2021/8/25 14:21
 *   @describe: 低电量
 */
@SuppressLint("InflateParams")
class LowPowerDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
    lowPowerDialogListener: LowPowerDialogListener
) : HideNavigationBarDialog(
    context,
    themeResId
) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var textViewBackToCharge: TextView
    var timer: Timer = Timer()
    var seconds = 10

    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_robot_low_power, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewDate).apply {
            text = sdf.format(Date())
        }
        textViewBackToCharge = dialogView.findViewById<TextView>(R.id.textViewBackToCharge).apply {
            isClickable = true
            setOnClickListener {
                this@LowPowerDialog.timer?.cancel()
                lowPowerDialogListener.buttonPress(this@LowPowerDialog)
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

        timer.schedule(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (seconds < 0) {
                    timer.cancel()
                    lowPowerDialogListener.timeUp(this@LowPowerDialog)
                }else{
                    seconds--
                    textViewBackToCharge.apply {
                        text = "前往充电（${seconds}s）"
                    }
                }
            }
        }, Date(), 1000)
    }

    interface LowPowerDialogListener {
        fun timeUp(dialog: LowPowerDialog)
        fun buttonPress(dialog: LowPowerDialog)
    }
}