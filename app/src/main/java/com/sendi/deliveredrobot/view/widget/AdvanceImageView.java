package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
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
        Log.d("TAG", "initView图片: " + Universal.pic);
        if (Universal.pic == 2) {//平铺(正常图)
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));//设置整个布局的参数
            //背景颜色
            if (Objects.equals(QuerySql.ADV().getFontBackGround(), "")) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setBackgroundColor(Color.WHITE);
            } else {
                if (QuerySql.ADV().getFontBackGround() == null) {
                    imageView.setBackgroundColor(Color.WHITE);
                } else {
                    imageView.setBackgroundColor(Color.parseColor(QuerySql.ADV().getFontBackGround() + ""));
                }
            }
        } else {//全图(铺满整个屏幕)
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//            imageView.setLayoutParams(new RelativeLayout.LayoutParams(1080, 1920));//设置整个布局的参数
        }

        addView(imageView, layoutParams);
    }

    public void setImage(String path) {
        //tmd，我也不知道为啥有时候显示不全，又不想算这个傻逼副屏，只好多个判断
        if (Universal.pic == 2) {
            Glide.with(getContext()).load(path).into(imageView);
        } else {
            Glide.with(getContext().getApplicationContext())
                    .setDefaultRequestOptions(
                            new RequestOptions()
//                                    .frame(3000000)
                                    .centerCrop()
                                    .error(R.drawable.ic_loading_2170e7)
                                    .placeholder(R.drawable.qmui_icon_notify_error)
//                                    .override(1920, 1080)//设置图片宽高
                    )
                    .load(path)
                    .into(imageView);
        }
    }
}