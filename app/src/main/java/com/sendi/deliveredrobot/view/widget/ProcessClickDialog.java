package com.sendi.deliveredrobot.view.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.navigationtask.BillManager;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.service.UpdateReturn;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.Objects;

import in.xiandan.countdowntimer.CountDownTimerSupport;
import in.xiandan.countdowntimer.OnCountDownTimerListener;

public class ProcessClickDialog extends Dialog {

    public Context context;

    private CountDownTimerSupport mTimer;
    public TextView returnTv;
    public Button continueBtn, finishBtn, nextBtn, otherBtn;
    private int countdownTime; // 成员变量来存储时间值

    public ProcessClickDialog(Context context) {
        super(context, R.style.Dialog);
        this.context = context;
    }
    public void setCountdownTime(int time) {
        this.countdownTime = time;
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
        TaskArray.setOnChangeListener(() -> {
            if (Objects.equals(TaskArray.getToDo(), "3")) {
               mTimer.pause();
            }
            if (Objects.equals(TaskArray.getToDo(), "5")){
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

    private void startCountdown( int time ) {
        mTimer = new CountDownTimerSupport(time * 1000L, 1000);
        // SimpleOnCountDownTimerListener
        mTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                // 倒计时间隔
                returnTv.setText(String.valueOf((int) (millisUntilFinished / 1000)));
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



    @Override
    protected void onStop() {
        Universal.Process = false;
        if (mTimer!=null) {
            mTimer.stop();
        }
        super.onStop();
    }

    @Override
    public void dismiss() {
        Universal.Process = false;
        if (mTimer!=null) {
            mTimer.stop();
        }
        super.dismiss();
    }


    /**
     * 调用弹窗全屏显示方法
     */
    @Override
    public void show( ) {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        Universal.Process = true;
        MediaPlayerHelper.getInstance().pause();
        BaiduTTSHelper.getInstance().pause();
        startCountdown(countdownTime); // 使用成员变量中的时间值
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
