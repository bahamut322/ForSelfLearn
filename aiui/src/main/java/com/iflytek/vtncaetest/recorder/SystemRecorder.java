package com.iflytek.vtncaetest.recorder;


import static com.iflytek.vtncaetest.engine.EngineConstants.isRecording;
import static com.iflytek.vtncaetest.engine.EngineConstants.saveAudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.iflytek.vtncaetest.engine.AiuiEngine;
import com.iflytek.vtncaetest.engine.EngineConstants;
import com.iflytek.vtncaetest.engine.WakeupEngine;
import com.iflytek.vtncaetest.utils.AudioAmplify;
import com.iflytek.vtncaetest.utils.AudioFilter;
import com.iflytek.vtncaetest.utils.FileUtil;

public class SystemRecorder implements AudioRecorder {
    private static final String TAG = SystemRecorder.class.getSimpleName();
    private static SystemRecorder recorder;
    //录音缓存
    private static int mBufferSize;
    //数据处理模式,true(不降噪唤醒直接识别,16k16bit,1声道),false(先降噪唤醒再识别，16k16bit,2声道)
    public static boolean AUDIO_TYPE_ASR = false;
    //录音机
    private static AudioRecord audioRecord;

    //单例模式,私有构造器
    private SystemRecorder() {
    }

    //单例模式
    public static SystemRecorder getInstance() {
        // 检查对象是否存在,不存在就进入同步区块
        if (recorder == null) {
            // 同步区块里面的代码只有在第一次才会执行
            synchronized (SystemRecorder.class) {
                if (recorder == null) {
                    recorder = new SystemRecorder();
                    int SAMPLE_RATE = 16000;
                    mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    if(EngineConstants.recorderType==0) {
                        audioRecord = new AudioRecord(
                                MediaRecorder.AudioSource.VOICE_COMMUNICATION,  //获取手机回声消除后的音频
                                SAMPLE_RATE,                                    //采样率16k
                                AudioFormat.CHANNEL_IN_MONO,                    //录制1声道数据，算法需要1mic+1ref数据,后面补充补0
                                AudioFormat.ENCODING_PCM_16BIT,                 //16bit数据
                                mBufferSize
                        );
                    }else if (EngineConstants.recorderType == 3) {
                        audioRecord = new AudioRecord(
                                MediaRecorder.AudioSource.MIC,                  //获取原始音频
                                SAMPLE_RATE,                                    //采样率16k
                                AudioFormat.CHANNEL_IN_STEREO,                  //录制2声道数据，1mic+1ref
                                AudioFormat.ENCODING_PCM_16BIT,                 //16bit数据
                                mBufferSize
                        );
                    } else if (EngineConstants.recorderType == 4) {
                        audioRecord = new AudioRecord(
                                MediaRecorder.AudioSource.MIC,                  //获取原始音频
                                SAMPLE_RATE,                                    //采样率16k
                                AudioFormat.CHANNEL_IN_MONO,                  //录制2声道数据，1mic+1ref
                                AudioFormat.ENCODING_PCM_16BIT,                 //16bit数据
                                mBufferSize
                        );
                    }
                    Log.i(TAG, "创建systemRecorder成功");
                }
            }
        }
        Log.i(TAG, "return systemRecorder");
        return recorder;
    }


    @Override
    public void stopRecord() {
        isRecording = false;
        audioRecord.stop();
        Log.i(TAG, "停止录音");
    }

    @Override
    public void destroyRecord() {
        stopRecord();
        recorder = null;
        Log.i(TAG, "销毁recorder");
    }

    @Override
    public int startRecord() {
        if (AUDIO_TYPE_ASR) {
            //识别模式让aiui进入工作状态，唤醒模式唤醒后再让aiui进入工作状态，否则唤醒音频直接送去识别了
            AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE);
        }

        //已经在录音中就不创建线程了
        if (isRecording) {
            return 0;//0(成功)
        }

        new Thread(() -> {
            // 开始录音
            audioRecord.startRecording();
            isRecording = true;
            try {
                byte[] audioData = new byte[mBufferSize];
                while (isRecording) {
                    int readSize = audioRecord.read(audioData, 0, mBufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != readSize && isRecording) {
                        //改变原始音频增益
                        if(EngineConstants.rawAudioGain!=1.0f){
                            AudioAmplify.amplifyAll(audioData,EngineConstants.rawAudioGain);
                        }

                        //保存原始音频，16k16bit单声道
                        if (saveAudio) {
                            FileUtil.writeFile(audioData, "/sdcard/raw.pcm");
                        }
                        //如果音频送去识别，只保留第一路数据
                        if (AUDIO_TYPE_ASR) {
                            //改变识别音频增益
                            if(EngineConstants.asrAudioGain!=1.0f){
                                AudioAmplify.amplifyAll(audioData,EngineConstants.asrAudioGain);
                            }
                            byte[] asrData=audioData;
                            if(EngineConstants.recorderType==3) {
                                //识别引擎需要1声道数据，因为recorderType==3录音是2mic，去掉第2声道数据
                                asrData= AudioFilter.convert(audioData, 2, "0");
                            }
                            //保存识别音频，16k16bit单声道
                            if (saveAudio) {
                                FileUtil.writeFile(asrData, "/sdcard/asr.pcm");
                            }
                            //将数据送给送给aiui去识别
                            AiuiEngine.MSG_writeAudio(asrData);
                        }
                        //如果音频送去降噪唤醒，保留2路数据
                        else {
                            byte[] wakeupData=audioData;
                            if(EngineConstants.recorderType==0) {
                                //唤醒引擎需要2通道数据，因为recorderType==0录音是1mic，增加1通道空数据
                                wakeupData = AudioFilter.convert(audioData, 1, "0,-1");
                            }
                            //唤醒音频保存开关，16k16bit-2声道
                            if (saveAudio) {
                                FileUtil.writeFile(wakeupData, "/sdcard/beforeWakeup.pcm");
                            }
                            //将数据送入唤醒引擎
                            WakeupEngine.writeAudioToCAE(wakeupData);
                        }
                    }
                }
                //手动闭麦，为了触发vad end要送一段模拟的音频，1个sample有2byte，1ms有16个sampel(采样值)，,我选取了2000ms
                byte[] simulateVadEnd = new byte[2 * 16 * 2000];
                if (AUDIO_TYPE_ASR) {
                    //将数据送给送给aiui去识别
                    AiuiEngine.MSG_writeAudio(simulateVadEnd);
                } else {
                    //将数据送入唤醒引擎
                    WakeupEngine.writeAudioToCAE(simulateVadEnd);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Log.i(TAG, "开启录音成功！");
        return 0;        //0(成功)
    }
}
