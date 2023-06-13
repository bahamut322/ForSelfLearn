package com.sendi.deliveredrobot.baidutts.listener;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.sendi.deliveredrobot.BuildConfig;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.MainHandlerConstant;
import com.sendi.deliveredrobot.entity.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.navigationtask.TaskQueues;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Order;

import java.util.Objects;

/**
 * SpeechSynthesizerListener 简单地实现，仅仅记录日志
 * Created by fujiayi on 2017/5/19.
 */

public class MessageListener implements SpeechSynthesizerListener, MainHandlerConstant {
    private static final String TAG = "MessageListener";

    private int progressSpeak;

    /**
     * 播放开始，每句播放开始都会回调
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeStart(String utteranceId) {
        sendMessage("准备开始合成,序列号:" + utteranceId);
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param bytes       二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法和合成到第几个字对应。
     *                    engineType 下版本提供。1:音频数据由离线引擎合成； 0：音频数据由在线引擎（百度服务器）合成。
     */

    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress) {
        Log.i(TAG, "合成进度回调, progress：" + progress + ";序列号:" + utteranceId);
    }

    @Override
    // engineType 下版本提供。1:音频数据由离线引擎合成； 0：音频数据由在线引擎（百度服务器）合成。
    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress, int engineType) {
        onSynthesizeDataArrived(utteranceId, bytes, progress);
    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        sendMessage("合成结束回调, 序列号:" + utteranceId);
    }

    @Override
    public void onSpeechStart(String utteranceId) {
        sendMessage("播放开始回调, 序列号:" + utteranceId);
    }

    /**
     * 播放进度回调接口，分多次回调
     *
     * @param utteranceId
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    private int previousProgress = 0; // 存储前一次的 progress

    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        new AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().getVoiceVolume());//设置语音音量
        if (previousProgress == Universal.BaiduSpeakLength && progress == Universal.BaiduSpeakLength) {
            LogUtil.INSTANCE.i("连续生成两次45");
        } else {
            if (Universal.taskNum != 0 && progress == Universal.BaiduSpeakLength) {
                Universal.progress++;
                RobotStatus.INSTANCE.getProgress().postValue(Universal.progress * Universal.BaiduSpeakLength);
            } else if (Universal.progress <= Universal.taskNum - 1 && progress != Universal.BaiduSpeakLength) {
                RobotStatus.INSTANCE.getProgress().postValue(Universal.progress * Universal.BaiduSpeakLength + progress);
            }
        }
        previousProgress = progress; // 更新前一次的 progress
        Log.i(TAG, "播放进度1, Universal.progress：" + Universal.progress + ";Universal.taskNum:" + Universal.taskNum);
        Log.i(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId);
        if (utteranceId.equals("explantion")) {
            progressSpeak = progress;
            Log.e(TAG, "onSpeechProgressChanged: " + progressSpeak);
        }
    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        sendMessage("播放结束回调, 序列号:" + utteranceId);
        Order.setFlage("0");
        Log.d(TAG, "onSpeechFinish: 百度语音Finish");
        if (Universal.taskNum != 0 && Universal.progress == Universal.taskNum) {
            MediaPlayerHelper.resume();
        } else if (Universal.taskNum == 0) {
            MediaPlayerHelper.resume();
        }
        //观察utteranceId为0的语音是否朗读完毕，之后继续朗读其他语音
        if (utteranceId.equals("0")) {
            RobotStatus.INSTANCE.getIdentifyFace().postValue(1);
            if (progressSpeak != RobotStatus.INSTANCE.getSpeakNumber().getValue().length()) {
                RobotStatus.INSTANCE.getSpeakNumber().postValue(RobotStatus.INSTANCE.getSpeakNumber().getValue().substring(progressSpeak));
                RobotStatus.INSTANCE.getSpeakContinue().postValue(1);
            } else if (utteranceId.equals("explantion")) {
                RobotStatus.INSTANCE.getSpeakContinue().postValue(3);
            }
        }
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        sendErrorMessage("错误发生：" + speechError.description + "，错误编码："
                + speechError.code + "，序列号:" + utteranceId);
    }

    private void sendErrorMessage(String message) {
        sendMessage(message, true);
    }


    private void sendMessage(String message) {
        sendMessage(message, false);
    }

    protected void sendMessage(String message, boolean isError) {
        if (isError) {
            Log.e(TAG, message);
        } else {
            Log.i(TAG, message);
        }

    }
}
