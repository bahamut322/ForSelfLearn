package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;


public class AdvanceImageView extends RelativeLayout {
    public ImageView imageView;
    public ProgressBar progressBar;
    public int viewType;

    public AdvanceImageView(Context context,int viewType) {
        super(context);
        this.viewType = viewType;
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
        imageView = new ImageView(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setLayoutParams(layoutParams);
        // 设置背景颜色
        if (TextUtils.isEmpty(QuerySql.ADV().getFontBackGround())) {
            imageView.setBackgroundColor(Color.WHITE);
        } else {
            imageView.setBackgroundColor(Color.parseColor(QuerySql.ADV().getFontBackGround()));
        }
        if (viewType == 2) {
            //按比例缩放图片，使图片完整地显示在ImageView中心，如果图片比ImageView小，则不会放大
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            //按比例缩放图片，使图片充满整个ImageView，可能会裁剪图片
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        addView(imageView);

        progressBar = new ProgressBar(getContext());
        RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(progressBarParams);
        addView(progressBar);
    }

    public void setImage(String path) {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(getContext())
                .load(path)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }
}