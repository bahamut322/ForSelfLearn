package com.sendi.deliveredrobot.view.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.Objects;

import in.xiandan.countdowntimer.CountDownTimerSupport;
import in.xiandan.countdowntimer.OnCountDownTimerListener;

public class ProcessClickDialog extends Dialog {

    public Context context;
    //    private CountDownTimer countDownTimer;
    private CountDownTimerSupport mTimer;
    public TextView returnTv;
    public Button continueBtn, finishBtn, nextBtn, otherBtn;

    //    private boolean isCountdownRunning = false;
    public ProcessClickDialog(Context context) {
        super(context, R.style.Dialog);
        this.context = context;
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_process_click);
        returnTv = findViewById(R.id.returnTv);
        continueBtn = findViewById(R.id.continueBtn);
        finishBtn = findViewById(R.id.finishBtn);
        nextBtn = findViewById(R.id.nextBtn);
        Universal.speakIng =true;
        TaskNext.setOnChangeListener(() -> {
            if (Objects.equals(TaskNext.getToDo(), "3")) {
               mTimer.pause();
            }
            if (Objects.equals(TaskNext.getToDo(), "5")){
                mTimer.resume();
            }
        });

        if (BillManager.INSTANCE.billList().size() == 1) {
            nextBtn.setBackgroundResource(R.drawable.bg_button_1);
            nextBtn.setEnabled(false);
        }
        otherBtn = findViewById(R.id.otherBtn);
        continueBtn.setOnClickListener(view -> {
            new UpdateReturn().resume();
            dismiss();
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            RobotStatus.INSTANCE.getOnTouch().postValue(true);
            return false;
        } else {
            RobotStatus.INSTANCE.getOnTouch().postValue(false);
        }
        return super.onTouchEvent(event);
    }

    private void startCountdown() {
        mTimer = new CountDownTimerSupport(QuerySql.QueryBasic().getWhetherTime() * 1000L, 1000);
        // SimpleOnCountDownTimerListener
        mTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                // 倒计时间隔
                returnTv.setText((int) (millisUntilFinished / 1000)+"");
            }

            @Override
            public void onFinish() {
                // 倒计时结束
                LogUtil.INSTANCE.i(this+"倒计时结束");
                    dismiss();
                    new UpdateReturn().resume();
            }

            @Override
            public void onCancel() {
                // 倒计时手动停止
                LogUtil.INSTANCE.e("dialog倒计时手动停止");
            }
        });
    }

//    private void startCountdown() {
//        if (!isCountdownRunning) {
//            countDownTimer = new CountDownTimer(QuerySql.QueryBasic().getWhetherTime()* 1000L, 1000) {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    returnTv.setText((int) (millisUntilFinished / 1000)+"");
//                }
//                @Override
//                public void onFinish() {
//                    LogUtil.INSTANCE.i(this+"倒计时结束");
//                    dismiss();
//                    new UpdateReturn().resume();
//                }
//            }.start();
//            isCountdownRunning = true;
//        }
//    }


    @Override
    protected void onStop() {
//        stopCountdown();
//        isCountdownRunning = false;
        Universal.speakIng =false;
        if (mTimer!=null) {
            mTimer.stop();
        }
        super.onStop();
    }

    @Override
    public void dismiss() {
//        stopCountdown();
//        isCountdownRunning = false;
        Universal.speakIng =false;
        if (mTimer!=null) {
            mTimer.stop();
        }
        super.dismiss();
    }

//    private void stopCountdown() {
//        if (isCountdownRunning) {
//            countDownTimer.cancel();
//            LogUtil.INSTANCE.i(this+"倒计时停止");
//            isCountdownRunning = false;
//        }
//    }

    /**
     * 调用弹窗全屏显示方法
     */
    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        startCountdown();
        mTimer.start();
        new UpdateReturn().pause();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);//设置全屏
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * 弹窗弹出时全屏显示
     */
    private void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }
}
