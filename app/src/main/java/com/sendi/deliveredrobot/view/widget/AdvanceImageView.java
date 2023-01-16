package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.entity.Universal;

public class AdvanceImageView extends RelativeLayout {
    public ImageView imageView;


    public AdvanceImageView(Context context) {
        super(context);
        initView();
    }

    public AdvanceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AdvanceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    public void initView() {
        if (imageView == null) {
            imageView = new ImageView(getContext());
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        /*
         *设置Imageview占满父view播放
         * 铺满：imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         * 平铺：layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
         *      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
         */
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if (Universal.picTypeNum == 2) {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));//设置整个布局的参数
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setBackgroundColor(Color.WHITE);
        } else {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(1080, 1920));//设置整个布局的参数
        }
        addView(imageView, layoutParams);
    }

    public void setImage(String path) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        Glide.with(getContext()).load(path).into(imageView);
                        break;
                }
            }
        };
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }
}