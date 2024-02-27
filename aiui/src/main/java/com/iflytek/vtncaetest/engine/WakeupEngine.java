package com.iflytek.vtncaetest.engine;


import static com.iflytek.vtncaetest.engine.EngineConstants.freeAsr;
import static com.iflytek.vtncaetest.engine.EngineConstants.gainTest;
import static com.iflytek.vtncaetest.engine.EngineConstants.mAIUIState;
import static com.iflytek.vtncaetest.engine.EngineConstants.saveAudio;
import static com.iflytek.vtncaetest.engine.EngineConstants.serialNumber;
import static com.iflytek.vtncaetest.engine.EngineConstants.wakeEngineLogLevel;
import static com.iflytek.vtncaetest.engine.EngineConstants.wakeupEngineDir;

import android.util.Log;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.iflyos.cae.CAE;
import com.iflytek.iflyos.cae.ICAEListener;
import com.iflytek.vtncaetest.utils.AudioAmplify;
import com.iflytek.vtncaetest.utils.ErrorCode;
import com.iflytek.vtncaetest.utils.FileUtil;
import com.iflytek.vtncaetest.utils.GainTest;
import com.iflytek.vtncaetest.utils.NetWorkUtil;
import com.iflytek.vtncaetest.utils.SoundPoolUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;


public class WakeupEngine {
    private static final String TAG = WakeupEngine.class.getSimpleName();
    private static WakeupEngine wakeupEngine;
    private static Boolean suspected_wakeup = false; //低阈值标记
    // 信息透传回调监听
    private static WakeupListener wakeupListener;
    //缓存唤醒前的数据
    private static Queue<byte[]> queue = new LinkedList<>();
    private static byte[] asrAudio;

    private WakeupEngine() {
    }

    //初始化，单例模式
    public static int getInstance(WakeupListener wakeupListener) {
        // 检查对象是否存在,不存在就进入同步区块
        if (wakeupEngine == null) {
            // 同步区块里面的代码只有在第一次才会执行
            synchronized (WakeupEngine.class) {
                if (wakeupEngine == null) {
                    WakeupEngine.wakeupListener = wakeupListener;
                    CAE.loadLib();
                    CAE.setLibName(EngineConstants.DenoiseLibName);//指定算法so名称，不指定默认libvtn.so
                    //唤醒引擎读取通用配置
                    String configPath = String.format("%s/%s", wakeupEngineDir, EngineConstants.iniPath);
                    int result = CAE.CAENew(serialNumber, configPath, mCAEListener);
                    if (result == 0) {
                        wakeupEngine = new WakeupEngine();
                        Log.i(TAG, "wakeupEngine初始化成功");
//                        SoundPoolUtil.create();
                    } else {
                        Log.e(TAG, "wakeupEngine初始化失败,错误码：" + result);
                        Log.e(TAG, "解决方案：" + ErrorCode.getError(result) + " \n 错误解决详情参考：https://www.yuque.com/iflyaiui/zzoolv/igbuol");
                    }
                    // Alsa-Jni日志控制 1(debug) 2(info) 3(warn) 4(error) 5(fatal)
                    CAE.CAESetShowLog(wakeEngineLogLevel);
                    return result;
                }
            }
        }
        Log.i(TAG, "return wakeupEngine");
        return 0;
    }

