package com.iflytek.vtncaetest.utils;

/**
 * 过滤tts音频中有的静音音频。例如5s音频，1s是静音段，过滤后是4s
 */
public class TtsFilterUtil {

    static final int Threshold = 500;//只保留音频采样绝对值高于Threshold的数据
    static byte[] tempArray; //存放符合要求的数据，不符合要求的数据丢弃
    static int waitSample; //单声道音频，1ms有16帧，需要等待一定时间才认为是尾部的tts
    static int remainingSample; //单声道音频，1ms有16帧，需要等待一定时间才认为是尾部的tts
    static int ttsArrayLength = 50000;//根据实际情况选择数组大小

    /**
     * @param input      输入的tts数据
     * @param milisecond 静音后继续等待一定的时间(ms)才认为真正静音，避免误删除有效数据
     */
    public static byte[] filter(byte[] input, int milisecond) {

        //初始化数据
        if (tempArray == null) {
            tempArray = new byte[ttsArrayLength]; //tts静音后，还剩下多久才能判断是尾部tts
            waitSample = 16 * milisecond; //单声道音频，1ms有16帧，需要等待一定时间才认为是尾部的tts
            remainingSample = waitSample; //防止去掉正常说话中间的静音段，等待remainingSample后开始裁剪静音音频
        }


        int resultIndex = 0;
        //2个byte是1个采样点，每个采样点都处理一次
        for (int i = 0; i < input.length; i += 2) {
            int volum = getShort(input, i);
            //保留幅度超过MIN的数据
            if (Math.abs(volum) > Threshold) {
                remainingSample = waitSample;
                tempArray[resultIndex] = input[i];
                tempArray[resultIndex + 1] = input[i + 1];
                resultIndex += 2;
            } else {
                //remainingSample>0.幅度低于MIN，此时认为还是正常说话间隔，但是开始倒计时。
                if (remainingSample > 0) {
                    remainingSample--;
                    tempArray[resultIndex] = input[i];
                    tempArray[resultIndex + 1] = input[i + 1];
                    resultIndex += 2;
                }
            }
        }
        byte[] result = new byte[resultIndex]; //储存最终的数据
        System.arraycopy(tempArray, 0, result, 0, resultIndex);
        return result;
    }

    /**
     * 将byte数值转换成short类型
     */
    private static short getShort(byte[] src, int start) {
        return (short) ((src[start] & 0xFF) | (src[start + 1] << 8));
    }
}

