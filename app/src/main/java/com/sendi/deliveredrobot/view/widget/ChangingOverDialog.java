package com.sendi.deliveredrobot.view.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.service.UpdateReturn;


public class ChangingOverDialog extends Dialog {

    public Context context;
    public GridView pointGV;
    public ImageView returnImg;
    public ConstraintLayout dialog_button;
    public TextView askTv;
    public Button Sure, No;

    public ChangingOverDialog(Context context) {
        super(context, R.style.Dialog);
        this.context = context;
    }

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_changing_over);
        pointGV = findViewById(R.id.pointGV);
        returnImg = findViewById(R.id.returnImg);
        dialog_button = findViewById(R.id.dialog_button);
        askTv = findViewById(R.id.askTv);
        Sure = findViewById(R.id.Yes_Exit);
        No = findViewById(R.id.No_Exit);
        dialog_button.setVisibility(View.GONE);
    }

    @Override
    public void dismiss() {
        Log.d("TAG", "dismiss: dialog");
        Universal.Changing = false;
        Universal.explainUnSpeak = false;
    super.dismiss();
    }

    /**
     * 调用弹窗全屏显示方法
     */
    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        Log.d("TAG", "show: dialog");
        Universal.Changing = true;
        dialog_button.setVisibility(View.GONE);
        new UpdateReturn().pause();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);//设置全屏
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }

    @Override
    protected void onStop() {
        super.onStop();
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

