package com.sendi.deliveredrobot.helpers;

import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;


import android.os.Looper;

/**
 * @author swn
 * @describe 单列模式的MP3播放
 * start、pause、stop、isPlaying、getCurrentPosition 和 getDuration 方法来控制播放状态和获取播放进度信息。
 * OnMusicProgressListener 接口来监听播放进度的变化。
 */
public class MP3Player {
    private static MP3Player sInstance;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private Runnable mRunnable;
    private OnMusicProgressListener mProgressListener;

    private MP3Player() {
        mMediaPlayer = new MediaPlayer();
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mProgressListener != null) {
                    mProgressListener.onProgress(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
                }
                mHandler.postDelayed(this, 1000);
            }
        };
    }

    public static MP3Player getInstance() {
        if (sInstance == null) {
            synchronized (MP3Player.class) {
                if (sInstance == null) {
                    sInstance = new MP3Player();
                }
            }
        }
        return sInstance;
    }

    public void start(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.start();
            mHandler.post(mRunnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        mMediaPlayer.pause();
        mHandler.removeCallbacks(mRunnable);
    }

    public void stop() {
        mMediaPlayer.stop();
        mHandler.removeCallbacks(mRunnable);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void setOnMusicProgressListener(OnMusicProgressListener listener) {
        mProgressListener = listener;
    }

    public interface OnMusicProgressListener {
        void onProgress(int current, int duration);
    }
}
