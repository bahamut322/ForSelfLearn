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
class MyCountDownTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    val onTick: (Long) -> Unit,
    val onFinish: () -> Unit
) : CountDownTimer(millisInFuture, countDownInterval) {

    private var isPaused = false
    private var isFinished = false
    private var isStarted = false

    override fun onTick(millisUntilFinished: Long) {
        if (!isPaused) {
            onTick.invoke(millisUntilFinished)
        }
    }

    override fun onFinish() {
        if (!isPaused) {
            isFinished = true
            onFinish.invoke()
        }
    }

    fun startCountDown() {
        isStarted = true
        start()
    }

    fun pause() {
        if (isStarted) {
            cancel()
            isPaused = true
        }
    }

    fun resume() {
        // todo 有问题 是否应该判断是否已经暂停
        if (isStarted) {
            isPaused = false
            start()
        }
    }

    fun manuallyFinish() {
        if (isStarted && !isFinished) {
            cancel()
            onFinish.invoke()
        }
    }
}
