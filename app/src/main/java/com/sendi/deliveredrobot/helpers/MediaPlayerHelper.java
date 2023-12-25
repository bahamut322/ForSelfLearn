package com.sendi.deliveredrobot.helpers;

import android.media.MediaPlayer;
import android.os.Handler;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.view.widget.Order;

import java.io.IOException;

/**
 * @author swn
 * @describe 音屏播放类
 */
public class MediaPlayerHelper {

    private static MediaPlayerHelper instance;
    private MediaPlayer mMediaPlayer;
    private boolean isPaused = false;
    private int currentPosition = 0;
    private Handler mHandler;
    private OnProgressListener mOnProgressListener;

    private MediaPlayerHelper() {
        // 私有构造函数，防止外部实例化
    }

    public static MediaPlayerHelper getInstance() {
        if (instance == null) {
            instance = new MediaPlayerHelper();
        }
        return instance;
    }

    public void play(String fileName, String FLage) {
        new Handler().postDelayed(() -> {
            // 要延迟执行的方法
            Order.setFlage(FLage);
            releaseMediaPlayer(); // 释放之前的 MediaPlayer
            mMediaPlayer = new MediaPlayer();
            try {
                if (fileName != null) {
                    new AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().getVideoVolume());
                    mMediaPlayer.setDataSource(fileName);
                    mMediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        startProgressUpdate();
                    });
                    mMediaPlayer.prepareAsync();
                }
            } catch (IOException e) {
                e.printStackTrace();
                releaseMediaPlayer(); // 出现异常时释放 MediaPlayer
            }
        }, 2000); // 延迟2秒执行
    }

    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPaused = true;
            currentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resume() {
        if (mMediaPlayer != null && isPaused) {
            mMediaPlayer.seekTo(currentPosition);
            mMediaPlayer.start();
            isPaused = false;
            Order.setFlage("1");
        }
    }

    public void stop() {
        releaseMediaPlayer();
        stopProgressUpdate();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    private void startProgressUpdate() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(mProgressUpdateRunnable, 500);
    }

    private void stopProgressUpdate() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mProgressUpdateRunnable);
            mHandler = null;
        }
    }

    private final Runnable mProgressUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mOnProgressListener != null) {
                mOnProgressListener.onProgress(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
                try {
                    if (mMediaPlayer.getCurrentPosition() >= mMediaPlayer.getDuration()) {
                        stopProgressUpdate();
                        Order.setFlage("0");
                    }
                } catch (Exception ignored) {
                }
            }
            try {
                if (mHandler != null) {
                    mHandler.postDelayed(this, 500);
                }
            } catch (Exception ignored) {
            }
        }
    };

    public interface OnProgressListener {
        void onProgress(int currentPosition, int totalDuration);
    }
}