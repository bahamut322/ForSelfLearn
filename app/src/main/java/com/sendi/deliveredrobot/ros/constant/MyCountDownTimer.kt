package com.sendi.deliveredrobot.ros.constant

import android.os.CountDownTimer
import com.sendi.deliveredrobot.entity.QuerySql
import com.sendi.deliveredrobot.navigationtask.BillManager
import com.sendi.deliveredrobot.navigationtask.RobotStatus.ready
import com.sendi.deliveredrobot.utils.LogUtil

class MyCountDownTimer(millisInFuture: Long, countDownInterval: Long, val onTick: (Long) -> Unit, val onFinish: () -> Unit) :
    CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        onTick.invoke(millisUntilFinished)
    }

    override fun onFinish() {
        onFinish.invoke()
    }
}
