package com.sendi.deliveredrobot.baidutts.listener;

import android.util.Log;

import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.LogUtil;

/**
 * @Author Swn
 * @Data 2024/1/10
 * @describe 长文字朗读算字
 */
public class TTSProgressHandlerImpl implements TTSProgressHandler {
    private static final String TAG = "TTSProgressHandlerImpl";
    private int previousProgress = 0;

    @Override
    public void handleProgressUpdate(String utteranceId, int progress) {
        if (utteranceId.equals(Universal.speakTextId)) {
            //首先规避一下重复数
            if (progress == previousProgress) {
                LogUtil.INSTANCE.d("生成了重复数");
            } else if (Universal.ExplainSpeak.size() != 0) {
                //当TTS的播放进度和列表第一项相等的时候
                if (progress == Universal.ExplainSpeak.get(0)) {
                    Universal.taskNum += Universal.ExplainSpeak.get(0);
                    Log.d(TAG, "当前朗读完的item的总算" + Universal.taskNum);
                    RobotStatus.INSTANCE.getProgress().postValue(Universal.taskNum);
                    Universal.ExplainSpeak.remove(0);
                } else {
                    if (progress != Universal.ExplainSpeak.get(0)) {
                        Log.d(TAG, "当前朗读完的item的总算" + Universal.taskNum);
                        RobotStatus.INSTANCE.getProgress().postValue(Universal.taskNum + progress);
                    }
                }
            }
            LogUtil.INSTANCE.d(" BaiduTTS播放进度：" + progress
                    + "播放总进度：" + RobotStatus.INSTANCE.getProgress().getValue()
                    + " 当前朗读完的item的总算：" + Universal.taskNum
                    + " 播放目标进度：" + Universal.ExplainLength
            );
            previousProgress = progress; // 更新前一次的 progress，避重
        }
    }
}
