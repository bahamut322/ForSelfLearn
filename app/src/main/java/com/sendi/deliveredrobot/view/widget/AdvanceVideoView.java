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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.BaiduTTSHelper;
import com.sendi.deliveredrobot.baidutts.listener.MessageListener;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.room.entity.BasicConfig;
import com.warnyul.android.widget.FastVideoView;

import java.util.Objects;

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
        this.path1 = path;
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
            //因为我不会在工具类中用观察者，所以定义了一个接口观察变量变化
            Order.setOnChangeListener(() -> {
                if (Objects.equals(Order.getFlage(), "1")) {
                    mediaPlayer.setVolume(0, 0);
                } else if (Objects.equals(Order.getFlage(), "0")) {
                    //恢复成视频播放声音大小
                    new AudioMngHelper(MyApplication.Companion.getInstance()).setVoice100((int) QuerySql.QueryBasic().getVideoVolume());
                    mediaPlayer.setVolume(1, 1);
                }
            });
            mediaPlayer.setLooping(true);
            new Handler().postDelayed(() -> imageView.setVisibility(GONE), 400);//防止videoview播放视频前有个闪烁的黑屏
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