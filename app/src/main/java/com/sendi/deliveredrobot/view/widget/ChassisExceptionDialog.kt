package com.sendi.deliveredrobot.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sendi.deliveredrobot.MyApplication
import com.sendi.deliveredrobot.R
import com.sendi.deliveredrobot.RobotCommand
import com.sendi.deliveredrobot.helpers.ROSHelper
import com.sendi.deliveredrobot.navigationtask.RobotStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 *   @author: heky
 *   @date: 2023/3/6
 *   @describe: 电机异常
 */
@SuppressLint("InflateParams")
class ChassisExceptionDialog(
    context: Context,
    themeResId: Int = R.style.simpleDialogStyle,
) : HideNavigationBarDialog(
    context,
    themeResId
) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    var textViewBackToCharge: TextView
    var timer: Timer = Timer()
    var seconds = 120

    init {
        val mWindowWidth: Int
        val mWindowHeight: Int
        val dialogView: View = LayoutInflater.from(MyApplication.instance!!)
            .inflate(R.layout.dialog_chassis_exception, null)
        val displayMetrics = MyApplication.instance!!.resources.displayMetrics
        dialogView.findViewById<TextView>(R.id.textViewDate).apply {
            text = sdf.format(Date())
        }
        textViewBackToCharge = dialogView.findViewById<TextView>(R.id.textViewBackToCharge).apply {
            isClickable = true
            setOnClickListener {
                MainScope().launch(Dispatchers.Default) {
                    if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
                        dismiss()
                        this@ChassisExceptionDialog.timer.cancel()
                    }
                }
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
                    MainScope().launch(Dispatchers.Default) {
                        if(RobotStatus.manageStatus == RobotCommand.MANAGE_STATUS_PAUSE){
                            if (ROSHelper.manageRobot(RobotCommand.MANAGE_STATUS_CONTINUE)) {
                                timer.cancel()
                                dismiss()
                            }else{
                                seconds = 120
                            }
                        }else{
                            seconds = 120
                        }
                    }
                }else{
                    seconds--
                    textViewBackToCharge.apply {
                        text = "确认完毕，继续任务（${seconds}s）"
                    }
                }
            }
        }, Date(), 1000)
    }
}