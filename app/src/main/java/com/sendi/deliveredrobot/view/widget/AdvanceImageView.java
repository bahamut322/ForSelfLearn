package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.Universal;

import java.util.Objects;

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

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (Universal.picTypeNum == 2) {//平铺(正常图)
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            //背景颜色
            if (Objects.equals(Universal.fontBackGround, "")){
                imageView.setBackgroundColor(Color.WHITE);
            }else {
                imageView.setBackgroundColor(Color.parseColor(Universal.fontBackGround + ""));
            }
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));//设置整个布局的参数
        } else {//全图(铺满整个屏幕)
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(1080, 1920));//设置整个布局的参数
        }
        addView(imageView, layoutParams);
    }

    public void setImage(String path) {
        Glide.with(getContext()).load(path).into(imageView);


    }
}