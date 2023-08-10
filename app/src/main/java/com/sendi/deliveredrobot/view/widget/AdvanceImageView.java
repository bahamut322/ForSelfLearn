package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;

import java.util.Objects;

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
        if (Universal.pic == 2) {
            //按比例缩放图片，使图片完整地显示在ImageView中心，如果图片比ImageView小，则不会放大
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        }else {
            //按比例缩放图片，使图片充满整个ImageView，可能会裁剪图片
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        addView(imageView);
    }

    public void setImage(String path) {
        Glide.with(getContext()).load(path).into(imageView);
    }
}