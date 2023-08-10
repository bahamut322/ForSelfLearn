package com.sendi.deliveredrobot.navigationtask

import android.os.SystemClock
import com.sendi.deliveredrobot.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author Swn
 * @Data 2023-07-27
 * @describe
 */
suspend fun Vire (second: Long = 5, title: String = "") {
    LogUtil.i("${title}开始延时${second}秒")
    withContext(Dispatchers.IO) {
        SystemClock.sleep(second * 1000)
    }
    LogUtil.i("${title}结束延时")
}

suspend fun VireFloat(second: Float = 5f, title: String = "") {
    LogUtil.i("${title}开始延时${second}秒")
    withContext(Dispatchers.IO) {
        SystemClock.sleep((second * 1000).toLong())
    }
//    virtualTaskExecute((second).toLong())
    LogUtil.i("${title}结束延时")
}