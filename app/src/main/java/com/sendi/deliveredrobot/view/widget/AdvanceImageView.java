package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.entity.Universal;

public class AdvanceImageView extends RelativeLayout {
    public  ImageView imageView;


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


    public  void initView() {
        imageView = new ImageView(getContext());

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        /*
         *设置Imageview占满父view播放
         * 铺满：imageView.setScaleType(ImageView.ScaleType.FIT_XY);
         * 平铺：layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
         *      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
         */
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if (Universal.picTypeNum==0){
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }else {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        addView(imageView,layoutParams);
    }

    public void setImage(String path) {
        Glide.with(getContext()).load(path).into(imageView);
    }

}