package com.sendi.deliveredrobot.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sendi.deliveredrobot.viewmodel.DateViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 *   @author: heky
 *   @date: 2021/7/7 15:04
 *   @describe: 时间变化
 */
class TimeChangeReceiver : BroadcastReceiver() {
//    lateinit var binding: ActivityMainBinding
    var dateViewModel: DateViewModel? = null

    @SuppressLint("SimpleDateFormat")
    private val sdf2 = SimpleDateFormat("HH:mm")

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_TIME_TICK -> { //每过一分钟 触发
//                with(binding.textViewTime) {
//                    val date = Date()
//                    text = sdf2.format(date)
//                }
                val date = Date()
                dateViewModel?.date?.value = sdf2.format(date)
            }

            Intent.ACTION_TIME_CHANGED -> {//设置了系统时间
            }

            Intent.ACTION_TIMEZONE_CHANGED -> {//设置了系统时区的action
            }
        }
    }
}