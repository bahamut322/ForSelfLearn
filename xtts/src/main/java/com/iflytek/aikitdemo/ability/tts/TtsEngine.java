package com.iflytek.aikitdemo.ability.tts;

import android.media.AudioFormat;
import android.os.SystemClock;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.aikit.core.AiEvent;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiListener;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiStatus;
import com.iflytek.aikitdemo.ability.AbilityConstant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Desc: 合成播放引擎简单封装
 * @Author leon
 * @Date 2023/9/22-14:09
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
public class TtsEngine implements AiListener {


    private static final String TAG = "TtsEngine";

    private SynthesisCallback mCallback;
    private String engineId;
    private AiHandle aiHandle;
    private Queue<byte[]> byteQueue;
    private ExecutorService executorService;
    private volatile boolean loop = true;
    private volatile boolean isPause;
    private volatile boolean isSpeaking;

    private int cacheTotalLength = -1;

    private String text = "";

    public void setText(String text){
        this.text = text;
    }

    public final void onCreate(@NotNull String engineId, TtsHelper.XTTSCallback xttsCallback) {
        this.engineId = engineId;
        AiHelper.getInst().registerListener(engineId, this);
        if (this.byteQueue == null) {
            this.byteQueue = new ConcurrentLinkedQueue<>();
        }
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(2);
        }
        this.executorService.execute((Runnable) (new Runnable() {
            public void run() {
                while (TtsEngine.this.loop) {
                    if (TtsEngine.this.isPause) {
                        SystemClock.sleep(50L);
                    } else {
                        Queue<byte[]> queue = TtsEngine.this.byteQueue;
                        byte[] data = queue != null ? queue.poll() : null;
                        SynthesisCallback synthesisCallback;
                        if (data != null) {
                            if (cacheTotalLength > 0) {
                                float progress = (cacheTotalLength - queue.size()) * 1f / cacheTotalLength;
                                if (progress > 0) {
                                    TtsEngine.this.isSpeaking = true;
                                    xttsCallback.progressChange((int) (text.length() * progress));
                                    if(progress == 1){
                                        Log.d(TAG, "total length:" + cacheTotalLength );
                                        xttsCallback.speakAllFinish();
                                    }
                                }
                            }
                            synthesisCallback = TtsEngine.this.mCallback;
                            if (synthesisCallback != null) {
                                synthesisCallback.audioAvailable(data, 0, data.length);
                            }
//                            Log.d(TAG, "合成播放 audioTrack available");
                        } else if (TtsEngine.this.isSpeaking) {
                            synthesisCallback = TtsEngine.this.mCallback;
                            if (synthesisCallback != null) {
                                synthesisCallback.done();
                            }
                            TtsEngine.this.isSpeaking = false;
                            Log.d(TAG, "合成播放结束");
                        }
                    }
                }
            }
        }));
    }

    public final synchronized void textToSpeech(
            @NotNull String text,
            @NotNull TtsSpeechPrams params,
            @NotNull SynthesisCallback callback) {
        if (TextUtils.isEmpty(text)) return;
        this.mCallback = callback;
        int ret = -1;
        ret = AiHelper.getInst().engineInit(this.engineId);
        if (ret != AbilityConstant.ABILITY_SUCCESS_CODE) {
            Log.w(TAG, "open ivw error code ===> $ret");
            if (mCallback != null) {
                mCallback.error(ret);
            }
            return;
        }
        if (aiHandle != null) {
            AiHelper.getInst().end(aiHandle);
        }
        AiRequest.Builder paramBuilder = AiRequest.builder();
        paramBuilder.param("rdn", 0);
        paramBuilder.param("reg", 0);
        paramBuilder.param("textEncoding", "UTF-8");
        paramBuilder.param("vcn", params.getVcn());
        paramBuilder.param("pitch", params.getPitch());
        paramBuilder.param("volume", params.getVolume());
        paramBuilder.param("speed", params.getSpeed());
        if (Objects.equals(engineId, AbilityConstant.XTTS_ID)) {
            paramBuilder.param("language", params.getLanguage());
        }
        this.aiHandle = AiHelper.getInst().start(engineId, paramBuilder.build(), null);
        if (this.aiHandle.getCode() != AbilityConstant.ABILITY_SUCCESS_CODE) {
            Log.w(TAG, "启动会话失败");
            if (mCallback != null) {
                mCallback.error(this.aiHandle.getCode());
            }
            return;
        }
        // 构建写入数据
        AiRequest.Builder dataBuilder = AiRequest.builder();
        dataBuilder.text("text", text);
        // 写入数据
        ret = AiHelper.getInst().write(dataBuilder.build(), aiHandle);
        if (ret != AbilityConstant.ABILITY_SUCCESS_CODE) {
            Log.w(TAG, "合成写入数据失败");
            if (mCallback != null) {
                mCallback.error(ret);
            }
            return;
        }
        Log.w(TAG, "合成写入开始");
    }

    public final void pause() {
        this.isPause = true;
    }

    public final void resume() {
        this.isPause = false;
    }

    public final void stop() {
        isSpeaking = false;
        Queue<byte[]> queue = this.byteQueue;
        if (queue != null) {
            queue.clear();
        }
        this.pause();
        if (this.aiHandle != null) {
            AiHelper.getInst().end(this.aiHandle);
        }
        this.aiHandle = null;
    }

    public final void onDestroy() {
        this.stop();
        this.loop = false;
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
        this.executorService = null;
    }


    /**
     * @see AiStatus
     * @param handleID
     * @param responseData
     * @param usrContext
     */
    @Override
    public void onResult(int handleID, @Nullable List<AiResponse> responseData, @Nullable Object usrContext) {
//        if (!isSpeaking) return;
//        Log.d(TAG, "引擎下发数据回调");
        if (responseData == null || responseData.isEmpty()){
            Log.d(TAG, "引擎下发的数据为null");
            return;
        };
        for (AiResponse aiResponse : responseData) {
            byte[] value = aiResponse.getValue();
            if (value == null) {
                continue;
            }
            byteQueue.offer(value);
            //@see {}
//            Log.d(TAG, "引擎数据状态==>" + aiResponse.getStatus());
        }
    }

    @Override
    public void onEvent(int handleID, int event, @Nullable List eventData, @Nullable Object usrContext) {
        SynthesisCallback callback;
        if (event == AiEvent.EVENT_START.getValue()) {
            callback = this.mCallback;
            if (callback != null) {
                callback.start(-1, AudioFormat.ENCODING_PCM_16BIT, 1);
            }
            Log.d(TAG, "引擎计算开始");
            cacheTotalLength = -1;
        } else if (event == AiEvent.EVENT_PROGRESS.getValue()) {
            Log.d(TAG, "引擎计算中");
        } else if (event == AiEvent.EVENT_END.getValue()) {
            Log.d(TAG, "引擎计算结束" + Thread.currentThread().getName());
            cacheTotalLength = byteQueue.size();
//            Log.d(TAG, "eventData size:"+ byteQueue.size());
//            this.loop = false;
        } else if (event == AiEvent.EVENT_TIMEOUT.getValue()) {
            callback = this.mCallback;
            if (callback != null) {
                callback.error(TextToSpeech.ERROR_NETWORK_TIMEOUT);
            }
        }
    }

    @Override
    public void onError(int handleID, int err, @Nullable String msg, @Nullable Object usrContext) {
        SynthesisCallback callback = this.mCallback;
        if (callback != null) {
            callback.error(err);
        }
    }
}
