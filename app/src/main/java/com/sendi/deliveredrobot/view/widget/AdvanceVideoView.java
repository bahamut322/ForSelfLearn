package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.warnyul.android.widget.FastVideoView;

public class AdvanceVideoView extends RelativeLayout {
    private ImageView imageView;
    private FastVideoView videoView;
    private RelativeLayout videoRela;
    private String path1;

    public AdvanceVideoView(Context context) {
        super(context);
        initView();
    }

    public AdvanceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AdvanceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        videoRela = new RelativeLayout(getContext());
        addView(videoRela, new LayoutParams(-1, -1));
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(imageView, new LayoutParams(-1, -1));
    }

    public void setImage(String path) {
        this.path1= path;
        Glide.with(getContext()).load(path).into(imageView);
    }

    public void setVideo(MediaPlayer.OnCompletionListener onCompletionListener) {
        if (videoView != null) {
            videoRela.removeView(videoView);
            videoView = null;
        }
        videoView = new FastVideoView(getContext());
        videoView.setVideoPath(path1);
        //        videoView.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //设置videoview居中父view播放等比例缩放
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoView.setLayoutParams(layoutParams);
        videoRela.addView(videoView, layoutParams);
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.start();
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.setLooping(true);
            new Handler().postDelayed(() -> {
                imageView.setVisibility(GONE);
            }, 400);//防止videoview播放视频前有个闪烁的黑屏
        });
    }

    public void setPause() {
        if (videoView != null) {
            videoView.pause();
            imageView.setVisibility(VISIBLE);
        }
    }

    public void setRestart() {
        if (videoView != null) {
            videoView.start();
            imageView.setVisibility(GONE);
        }
    }
}