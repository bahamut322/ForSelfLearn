package com.sendi.deliveredrobot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sendi.deliveredrobot.MainActivity
import com.sendi.deliveredrobot.utils.LogUtil

/**
 * @author lsz
 * @desc 广播 接收 开机广播 启动应用
 * @date 2021/8/20 9:38
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        LogUtil.i("boot " + intent.action)
        val autoStart = Intent(context, MainActivity::class.java)
        autoStart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(autoStart)
    }
}