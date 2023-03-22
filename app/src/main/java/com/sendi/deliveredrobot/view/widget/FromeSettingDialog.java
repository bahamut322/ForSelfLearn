package com.sendi.deliveredrobot.view.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.LogUtil;

import java.util.Objects;


public class FromeSettingDialog extends Dialog {

    public Context context;
    public Button YesExit;
    public Button NoExit;
    public TextView errorTv;
    public VerificationCodeView passwordEt;

    public FromeSettingDialog(Context context) {
        super(context, R.style.Dialog);
        this.context = context;
//        init(context);
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fromsetting);

        NoExit = findViewById(R.id.cancelBtn);
        YesExit = findViewById(R.id.submitBtn);
        passwordEt = findViewById(R.id.verificationCodeView);
        errorTv = findViewById(R.id.errorhint);

        YesExit.setOnClickListener(v -> {
            if (Objects.equals(passwordEt.getContent(), Universal.password)){
                cancel();
                dismiss();
                RobotStatus.INSTANCE.getPassWordToSetting().postValue(true);
            }else {
                passwordEt.clear();
                errorTv.setVisibility(View.VISIBLE);
            }
        });
        NoExit.setOnClickListener(v -> {
            passwordEt.clear();
            errorTv.setVisibility(View.GONE);
            dismiss();
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            RobotStatus.INSTANCE.getOnTouch().postValue(true);
            return false;
        }else {
            RobotStatus.INSTANCE.getOnTouch().postValue(false);
        }
        return super.onTouchEvent(event);
    }


    /**
     * 调用弹窗全屏显示方法
     */
    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
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
