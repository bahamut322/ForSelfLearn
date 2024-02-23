package com.iflytek.vtncaetest.engine;

public interface WakeupListener {

    /**
     * @param angle   唤醒角度
     * @param beam    唤醒波束（不需要设置，sdk内部自动处理）
     * @param score   唤醒得分
     * @param keyWord 唤醒词（拼音格式），例如：xiao3 fei1 xiao3 fei1
     */
    void onWakeup(int angle, int beam, int score, String keyWord);
}
