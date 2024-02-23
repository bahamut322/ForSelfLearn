package com.iflytek.vtncaetest.recorder;


public interface AudioRecorder {
    // 开始录音
    int startRecord();

    // 停止录音
    void stopRecord();

    // 销毁录音机
    void destroyRecord();
}
