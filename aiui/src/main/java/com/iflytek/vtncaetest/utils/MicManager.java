package com.iflytek.vtncaetest.utils;

import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder;
import android.util.Log;


import java.util.List;

/**
 * 录音冲突管理
 */
public class MicManager {
    private static final String TAG = MicManager.class.getSimpleName();
    private static AudioManager audioManager;
    private static Boolean AiRecording = false; //语音助手的录音状态，true(录音中),false(未录音)

    public static void startRecordWhenIdle(Context context) {

        new Thread(() -> {
            while (!AiRecording) {
                audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                Log.d(TAG, "mode==" + audioManager.getMode());
                //audioManager.getMode()==3是通话状态
                //audioManager.getMode()==0是空闲状态，此时可以开始让语音助手开始录音
                if (audioManager.getMode() == 0) {

                    //TODO 添加开始语音助手录音的代码
                    //startRecord()
                    AiRecording = true;
                }

                //间隔时间休眠，减少资源占用
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void stopRecordWhenBusy(Context context) {
        audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audioManager.registerAudioRecordingCallback(new AudioManager.AudioRecordingCallback() {
            @Override
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                super.onRecordingConfigChanged(configs);
                for (AudioRecordingConfiguration audioRecordingConfiguration : configs) {
                    Log.d(TAG, "audioSource==" + audioRecordingConfiguration.getClientAudioSource());
                    if (audioRecordingConfiguration.getClientAudioSource() == MediaRecorder.AudioSource.VOICE_COMMUNICATION) {
                        //TODO 添加停止语音助手录音的代码
                        //stopRecord()
                        AiRecording = false;
                        //开始监听录音机,空闲时开启ai语音助手
                        startRecordWhenIdle(context);
                    }
                }
            }
        }, null);
    }


}
