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
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Order;

import java.util.Objects;

/**
 * SpeechSynthesizerListener 简单地实现，仅仅记录日志
 * Created by fujiayi on 2017/5/19.
 */

public class MessageListener implements SpeechSynthesizerListener, MainHandlerConstant {
    private static final String TAG = "MessageListener";

    private static int progressSpeak = -1;

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
        if (utteranceId == "explanation") {
            return;
        }
        RobotStatus.INSTANCE.setTtsIsPlaying(true);
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
        new Thread(() -> {
            if (utteranceId.equals("explanation")) {
                //首先规避一下重复数
                if (progress == previousProgress) {
                    LogUtil.INSTANCE.i("生成了重复数");
                } else if (Universal.ExplainSpeak.size() != 0) {
                    //当TTS的播放进度和列表第一项相等的时候(为了数统一，不然我也不会在播放进度和第一项长度相等的时候做处理，显得没事干)
                    if (progress == Universal.ExplainSpeak.get(0)) {
                        //将每次的第一项累加在一起
                        Universal.taskNum += Universal.ExplainSpeak.get(0);
                        Log.d(TAG, "当前朗读完的item的总算" + Universal.taskNum);
                        //为了简单(主要是懒，懒得维护队列)，移除第一项，将后面的子项往前移动变成第一项
                        //在将数据赋值给观察者
                        RobotStatus.INSTANCE.getProgress().postValue(Universal.taskNum);
                        Universal.ExplainSpeak.remove(0);
                    } else {
                        //这里是统计零散的数据长度。计算方法就是：每次累加的子项+TTS当前播放长度=总播放长度
                        if (progress != Universal.ExplainSpeak.get(0)) {
                            Log.d(TAG, "当前朗读完的item的总算" + Universal.taskNum);
                            RobotStatus.INSTANCE.getProgress().postValue(Universal.taskNum + progress);
                        }
                    }
                }
                LogUtil.INSTANCE.e(" BaiduTTS播放进度：" + progress
                        + "播放总进度：" + RobotStatus.INSTANCE.getProgress().getValue()
                        + " 当前朗读完的item的总算：" + Universal.taskNum
                        + " 播放目标进度：" + Universal.ExplainLength
                );
                previousProgress = progress; // 更新前一次的 progress，避重
            }
        }).start();

        Log.i(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId);

    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        if(!Objects.equals(utteranceId, "explanation")){
            RobotStatus.INSTANCE.setTtsIsPlaying(false);
        }
        sendMessage("播放结束回调, 序列号:" + utteranceId);
//        if (TaskQueues.isCompleted()) {
//            Order.setFlage("0");
//        }
        MediaPlayerHelper.getInstance().resume();
        //观察utteranceId为0的语音是否朗读完毕，之后继续朗读其他语音
        if (utteranceId.equals("0")) {
            //恢复视频声音
            Order.setFlage("0");
            RobotStatus.INSTANCE.getIdentifyFace().postValue(1);
            if (Objects.requireNonNull(RobotStatus.INSTANCE.getSpeakNumber().getValue()).length() != 0) {
                RobotStatus.INSTANCE.getSpeakContinue().postValue(1);
            }
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
