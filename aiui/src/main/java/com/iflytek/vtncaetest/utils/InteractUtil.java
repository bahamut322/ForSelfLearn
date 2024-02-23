package com.iflytek.vtncaetest.utils;

import android.util.Log;
import android.widget.Button;

import com.iflytek.vtncaetest.engine.AiuiEngine;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 唤醒后计时器
 */
public class InteractUtil {
    private static final String TAG = InteractUtil.class.getSimpleName();

    private static Timer timer = null;         //定时器对象
    private static TimerTask timerTask = null; //执行定时器的线程（要调度的任务）
    private static int TimeInterval = 1000;      //定时器执行时间间隔,单位ms
    private static int TimeTotal;              //定时器倒计时总时间,单位ms
    private static int interactTime = 10000;     //唤醒后持续交互的时间,单位ms

    /**
     * @param btnStopRecord demo界面中显示剩余时间，实际项目可去掉，直接调用startTimerSimple()
     */
    public static void startTimer(Button btnStopRecord) {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            TimeTotal = interactTime;
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    TimeTotal = TimeTotal - TimeInterval;  //计时器，定时减少剩余时间
                    if (TimeTotal > 0) {
                        Log.i(TAG, "当前时间=" + TimeTotal);
                        btnStopRecord.setText("可交互状态，剩余" + TimeTotal + "ms");
                    } else {
                        Log.i(TAG, "到时间了=" + TimeTotal);
                        btnStopRecord.setText("不可交互状态");
                        timer.cancel();    //到时间后销毁对象
                        timer.purge();
                        timer = null;
                        timerTask = null;
                        //到时间后不再识别,进入待唤醒状态
                        AiuiEngine.MSG_reset_wakeup();
                    }
                }
            };
            timer.schedule(timerTask, 0, TimeInterval);
        } else {
            //重复唤醒会重置时间
            resetTimer();
        }
    }

    public static void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            TimeTotal = interactTime;
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    TimeTotal = TimeTotal - TimeInterval;  //计时器，定时减少剩余时间
                    if (TimeTotal > 0) {
                        Log.i(TAG, "当前时间=" + TimeTotal);
                    } else {
                        Log.i(TAG, "到时间了=" + TimeTotal);
                        timer.cancel();    //到时间后销毁对象
                        timer.purge();
                        timer = null;
                        timerTask = null;
                        //到时间后不再识别,进入待唤醒状态
                        AiuiEngine.MSG_reset_wakeup();
                    }

                }
            };
            timer.schedule(timerTask, 0, TimeInterval);
        } else {
            //重复唤醒会重置时间
            resetTimer();
        }
    }

    public static void resetTimer() {
        TimeTotal = interactTime;
        Log.i(TAG, "重置交互持续时间");
    }
}
