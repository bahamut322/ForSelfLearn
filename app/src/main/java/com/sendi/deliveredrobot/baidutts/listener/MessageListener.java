package com.sendi.deliveredrobot.baidutts.listener;

import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.MainHandlerConstant;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.entity.Universal;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.helpers.SpeakHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.view.widget.MediaStatusManager;

/**
 * SpeechSynthesizerListener 简单地实现，仅仅记录日志
 * Created by fujiayi on 2017/5/19.
 */

public class MessageListener implements SpeechSynthesizerListener, MainHandlerConstant {
    private static final String TAG = "MessageListener";
    TTSProgressHandler progressHandler = new TTSProgressHandlerImpl();
    private static int progressSpeak = -1;

    private SpeakHelper.SpeakCallback speakCallback;

    public MessageListener(SpeakHelper.SpeakCallback speakCallback){
        this.speakCallback = speakCallback;
    }

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
        new AudioMngHelper(MyApplication.context).setVoice100(QuerySql.QueryBasic().getVoiceVolume());//设置语音音量
        if (utteranceId == Universal.speakTextId) {
            return;
        }
        RobotStatus.INSTANCE.getIdentifyFaceSpeak().postValue(0);
        RobotStatus.INSTANCE.setTtsIsPlaying(true);
        MediaStatusManager.stopMediaPlay(true);
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

        //耗时计算的方法，丢到子线程去做咯，面的卡线程
        progressHandler.handleProgressUpdate(utteranceId, progress);

        Log.d(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId);

        if (speakCallback != null) {
            speakCallback.progressChange(utteranceId, progress);
        }

    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        if (speakCallback != null) {
            speakCallback.speakFinish(utteranceId);
        }
//        if(!Objects.equals(utteranceId, "explanation")){
//            RobotStatus.INSTANCE.setTtsIsPlaying(false);
//        }
        sendMessage("播放结束回调, 序列号:" + utteranceId);
//        if (TaskQueues.isCompleted()) {
//            Order.setFlage("0");
//        }
        MediaPlayerHelper.getInstance().resume();
        //观察utteranceId为0的语音是否朗读完毕，之后继续朗读其他语音
        if (utteranceId.equals("0")) {
            //恢复视频声音
            MediaStatusManager.stopMediaPlay(false);
            RobotStatus.INSTANCE.getIdentifyFaceSpeak().postValue(1);
        }
//        RobotStatus.INSTANCE.getProgress().observeForever(integer -> {
//            if (utteranceId.equals("explanation") && integer == Universal.ExplainLength) {
//                Log.d(TAG, "Tips: 讲解内容完成");
//                RobotStatus.INSTANCE.getSpeakContinue().postValue(3);
//            }
//        });
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