    //唤醒结果回调
    static ICAEListener mCAEListener = new ICAEListener() {
        //降噪后音频送去识别，16k，16bit，1声道
        @Override
        public void onAudioCallback(byte[] caeOutputAudio, int length) {

//            Log.i(TAG, "audio length="+length);
            if (freeAsr) {
                if (length != 1024 && length != 2048 && length != 2560) {
                    Log.e(TAG, "onAudioCallback()数据长度异常,length=" + length);
                    return;
                }

                /*缓存320ms-400ms音频（根据实际硬件修改）,超出长度丢弃
                  1024byte相当于32ms,2048相当于64ms，2560相当于80ms
                 */
                if ((length == 1024 && queue.size() > 10) || (length == 2048 && queue.size() > 4) || (length == 2560 && queue.size() > 3)) {
                    queue.poll();
                }
                //缓存数据
                queue.add(caeOutputAudio);
            }

            // CAE降噪后音频写入AIUI SDK,AIUI内部用VAD过滤无效音频，再送到云端识别
            if (AIUIConstant.STATE_WORKING == mAIUIState && EngineConstants.wakeupType == EngineConstants.WAKEUPTYPE_VOICE) {
                if (freeAsr) {
                    //连说模式将缓存数据取出来
                    byte[] temp = new byte[queue.size() * length];
                    int offset = 0;
                    while (queue.size() > 0) {
                        Log.e(TAG, "queue size =" + queue.size());
                        System.arraycopy(queue.poll(), 0, temp, offset, length);
                        offset += length;
                    }
                    asrAudio = temp;
                } else {
                    asrAudio = caeOutputAudio;
                }

                //改变识别音频增益
                if (EngineConstants.asrAudioGain != 1.0f) {
                    AudioAmplify.amplifyAll(asrAudio, EngineConstants.asrAudioGain);
                }
                //保存识别音频(跟afterDenoise.pcm的区别是只保存唤醒后的音频)，16k-16bit-1声道
                if (saveAudio) {
                    FileUtil.writeFile(asrAudio, "/sdcard/asr.pcm");
                }
                //振幅测试
                if (gainTest) {
                    int sample = GainTest.calculate(asrAudio);
                    //环境安静时，说“小飞小飞”，一句话里面有10个点采样值超过2000则正常
                    if (sample > 10) {
                        Log.i(TAG, "振幅合格，振幅超过2000的采样点数量为：" + sample);
                    } else {
                        Log.e(TAG, "振幅不合格，振幅超过2000的采样点数量为：" + sample);
                    }
                }
                //音频送给识别引擎，格式：16k 16bit 1声道
                AiuiEngine.MSG_writeAudio(asrAudio);
            }
        }

        @Override
        public void onIvwAudioCallback(byte[] bytes, int i) {
            //保存唤醒+识别音频，一般不需要处理
        }

        @Override
        public void onWakeup(String result) {
            if (EngineConstants.wakeInfoOn) {
                JSONObject wakeupResult = null;
                try {
                    wakeupResult = new JSONObject(result).getJSONObject("ivw");
                    int physicalBeam = wakeupResult.getInt("physical"); //波束
                    int score = wakeupResult.getInt("score");           //唤醒得分，得分>阈值才会抛出唤醒
                    int angle = wakeupResult.getInt("angle");           //唤醒角度
                    String keyWord = wakeupResult.getString("keyword"); //唤醒词
                    Log.i(TAG, "唤醒回调onWakeup(),angle:" + angle + " beam:" + physicalBeam + " score=" + score + " 唤醒词" + keyWord);
            /*选一种唤醒处理逻辑，推荐2，4
              1.只有唤醒词，SingleWakeWord(physicalBeam,score,angle,keyWord);
              2.唤醒词+唤醒后命令词，WakeWordAndCommandWord(physicalBeam,score,angle,keyWord);
              3.唤醒词+免唤醒命令词，WakeWordAndFreeWakeCommandWord(physicalBeam,score,angle,keyWord);
              4.双阈值(只适合浅定制唤醒词，深定制不能用)，doubleThreshold(physicalBeam,score,angle,keyWord,950,8000)
            */
                    SingleWakeWord(physicalBeam, score, angle, keyWord);
                } catch (JSONException e) {
                }

            } else {
                //量产设备不需要解析具体的唤醒信息，节省算力
                SingleWakeWord(0, 0, 0, "");
            }
        }
    };

    //1个唤醒词处理方式
    private static void SingleWakeWord(int physicalBeam, int score, int angle, String keyWord) {
        wakeResponse(physicalBeam, score, angle, keyWord);
    }

    //唤醒词+唤醒后命令词执行逻辑(误触发较低)
    private static void WakeWordAndCommandWord(int physicalBeam, int score, int angle, String keyWord) {
        //主唤醒词(不同唤醒词keyword不同)
        if (keyWord.contains("xiao3 fei1 xiao3 fei1")) {
            wakeResponse(physicalBeam, score, angle, keyWord);
        }
        //命令词执行逻辑
        else if (keyWord.contains("zeng1 da4 yin1 liang4") && AIUIConstant.STATE_WORKING == mAIUIState) {
            //没网才用离线命令,因为在线的效果更好
            if (!NetWorkUtil.isUsable()) {
                Log.d(TAG, "WakeWordAndCommandWord");
                //TODO 执行具体命令
            }
        }
    }

