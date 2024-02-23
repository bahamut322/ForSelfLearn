package com.iflytek.vtncaetest.utils;

/**
 * 振幅>2000才能较好识别，一句话中要有5个点
 */
public class GainTest {

    public static int calculate(byte[] input) {
        int count = 0;
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次，用局部变量提高效率
            int volume = byteToShort(input, i);//从short类型获取int类型的音频采样值(音量大小)
            if (Math.abs(volume) > 2000) count++;
        }
        return count;
    }

    /**
     * byte转short
     *
     * @param audio 要处理的音频数据
     * @param index 音频数据索引位置
     * @return
     */
    private static int byteToShort(byte[] audio, int index) {
        return ((audio[index] & 0xFF) | (audio[index + 1] << 8));
    }
}

