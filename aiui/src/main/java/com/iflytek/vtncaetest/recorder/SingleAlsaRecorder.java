package com.iflytek.vtncaetest.recorder;

import static com.iflytek.vtncaetest.engine.EngineConstants.saveAudio;

import android.util.Log;

import com.iflytek.alsa.AlsaRecorder;
import com.iflytek.vtncaetest.engine.EngineConstants;
import com.iflytek.vtncaetest.engine.WakeupEngine;
import com.iflytek.vtncaetest.utils.AudioAmplify;
import com.iflytek.vtncaetest.utils.AudioFilter;
import com.iflytek.vtncaetest.utils.FileUtil;
import com.iflytek.vtncaetest.utils.RootShell;

/**
 * 1.android只支持2声道，因此多麦需要直接从alsa录音，参考文档：https://www.yuque.com/iflyaiui/zzoolv/zmv0f5
 * 2.对1个声卡录音，回采和音频数据都来自一个声卡
 */
public class SingleAlsaRecorder implements AudioRecorder {
    private static final String TAG = SingleAlsaRecorder.class.getSimpleName();
    /**
     * alsa录音参数，参考文档：https://www.yuque.com/iflyaiui/zzoolv/fp0444
     * 示例参数：tinycap /sdcard/test.pcm -D 0 -d 0 -c 8 -r 16000 -b 16 -p 1536 -n 8
     * -D card 声卡号
     * -d device 设备号
     * -c channels  通道
     * -r rate     采样率
     * -b bits     位宽
     * -p period_size 一次中断的帧数
     * -n n_periods 周期数
     */

    //麦克风采样率，一般16k
    private final static int SampleRate = 16000;

    //一次中断的读取的帧数 一般不修改，某些不支持这么大数字时会报错，可以尝试减小。增大该值可以降低cpu，但是2次数据之间的延迟会增大
    //PeriodSize 必须是channel的倍数，否则数据异常
    private final static int PeriodSize = 128*EngineConstants.Channel;

    //周期数 一般不修改
    private final static int PeriodCount = 8;

    //0(16bit-小端格式),1(32bit-小端格式)
    private final static int Format = 0;

    //缓存大小(勿修改)
    private final static int mPcmBufferSize = PeriodSize * PeriodCount;

    // tinyalsa录音音频监听器
    AlsaRecorder.PcmListener alsaListener = new AlsaRecorder.PcmListener() {
        @Override
        public void onPcmData(byte[] bytes, int length) {
            //改变通道顺序和过滤无用通道
            byte[] processedData = bytes;
            if (EngineConstants.changeChannel) {
                processedData = AudioFilter.convert(bytes, EngineConstants.Channel, EngineConstants.channleParams);
            }
            //改变原始音频增益
            if (EngineConstants.rawAudioGain != 1.0f) {
                AudioAmplify.amplifyAll(processedData, EngineConstants.rawAudioGain);
            }
            //保存音频
            if (saveAudio) {
                FileUtil.writeFile(processedData, "/sdcard/raw.pcm");
            }
            //将数据送入唤醒引擎
            WakeupEngine.writeAudioToCAE(processedData);
        }

        @Override
        public void onError(int errorCode, String errorStr) {
            if (errorCode == -16) {
                destroyRecord();
            }
        }
    };
    //  录音数据信息透传回调监听
    private AlsaRecorder alsaRecorder;

    public SingleAlsaRecorder() {
        if (alsaRecorder == null) {
            //给声卡授权
//            RootShell.execRootCmdSilent("setenforce 0");
            //给声卡授权  chmod 777 /dev/snd/pcmCxDx
//            RootShell.execRootCmdSilent("chmod 777 /dev/snd/pcmC" + EngineConstants.Card + "D" + EngineConstants.Device + "c");
            alsaRecorder = AlsaRecorder.createInstance(EngineConstants.Card, EngineConstants.Device, EngineConstants.Channel, SampleRate,
                    PeriodSize, PeriodCount, Format, mPcmBufferSize);
            alsaRecorder.setLogShow(false);                // Alsa-Jni日志控制 true-开启  false-关闭
        }
    }

    public int startRecord() {
        if (alsaRecorder != null) {
            int recRet = alsaRecorder.startRecording(alsaListener);
            if (0 == recRet) {
                Log.i(TAG, "start recording sucess");
            } else {
                Log.e(TAG, "start recording fail");
                Log.e(TAG, "错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol ");
            }
            return recRet;
        } else {
            Log.e(TAG, "AlsaRecorder is null");
            return 111111;
        }
    }

    // 停止录音
    public void stopRecord() {
        alsaRecorder.stopRecording();
        Log.i(TAG, "stop record");
    }


    public void destroyRecord() {
        if (alsaRecorder != null) {
            stopRecord();
            alsaRecorder.destroy();
            alsaRecorder = null;
            Log.i(TAG, "销毁recorder");
        }
    }

}
