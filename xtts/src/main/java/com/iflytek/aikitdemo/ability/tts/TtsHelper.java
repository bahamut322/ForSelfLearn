package com.iflytek.aikitdemo.ability.tts;


import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.SynthesisCallback;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.iflytek.aikitdemo.ContextHolder;
import com.iflytek.aikitdemo.ability.AbilityCallback;
import com.iflytek.aikitdemo.ability.AbilityConstant;
import com.iflytek.aikitdemo.tool.PcmUtil;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @Desc: 代码简单示例-语音合成辅助类
 * @Author leon
 * @Date 2024/1/22-14:27
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
public class TtsHelper implements SynthesisCallback {

    private static final String TAG = "TtsHelperV2";

    private TtsSpeechPrams ttsParams;

    private ExecutorService executorService;

    private AudioTrack audioTrack;

    private TtsEngine ttsEngine;

    private volatile boolean loop = true;

    private volatile boolean isSpeaking = false;

    private Queue<String> textQueue;

    private String engineId = "";

    private volatile String text = "";

    private volatile boolean isFlush = false;

    private AbilityCallback callback;

    private File recordFile;

    private Handler fileHandler;

    private AbilityCallback callBack;

    //缓冲区大小，表示AudioTrack内部缓冲区的大小，单位是字节，可以通过AudioTrack.getMinBufferSize()方法来获取最小的缓冲区大小
    private int bufferSize = AudioTrack.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    ) * 2;


    public TtsHelper() {
        ttsParams = new TtsSpeechPrams();
        if (textQueue == null) {
            textQueue = new ConcurrentLinkedQueue<>();
        }
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(2);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA) //设置音频的用途，有媒体、闹钟、通知等多种选项，这里使用媒体用途
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) //设置音频的内容类型，有音乐、语音、电影等多种选项，这里使用音乐类型
                                    .build()
                    ) //构建一个AudioAttributes对象
                    .setAudioFormat(
                            new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT) //设置音频的数据格式
                                    .setSampleRate(16000) //设置音频的采样率
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO) //设置音频的声道
                                    .build()
                    ) //构建一个AudioFormat对象
                    .setBufferSizeInBytes(bufferSize) //设置缓冲区大小
                    .build();
        } else {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    16000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
            );
        }
        audioTrack.play();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (loop) {
                    text = textQueue.poll();
                    if (text != null) {
                        ttsEngine.textToSpeech(text, ttsParams, TtsHelper.this);
                    }
                    SystemClock.sleep(50L);
                }
            }
        });
        HandlerThread handlerThread = new HandlerThread("writeFile");
        handlerThread.start();
        fileHandler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                int what = msg.what;
                if (what == 0) {
                    createFile();
                } else if (what == 1) {
                    Object result = msg.obj;
                    if (!(result instanceof byte[])) {
                        result = null;
                    }
                    byte[] audioBytes = (byte[]) result;
                    if (audioBytes == null) {
                        return false;
                    }
                    byte[] data = audioBytes;
                    writeToFile(data);
                }
                return true;
            }
        });
    }


    public final void onCreate(@NotNull String engineId, @NotNull AbilityCallback callBack) {
        this.engineId = engineId;
        if (this.ttsEngine == null) {
            this.ttsEngine = new TtsEngine();
            TtsEngine engine = this.ttsEngine;
            engine.onCreate(engineId);
        }
        this.callBack = callBack;
    }

    /**
     * 设置发音人
     */
    public final void setVCN(@NotNull String vcn) {
        Log.i(TAG, "设置发音人==>" + vcn);
        ttsParams.setVcn(vcn);
        if (Objects.equals(engineId, AbilityConstant.TTS_ID)) return;
        if (Objects.equals(vcn, "xiaoyan") ||
                Objects.equals(vcn, "xiaofeng")) {
            ttsParams.setLanguage(1);
        } else if (Objects.equals(vcn, "xiaoyan")) {
            ttsParams.setLanguage(2);
        } else if (Objects.equals(vcn, "xiaomei")){
            ttsParams.setLanguage(12);
        }
    }

    /**
     * 设置发音人语速
     */
    public final void setSpeed(int speed) {
        Log.i(TAG, "设置发音人语速==>" + speed);
        ttsParams.setSpeed(speed);
    }

    /**
     * 设置发音人音调
     */
    public final void setPitch(int pitch) {
        Log.i(TAG, "设置发音人音调==>" + pitch);
        ttsParams.setPitch(pitch);
    }

    /**
     * 设置发音人音量
     */
    public final void setVolume(int volume) {
        Log.i(TAG, "设置发音人音量==>" + volume);
        ttsParams.setVolume(volume);
    }


    /**
     * 将文本添加到队列中
     *
     * @param text 待合成的文本
     */
    public final void speechText(@NotNull String text) {
        this.textQueue.offer(text);
        AudioTrack track = this.audioTrack;
        if (track != null) {
            track.play();
        }
        this.resume();
    }

    /**
     * 停止当前播放并加入合成播放文本
     *
     * @param text 待合成的文本
     */
    public final void stopSpeechText(@NotNull final String text) {
        if (!this.isFlush) {
            this.isFlush = true;
            ExecutorService service = this.executorService;
            if (service != null) {
                service.execute((Runnable) (new Runnable() {
                    public final void run() {
                        TtsHelper.this.stop();
                        SystemClock.sleep(200L);
                        TtsHelper.this.speechText(text);
                        TtsHelper.this.isFlush = false;
                    }
                }));
            }
        }
    }

    public final void pause() {
        AudioTrack track = this.audioTrack;
        if (track != null) {
            track.pause();
        }
        TtsEngine engine = this.ttsEngine;
        if (engine != null) {
            engine.pause();
        }
    }

    public final void resume() {
        AudioTrack track = this.audioTrack;
        if (track != null) {
            track.play();
        }
        TtsEngine engine = this.ttsEngine;
        if (engine != null) {
            engine.resume();
        }
    }

    /**
     * 停止播放
     */
    public final void stop() {
        isSpeaking = false;
        Queue<String> queue = this.textQueue;
        if (queue != null) {
            queue.clear();
        }
        TtsEngine engine = this.ttsEngine;
        if (engine != null) {
            engine.stop();
        }
        if (this.audioTrack != null) {
            this.audioTrack.pause();
            this.audioTrack.flush();
            this.audioTrack.stop();
        }
    }


    /**
     * 释放资源
     */
    public final void release() {
        isSpeaking = false;
        this.loop = false;
        AudioTrack track = this.audioTrack;
        if (track != null) {
            track.release();
        }
        ExecutorService service = this.executorService;
        if (service != null) {
            service.shutdownNow();
        }
        TtsEngine engine = this.ttsEngine;
        if (engine != null) {
            engine.onDestroy();
        }
        this.audioTrack = null;
        this.executorService = null;
        this.ttsEngine = null;
    }


    /**
     * 是否正在播放语音
     *
     * @return true 表示正在播放中 false 表示已经播放完毕
     */
    public final boolean isSpeaking() {
        boolean b;
        if (!this.isSpeaking) {
            Queue<String> queue = this.textQueue;
            if (queue != null) {
                if (queue.size() == 0) {
                    b = false;
                    return b;
                }
            }
        }
        b = true;
        return b;
    }

    @Override
    public int getMaxBufferSize() {
        return 0;
    }

    @Override
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        this.isSpeaking = true;
        ThreadExtKt.mainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (callBack != null) {
                    callBack.onAbilityBegin();
                }
                return Unit.INSTANCE;
            }
        });
        if (fileHandler != null) {
            fileHandler.obtainMessage(0).sendToTarget();
        }
        return 0;
    }

    @Override
    public int audioAvailable(byte[] buffer, int offset, int length) {
        if (buffer != null && isSpeaking) {
            AudioTrack track = this.audioTrack;
            if (track != null) {
                if (track.getPlayState() == 3) {
                    track.write(buffer, offset, length);
                }
            }
            if (fileHandler != null) {
                fileHandler.obtainMessage(1, buffer).sendToTarget();
            }
        }
        return 0;
    }

    @Override
    public int done() {
        this.isSpeaking = false;
        ThreadExtKt.mainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (callBack != null) {
                    callBack.onAbilityEnd();
                }
                return Unit.INSTANCE;
            }
        });
        return 0;
    }

    @Override
    public void error() {

    }

    @Override
    public void error(int errorCode) {
        this.isSpeaking = false;
        ThreadExtKt.mainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (callBack != null) {
                    callBack.onAbilityError(errorCode, new Throwable("TTS error"));
                }
                return Unit.INSTANCE;
            }
        });
    }

    @Override
    public boolean hasStarted() {
        return false;
    }

    @Override
    public boolean hasFinished() {
        return false;
    }


    /**
     * 创建合成音频保存的文件
     */
    private void createFile() {
        String saveFolder = SPUtil.INSTANCE.getString("audio_folder_path", "");
        if (TextUtils.isEmpty(saveFolder)) {
            String absolutePath = Objects.requireNonNull(ContextHolder.Companion.getCONTEXT().getExternalCacheDir()).getAbsolutePath();
            recordFile = new File(
                    absolutePath,
                    System.currentTimeMillis() + ".wav"
            );
        } else {
            recordFile = new File(
                    saveFolder,
                    System.currentTimeMillis() + ".wav"
            );
        }
        try {
            PcmUtil.changeWavHead(recordFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final void writeToFile(byte[] data) {
        try {
            File file = this.recordFile;
            if (file != null) {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(data);
                fos.flush();
                fos.close();
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }
}
