package com.iflytek.aikitdemo.media.audio;

import static java.lang.Math.abs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.iflytek.aikitdemo.ContextHolder;
import com.iflytek.aikitdemo.tool.ThreadExtKt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @Desc:
 * @Author leon
 * @Date 2023/9/25-14:29
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
public class AudioRecorderV2 implements Recorder {


    private static int SAMPLE_RATE_IN_HZ = 16000;
    private static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static String TAG = "AudioRecorder";

    private AudioRecord audioRecord;
    private File recordFile;
    private Thread recordingThread;
    private int bufferSize = 0;
    private RecorderCallback recordCallback;

    private AtomicBoolean isAudioRecording = new AtomicBoolean(false);
    private AtomicBoolean isAudioPaused = new AtomicBoolean(false);
    private Handler fileHandler;

    private AudioRecorderV2() {
    }

    private static class Holder {
        private static final AudioRecorderV2 instance = new AudioRecorderV2();
    }

    public static AudioRecorderV2 getInstance() {
        return Holder.instance;
    }

    @SuppressLint("MissingPermission")
    public void init() {
        if (null != audioRecord) {
            audioRecord.release();
        }
        try {
            bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_IN_HZ,
                    CHANNEL_CONFIGURATION,
                    AUDIO_FORMAT
            );
            audioRecord = new AudioRecord(
                    AUDIO_SOURCE,
                    SAMPLE_RATE_IN_HZ,
                    CHANNEL_CONFIGURATION,
                    AUDIO_FORMAT,
                    bufferSize
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (audioRecord != null && audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            try {
                throw new AudioRecordException("初始化录音失败");
            } catch (AudioRecordException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setRecorderCallback(@Nullable RecorderCallback callback) {
        this.recordCallback = callback;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Override
    public void startRecording() {
        if (audioRecord == null) return;
        int state = audioRecord.getState();
        switch (state) {
            case AudioRecord.STATE_INITIALIZED: {
                try {
                    audioRecord.startRecording();
                    initRecordInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            case AudioRecord.STATE_UNINITIALIZED: {
                init();
                audioRecord.startRecording();
                initRecordInfo();
            }

            default: {

            }
        }
    }

    private void initRecordInfo() {
        isAudioRecording.set(true);
        isAudioPaused.set(false);
        recordFile = new File(
                ContextHolder.Companion.getCONTEXT().getCacheDir().getAbsolutePath(),
                System.currentTimeMillis() + ".pcm"
        );
        recordingThread = new Thread(new RecordThread(), "RecordThread");
        try {
            recordingThread.start();
            ThreadExtKt.mainThread(() -> {
                recordCallback.onStartRecord();
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            try {
                throw new AudioRecordException("录音失败");
            } catch (AudioRecordException ex) {
                throw new RuntimeException(ex);
            }
        }
        HandlerThread handlerThread = new HandlerThread("writeFile");
        handlerThread.start();
        fileHandler = new Handler(handlerThread.getLooper(), msg -> {
            Pair<FileOutputStream, byte[]> pair = (Pair<FileOutputStream, byte[]>) msg.obj;
            writeToFile(pair.first, pair.second);
            return true;
        });
    }

    @Override
    public void resumeRecording() {
        if (audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            if (isAudioPaused.get()) {
                audioRecord.startRecording();
                ThreadExtKt.mainThread(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        recordCallback.onResumeRecord();
                        return Unit.INSTANCE;
                    }
                });
                isAudioPaused.set(false);
            }
        }
    }

    @Override
    public void pauseRecording() {
        if (audioRecord != null && isAudioRecording.get()) {
            audioRecord.stop();
            isAudioPaused.set(true);
            ThreadExtKt.mainThread(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    recordCallback.onPauseRecord();
                    return Unit.INSTANCE;
                }
            });
        }
    }

    @Override
    public void stopRecording() {
        if (audioRecord != null) {
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                try {
                    audioRecord.stop();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "stopRecording() problems", e);
                }
            }
            audioRecord.release();
            recordingThread.interrupt();
            isAudioRecording.set(false);
            isAudioPaused.set(false);
            ThreadExtKt.mainThread(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    recordCallback.onStopRecord(recordFile);
                    return Unit.INSTANCE;
                }
            });
        }
    }

    @Override
    public boolean isRecording() {
        return isAudioRecording.get();
    }

    @Override
    public boolean isPaused() {
        return isAudioPaused.get();
    }


    class RecordThread implements Runnable {

        @Override
        public void run() {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(recordFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            int scoringBufferMaxSize = bufferSize;
            byte[] audioData = new byte[scoringBufferMaxSize];
            while (isRecording()) {
                boolean localPaused = isPaused();
                if (localPaused) {
                    continue;
                }
                int audioSampleSize = getAudioRecordBuffer(
                        scoringBufferMaxSize, audioData
                );
                if (audioSampleSize > 0) {
                    int x = (int) (abs((float) audioData[0]) / Short.MAX_VALUE);
                    int recordVolume = ((2 * x - x * x) * 9);
                    if (audioSampleSize == scoringBufferMaxSize) {
                        recordCallback.onRecordProgress(audioData, audioSampleSize, recordVolume);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = new Pair(fos, audioData);
                        fileHandler.sendMessage(message);
                    } else {
                        byte[] copy = new byte[audioSampleSize];
                        System.arraycopy(audioData, 0, copy, 0, audioSampleSize);
                        recordCallback.onRecordProgress(copy, audioSampleSize, recordVolume);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = new Pair(fos, audioData);
                        fileHandler.sendMessage(message);
//                        writeToFile(fos, copy)
                    }
                }
            }
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    private void writeToFile(FileOutputStream fos, byte[] data) {
        if (fos == null) return;
        try {
            fos.write(data);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
    }

    private int getAudioRecordBuffer(
            int scoringBufferMaxSize,
            byte[] audioSamples
    ) {
        int read = audioRecord.read(
                audioSamples,
                0,
                scoringBufferMaxSize
        );
        return read;
    }


}
