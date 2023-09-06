package com.sendi.deliveredrobot.view.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.R;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.warnyul.android.widget.FastVideoView;

import java.io.File;

public class AdvanceVideoView extends RelativeLayout {
    private ImageView imageView;
    private FastVideoView videoView;
    private RelativeLayout videoRela;
    private String path1;
    private MediaPlayer mediaPlayer1;

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
        videoRela.setBackgroundResource(R.color.white);
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
        // 添加加载动画
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        LayoutParams progressParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoRela.addView(progressBar, progressParams);

        videoView = new FastVideoView(getContext());
        videoView.setVideoPath(path1);
        if (Universal.videolayout == 1) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(path1);
                String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                int videoWidth = Integer.parseInt(width);
                int videoHeight = Integer.parseInt(height);
                videoView.setScaleX(1080 / videoWidth);
                videoView.setScaleY(1920 / videoHeight);
            }catch (Exception ignored){}
        }

        LayoutParams layoutParams = new LayoutParams(-1, -1);
        //设置videoview居中父view播放等比例缩放
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoView.setLayoutParams(layoutParams);
        videoRela.addView(videoView, layoutParams);
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.start();
        mediaPlayer1 = new MediaPlayer();
        videoView.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer1 = mediaPlayer;
            if (Universal.AllvideoAudio == 1) {
                new AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().getVideoVolume());
                mediaPlayer.setVolume(1, 1);
            } else {
                mediaPlayer.setVolume(0, 0);
            }
            new Handler().postDelayed(() -> imageView.setVisibility(GONE), 400);//防止video view播放视频前有个闪烁的黑屏
            Log.d("TAG", "setVideo: " + getFileCount(Universal.advertisement));
            progressBar.setVisibility(View.GONE);
            if (getFileCount(Universal.advertisement) <= 1) {
                mediaPlayer.setLooping(true);
            }
        });
    }

    public void mediaStop() {
        if (videoView != null && videoView.isPlaying()) {
            Log.d("TAG", "setVideo: 静音");
            mediaPlayer1.setVolume(0, 0);
        }
    }

    public void mediaRestart() {
        if (videoView != null && videoView.isPlaying()) {
            Log.d("TAG", "setVideo: 调节声音大小");
            new AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().getVideoVolume());
            mediaPlayer1.setVolume(1.0f, 1.0f);
        }
    }

    public void setPause() {
        if (videoView != null) {
            videoView.pause();
            imageView.setVisibility(VISIBLE);
        }
    }

    public static int getFileCount(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += getFileCount(file.getAbsolutePath());
                }
            }
        }
        return count;
    }

    public void setRestart() {
        if (videoView != null) {
            videoView.start();
            imageView.setVisibility(GONE);
        }
    }
}