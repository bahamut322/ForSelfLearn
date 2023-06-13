package com.sendi.deliveredrobot.helpers;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.view.widget.Order;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author swn
 * @describe 音屏播放类
 */
public class MediaPlayerHelper {

    private static MediaPlayer mMediaPlayer;
    private static boolean isPaused = false;
    private static int currentPosition = 0;
    private static Timer mTimer;
    private static OnProgressListener mOnProgressListener;
    /**
     *
     * @param fileName 文件路径
     * @param FLage 是否播放视频声音 "1"：不 "0"：播放
     */
    public static void play(String fileName,String FLage) {

        new Handler().postDelayed(() -> {
            // 要延迟执行的方法
            Order.setFlage(FLage);
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            } else {
                mMediaPlayer.reset();
            }
            new AudioMngHelper(MyApplication.context).setVoice100((int) QuerySql.QueryBasic().getVideoVolume());//设置语音音量
            try {
                if(fileName!=null) {
                    mMediaPlayer.setDataSource(fileName);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    startTimer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 2000); // 延迟2秒执行
    }

    public static void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPaused = true;
            currentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public static void resume() {
        if (mMediaPlayer != null && isPaused) {
            mMediaPlayer.seekTo(currentPosition);
            mMediaPlayer.start();
            isPaused = false;
            new Handler().postDelayed(() -> {
                // 要延迟执行的方法
                Order.setFlage("1");
            }, 2000); // 延迟2秒执行

        }
//        if (mMediaPlayer == null && !isPaused){
//            Order.setFlage("0");
//        }
    }

    public static void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            stopTimer();
        }
    }

    public static void setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    private static void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer != null && mOnProgressListener != null) {
                    mOnProgressListener.onProgress(mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
                    if (mMediaPlayer.getCurrentPosition() >= mMediaPlayer.getDuration()){
                        //恢复成视频播放声音大小
                        stopTimer();
                        Order.setFlage("0");
                    }
                }
            }
        }, 0, 1000);
    }

    private static void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public interface OnProgressListener {
        void onProgress(int currentPosition, int totalDuration);
    }
}




