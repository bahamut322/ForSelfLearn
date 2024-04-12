package com.iflytek.aikitdemo.media.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import com.iflytek.aikitdemo.ContextHolder
import com.iflytek.aikitdemo.tool.PcmUtil
import com.iflytek.aikitdemo.tool.SPUtil
import com.iflytek.aikitdemo.tool.mainThread
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * @Desc: 简单的音频播放器
 * @Author leon
 * @Date 2023/3/8-15:49
 * Copyright 2023 iFLYTEK Inc. All Rights Reserved.
 */
class AudioRecorder private constructor() : Recorder {

    private val TAG = "AudioRecorder"

    private var audioRecord: AudioRecord? = null

    private var recordFile: File? = null
    private var recordingThread: Thread? = null
    private var bufferSize = 0
    private var recordCallback: RecorderCallback? = null

    private val isRecording = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private var fileHandler: Handler? = null

    private var focusRequest: AudioFocusRequest? = null

    companion object {

        private const val SAMPLE_RATE_IN_HZ = 16000
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        private const val CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AudioRecorder() }
    }

    @SuppressLint("MissingPermission")
    fun init() {
        if (null != audioRecord) {
            audioRecord?.release()
        }
        try {
            bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_IN_HZ, CHANNEL_CONFIGURATION, AUDIO_FORMAT
            )
            audioRecord = AudioRecord(
                AUDIO_SOURCE, SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIGURATION, AUDIO_FORMAT, bufferSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw AudioRecordException("初始化录音失败")
        }
        initAudioFocus()
    }

    /**
     * 处理音频焦点
     */
    private fun initAudioFocus() {
        val audioManager = ContextHolder.CONTEXT.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener {
                    // 处理音频焦点变化的逻辑
                }
                .build()
            focusRequest?.let { focusRequest ->
                audioManager?.requestAudioFocus(focusRequest)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                object : AudioManager.OnAudioFocusChangeListener {
                    override fun onAudioFocusChange(focusChange: Int) {
                        // 处理音频焦点变化的逻辑
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // 请求成功，开始录音
            Log.d(TAG, "initAudioFocus: 请求成功，开始录音")
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            // 请求失败，提示用户
            Log.d(TAG, "initAudioFocus: 请求失败，提示用户")
        } else if (result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            // 请求延迟，等待回调
            Log.d(TAG, "initAudioFocus: 请求延迟，等待回调")
        }
    }

    override fun setRecorderCallback(callback: RecorderCallback?) {
        recordCallback = callback
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording() {
        when (audioRecord?.state) {
            AudioRecord.STATE_INITIALIZED -> {
                try {
                    audioRecord?.startRecording()
                } catch (e: Exception) {
                    throw AudioRecordException("录音失败")
                }
            }

            AudioRecord.STATE_UNINITIALIZED -> {
                init()
                audioRecord?.startRecording()
            }

            else -> {
                throw AudioRecordException("录音失败")
            }
        }
        isRecording.set(true)
        isPaused.set(false)
        val saveFolder = SPUtil.getString("audio_folder_path", "")
        recordFile = if (TextUtils.isEmpty(saveFolder)) {
            File(
                ContextHolder.CONTEXT.externalCacheDir?.absolutePath ?: "",
                "${System.currentTimeMillis()}.wav"
            )
        } else {
            File(
                saveFolder,
                "${System.currentTimeMillis()}.wav"
            )
        }
        PcmUtil.changeWavHead(recordFile)
        recordingThread = Thread(RecordThread(), "RecordThread")
        try {
            recordingThread?.start()
            mainThread {
                recordCallback?.onStartRecord()
            }
        } catch (e: Exception) {
            throw AudioRecordException("录音失败")
        }
        val handlerThread = HandlerThread("writeFile")
        handlerThread.start()
        fileHandler = Handler(handlerThread.looper, Handler.Callback {
            val data = it.obj as? ByteArray ?: return@Callback false
            writeToFile(data)
            true
        })
    }


    override fun resumeRecording() {
        if (audioRecord != null && audioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
            if (isPaused.get()) {
                audioRecord?.startRecording()
                mainThread {
                    recordCallback?.onResumeRecord()
                }
                isPaused.set(false)
            }
        }
    }

    override fun pauseRecording() {
        if (audioRecord != null && isRecording.get()) {
            audioRecord?.stop()
            isPaused.set(true)
            mainThread {
                recordCallback?.onPauseRecord()
            }
        }
    }

    override fun stopRecording() {
        if (audioRecord != null) {
            if (audioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
                try {
                    audioRecord?.stop()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "stopRecording() problems", e)
                }
            }
            audioRecord?.release()
            recordingThread?.interrupt()
            isRecording.set(false)
            isPaused.set(false)
            mainThread {
                recordCallback?.onStopRecord(recordFile)
            }
        }
    }

    override fun isRecording(): Boolean {
        return isRecording.get()
    }

    override fun isPaused(): Boolean {
        return isPaused.get()
    }

    fun destroy() {
        val audioManager =
            ContextHolder.CONTEXT.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let {
                audioManager.abandonAudioFocusRequest(
                    it
                )
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus({})
        }
    }

    inner class RecordThread : Runnable {

        override fun run() {

            val scoringBufferMaxSize = bufferSize
            val audioData = ByteArray(scoringBufferMaxSize)
            while (isRecording()) {
                val localPaused = isPaused()
                if (localPaused) {
                    continue
                }
                val audioSampleSize = getAudioRecordBuffer(
                    scoringBufferMaxSize, audioData
                )
                if (audioSampleSize > 0) {
                    val x =
                        abs(audioData[0].toInt()).toFloat() / Short.MAX_VALUE
                    val recordVolume = ((2 * x - x * x) * 9).roundToInt()
                    if (audioSampleSize == scoringBufferMaxSize) {
                        recordCallback?.onRecordProgress(audioData, audioSampleSize, recordVolume)
                        val message = Message()
                        message.what = 1
                        message.obj = audioData
                        fileHandler?.sendMessage(message)
                    } else {
                        val copy = ByteArray(audioSampleSize)
                        System.arraycopy(audioData, 0, copy, 0, audioSampleSize)
                        recordCallback?.onRecordProgress(copy, audioSampleSize, recordVolume)
                        val message = Message()
                        message.what = 1
                        message.obj = audioData
                        fileHandler?.sendMessage(message)
                    }
                }
            }

        }
    }

    private fun writeToFile(data: ByteArray) {
        try {
            //将data写入文件recordFile
            recordFile?.let {
                val fos = FileOutputStream(it, true)
                fos.write(data)
                fos.flush()
                fos.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun splicingUrl(parentUri: String, fileName: String) = if (parentUri.endsWith(":")) {
        parentUri + fileName
    } else {
        "$parentUri/$fileName"
    }

    private fun getAudioRecordBuffer(
        scoringBufferMaxSize: Int,
        audioSamples: ByteArray
    ): Int {
        return audioRecord?.read(
            audioSamples,
            0,
            scoringBufferMaxSize
        ) ?: 0
    }

}