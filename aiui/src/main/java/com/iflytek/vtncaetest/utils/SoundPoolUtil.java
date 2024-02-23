package com.iflytek.vtncaetest.utils;

import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.iflytek.vtncaetest.ContextHolder;

import java.io.IOException;
import java.util.HashMap;


public class SoundPoolUtil {
    private static final String TAG = SoundPoolUtil.class.getSimpleName();
    private static SoundPool soundPool;
    private static HashMap<String, Integer> soundPoolMap = new HashMap();

    public static void create() {
        if (soundPool != null) {
            return;
        }

        /**
         * 初始化声音池
         * 参数1：最大同时播放音频数量，设置太大浪费CPU资源。
         * 参数2：流类型 默认使用AudioManager.STREAM_MUSIC
         * 参数3:质量  默认为0 这个值暂时没影响
         */
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        //加载音频到内存
        try {
            AssetManager am = ContextHolder.getContext().getAssets();
            soundPoolMap.put("唤醒提示音", soundPool.load(am.openFd("audio/wakeResponse.mp3"), 1));
            soundPoolMap.put("未联网提示音", soundPool.load(am.openFd("audio/offline.mp3"), 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //资源加载完成回调
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> Log.i(TAG, "音频加载完毕，id=" + sampleId));
    }

    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public static void play(String audioName) {
        if (soundPool == null) {
            Log.e(TAG, "soundPool未初始化");
            return;
        }
        /**
         * 参数1：加载返回的声音Id
         * leftVolume：左声道音量，0.0-1.0f
         * rightVolume：右声道音量，0.0-1.0f
         * priority：优先级
         * loop：循环播放:  0(不循环)   -1(循环)
         * rate：播放速率  0.5--2.0f
         */
        soundPool.play(soundPoolMap.get(audioName), 1.0f, 1.0f, 1, 0, 1.0f);
    }

}
