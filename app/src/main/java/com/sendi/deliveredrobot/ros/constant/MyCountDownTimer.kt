package com.sendi.deliveredrobot.ros.constant

import android.os.CountDownTimer

/**
 * @author swn
 * @describe 倒计时
 * @param millisInFuture 暂停时间
 * @param countDownInterval 间隔时间
 * @param onTick 倒计时中
 * @param onFinish 倒计时结束
 */
class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long, val onTick: (Long) -> Unit, val onFinish: () -> Unit) :
    CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        onTick.invoke(millisUntilFinished)
    }

    override fun onFinish() {
        onFinish.invoke()
    }
}
