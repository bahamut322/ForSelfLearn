package com.iflytek.vtncaetest.recorder;

import static com.iflytek.vtncaetest.engine.EngineConstants.saveAudio;

import android.util.Log;

import com.iflytek.alsa.AlsaRecorder;
import com.iflytek.vtncaetest.engine.EngineConstants;
import com.iflytek.vtncaetest.engine.WakeupEngine;
import com.iflytek.vtncaetest.utils.AudioAmplify;
import com.iflytek.vtncaetest.utils.AudioMerge;
import com.iflytek.vtncaetest.utils.FileUtil;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 1.android只支持2声道，因此多麦需要直接从alsa录音，参考文档：https://www.yuque.com/iflyaiui/zzoolv/zmv0f5
 * 2.对2个声卡录音，回采数据1个声卡，mic数据1个声卡
 */
public class DoubleAlsaRecorder implements AudioRecorder {
    private static final String TAG = DoubleAlsaRecorder.class.getSimpleName();

    private static LinkedBlockingQueue<byte[]> micQueue = new LinkedBlockingQueue<>();//缓存mic数据，因为2个声卡节点录音，数据要对齐
    private static LinkedBlockingQueue<byte[]> refQueue = new LinkedBlockingQueue<>();//缓存回采数据，因为2个声卡节点录音，数据要对齐
    int mergeDataSize;//回采与mic数据合并后，音频帧大小
    byte[] mergeData; //最终输出的数据
    //  录音数据信息透传回调监听
    private AlsaRecorder micRecorder;
    private AlsaRecorder refRecorder;

    //true(开启丢数据测试)
    boolean DatalossTest = true;
    int micCount = 0;  //mic录音机回调次数
    int refCount = 0;  //ref录音机回调次数
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
    private final static int mic_SampleRate = 16000;
    //一次中断的读取的帧数 一般不修改，某些不支持这么大数字时会报错，可以尝试减小。增大该值可以降低cpu，但是2次数据之间的延迟会增大
    private final static int mic_PeriodSize = 128*EngineConstants.mic_Channel;
    //周期数 一般不修改
    private final static int mic_PeriodCount = 8;
    //0(16bit-小端格式),1(32bit-小端格式)
    private final static int mic_Format = 0;
    //回调的音频帧大小。如果录音采样率不是16k,在转换音频的时候需要适配，例如48K音频录音可以改成 6144
    private final static int mic_BufferSize = mic_PeriodSize * mic_PeriodCount;


    //麦克风采样率，一般16k
    private final static int ref_SampleRate = 16000;
    //一次中断的读取的帧数 一般不修改，某些不支持这么大数字时会报错，可以尝试减小。增大该值可以降低cpu，但是2次数据之间的延迟会增大
    private final static int ref_PeriodSize = 128 * EngineConstants.ref_Channel;
    //周期数 一般不修改
    private final static int ref_PeriodCount = 8;
    //0(16bit-小端格式),1(32bit-小端格式)
    private final static int ref_Format = 0;
    //回调的音频帧大小。如果录音采样率不是16k,在转换音频的时候需要适配，例如48K音频录音可以改成 6144
    private final static int ref_BufferSize = ref_PeriodSize * ref_PeriodCount;

