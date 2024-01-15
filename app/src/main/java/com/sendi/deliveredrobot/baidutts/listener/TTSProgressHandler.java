package com.sendi.deliveredrobot.baidutts.listener;

/**
 * @Author Swn
 * @Data 2024/1/10
 * @describe 长文字朗读接口
 */
public interface TTSProgressHandler {
    void handleProgressUpdate(String utteranceId, int progress);
}