    //唤醒词+免唤醒命令词逻辑（容易误触发）
    private static void WakeWordAndFreeWakeCommandWord(int physicalBeam, int score, int angle, String keyWord) {
        //主唤醒词(不同唤醒词keyword不同)
        if (keyWord.contains("xiao3 fei1 xiao3 fei1")) {
            wakeResponse(physicalBeam, score, angle, keyWord);
        }
        //命令词执行逻辑，aiui在working状态是不响应离线命令的，因为对话内容可能包含某个离线命令
        else if (keyWord.contains("zeng1 da4 yin1 liang4") && mAIUIState != AIUIConstant.STATE_WORKING) {
            Log.d(TAG, "WakeWordAndFreeWakeCommandWord");
            //TODO 执行具体命令
        }
    }

    /**
     * 双阈值，略微优化唤醒体验
     * 注意：唤醒模型(res.bin) 的阈值要低于HighThreshold，不然双阈值无效
     *
     * @param HighThreshold       高唤醒阈值，必须比唤醒词bin的阈值高，建议950-1000
     * @param reduceThresholdTime 疑似唤醒，降低阈值的持续时间，单位ms， 示例：8000
     */
    private static void doubleThreshold(int physicalBeam, int score, int angle, String keyWord, int HighThreshold, int reduceThresholdTime) {
        Log.i(TAG, "唤醒成功,angle:" + angle + " beam:" + physicalBeam + " score=" + score + " 唤醒词" + keyWord);
        //疑似唤醒，降低阈值
        if (score < HighThreshold) {
            if (suspected_wakeup) {   //连续2次疑似唤醒，直接唤醒
                wakeResponse(physicalBeam, score, angle, keyWord);
            } else { //第一次疑似唤醒，降低阈值但不响应唤醒
                suspected_wakeup = true;
                Log.d(TAG, "切换为低唤醒阈值");
                new Thread(() -> {
                    try {
                        Thread.sleep(reduceThresholdTime);  //延迟执行
                        suspected_wakeup = false;  //到时间后，恢复到高阈值
                        Log.d(TAG, "切换为高唤醒阈值");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else {
            wakeResponse(physicalBeam, score, angle, keyWord);
        }
    }

    //唤醒后的响应
    private static void wakeResponse(int physicalBeam, int score, int angle, String keyWord) {
        //唤醒后播放提示音或用灯光，UI响应
//        SoundPoolUtil.play("唤醒提示音");
        //唤醒后给AIUI SDK发送唤醒事件：让AIUI SDK工作
        AiuiEngine.MSG_wakeup(EngineConstants.WAKEUPTYPE_VOICE);
        //回调唤醒结果给上层
        wakeupListener.onWakeup(angle, physicalBeam, score, keyWord);
    }


    public static void destroy() {
        if (wakeupEngine != null) {
            wakeupEngine = null;
            CAE.CAEDestory();
            Log.i(TAG, "销毁wakeupEngine");
        }
//        SoundPoolUtil.release();
    }

    // 外部音频写入唤醒引擎进行降噪和唤醒
    public static void writeAudioToCAE(byte[] data) {
        if (wakeupEngine != null) {
            CAE.CAEAudioWrite(data, data.length);
        } else {
            Log.e(TAG, "wakeupEngine未初始化");
        }
    }

    // 拾音波束方向，语音唤醒后自动处理，手动唤醒需要根据具体方向调用
    public static void setRealBeam(int beam) {
        CAE.CAESetRealBeam(beam);
    }

    // 获取CAE版本
    public static String getCAEVersion() {
        return CAE.CAEGetVersion();
    }

    /**
     * 动态替换唤醒词
     *
     * @param wakewordBinPath 唤醒资源路径，示例：/sdcard/vtn/res.bin
     * @return
     */
    public static int reloadWakeResource(String wakewordBinPath) {
        if (wakeupEngine != null) {
            return CAE.CAEReloadResource(wakewordBinPath);
        } else {
            Log.e(TAG, "wakeupEngine未初始化");
            return -1;
        }
    }

    /**
     * 仅支持2麦gvad算法，其他算法请勿使用
     * 手动触发全向唤醒
     */
    public static void mockWakeup() {
//        CAE.CAESetParams(CAE.CAE_PARAM_MOCK_WAKEUP, null);
    }
}