    // mic录音回调
    AlsaRecorder.PcmListener micAlsaPcmListener = new AlsaRecorder.PcmListener() {
        @Override
        public void onPcmData(byte[] micData, int length) {
            //改变原始音频增益
            if (EngineConstants.rawAudioGain != 1.0f) {
                AudioAmplify.amplifyAll(micData, EngineConstants.rawAudioGain);
            }
            //缓存mic数据到队尾
            micQueue.offer(micData);
            //mic和回采丢数据测试
            if (DatalossTest) {
                micCount++;
                Log.d(TAG, "mic size=" + length + " micCount=" + micCount);
                //运行一段时间，如果数据差5个buffer，就认为丢数据了
                int diff = micCount - refCount;
                if (diff > 5) {
                    Log.e(TAG, "回采可能丢数据了,count差值=" + diff);
                } else if (diff < -5) {
                    Log.e(TAG, "mic可能丢数据了,count差值=" + diff);
                }
            }
            if (mergeData == null) {
                mergeDataSize = mic_BufferSize + ref_BufferSize;
                mergeData = new byte[mergeDataSize];
            }
            if (!refQueue.isEmpty()) {
                //合并mic和回采数据为一个文件
                mergeData = AudioMerge.merge(micQueue.poll(), EngineConstants.mic_Channel, refQueue.poll(), EngineConstants.ref_Channel);

                //保存音频
                if (saveAudio) {
                    FileUtil.writeFile(micData, "/sdcard/mic_raw.pcm");
                    FileUtil.writeFile(mergeData, "/sdcard/merge.pcm");
                }
                //送数据给唤醒引擎
                WakeupEngine.writeAudioToCAE(mergeData);
            }
        }

        @Override
        public void onError(int errorCode, String errorStr) {
            if (errorCode == -16) {
                destroyRecord();
            }
        }
    };

    // 回采录音回调
    AlsaRecorder.PcmListener refAlsaPcmListener = new AlsaRecorder.PcmListener() {
        @Override
        public void onPcmData(byte[] refData, int length) {
            //缓存回采数据到队尾,调用object的接口clone克隆当前对象 进行数据深拷贝，避免多线程操作数据导致回采数据丢失的问题
            refQueue.offer(refData.clone());
            //mic和回采丢数据测试
            if (DatalossTest) {
                refCount++;
                Log.d(TAG, "ref size=" + length + " refCount=" + refCount);
            }
            //保存音频
            if (saveAudio) {
                FileUtil.writeFile(refData, "/sdcard/ref_raw.pcm");
            }
        }

        @Override
        public void onError(int errorCode, String errorStr) {
            if (errorCode == -16) {
                destroyRecord();
            }
        }
    };

    public DoubleAlsaRecorder() {
        if (micRecorder == null && refRecorder == null) {
            refRecorder = AlsaRecorder.createInstance(EngineConstants.ref_Card, EngineConstants.ref_Device, EngineConstants.ref_Channel, ref_SampleRate,
                    ref_PeriodSize, ref_PeriodCount, ref_Format, ref_BufferSize);
            micRecorder = AlsaRecorder.createInstance(EngineConstants.mic_Card, EngineConstants.mic_Device, EngineConstants.mic_Channel, mic_SampleRate,
                    mic_PeriodSize, mic_PeriodCount, mic_Format, mic_BufferSize);
            refRecorder.setLogShow(false);                // Alsa-Jni日志控制 true-开启  false-关闭
            micRecorder.setLogShow(false);                // Alsa-Jni日志控制 true-开启  false-关闭
        }
    }

    public int startRecord() {
        if (micRecorder != null && refRecorder != null) {
            int refReturn = refRecorder.startRecording(refAlsaPcmListener);
            int micReturn = micRecorder.startRecording(micAlsaPcmListener);
            if (0 == micReturn && 0 == refReturn) {
                Log.i(TAG, "start recording sucess");
            } else {
                Log.e(TAG, "start recording fail, micErrorCode" + micReturn + "  refErrorCode" + refReturn);
                Log.e(TAG, "错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol ");
            }
            return micReturn;
        } else {
            Log.e(TAG, "AlsaRecorder is null");
            return 111111;
        }
    }

    // 停止录音
    public void stopRecord() {
        micRecorder.stopRecording();
        refRecorder.stopRecording();
        //清空数组
        micQueue.clear();
        refQueue.clear();
        mergeData = null;
        Log.i(TAG, "stop record");
    }


    public void destroyRecord() {
        if (micRecorder != null && refRecorder != null) {
            stopRecord();
            micRecorder.destroy();
            refRecorder.destroy();
            micRecorder = null;
            refRecorder = null;
            Log.i(TAG, "销毁recorder");
        }
    }
}
