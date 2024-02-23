package com.iflytek.vtncaetest.utils;

import android.util.Log;

public class AcousticsTestUtil {
    private static final String TAG = AcousticsTestUtil.class.getSimpleName();

    /**
     * 检测截幅
     */
    public static void clippingTest(byte[] input, int chanelNumber) {
        int lastvolum = 0;
        int count = 0;  //存储连续几次音量差值小于一定值
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次，用局部变量提高效率
            int volum = getShort(input, i);//从short类型获取int类型的音频采样值(音量大小)
            //波形截幅测试，可能回采传过来的值幅度不大，但是波形已经截幅了
            if (volum <= -2000 || volum >= 2000) {
                if (Math.abs(lastvolum - volum) < 1500) {
                    count++;
                    //连续5个采样点差值小于1500(根据经验选定)，认为波形截幅
                    if (count == 4) {
                        count = 0;
                        //当前第几个采样点
                        int sample = i / 2;
                        //当前第几帧，1帧有多个采样点,x通道有x个采样点
                        long clippingFrame = sample / chanelNumber;
                        //截幅的时间,单位ms,因为采样率是16k/s,对应16帧/ms
                        long clippingTime = clippingFrame / 16;
                        //当前声道，声道从1开始计算
                        int channelCurrent = (inputLenth / 2) % chanelNumber + 1;
                        Log.e(TAG, "截幅时间" + clippingTime + "ms  采样值" + volum + " 声道" + channelCurrent);
                    }
                }
            }
            //保存历史音量，方便与下一次音量做比较，计算波形截幅
            lastvolum = volum;

            //最大值截幅测试，最大范围是-32767~32766,但是截幅要留一定的余量，缩小范围变成-32000~32000
            if (volum <= -32000 || volum >= 32000) {
                //当前第几个采样点
                int sample = i / 2;
                //当前第几帧，1帧有多个采样点,x通道有x个采样点
                long clippingFrame = sample / chanelNumber;
                //截幅的时间,单位ms,因为采样率是16k/s,对应16帧/ms
                long clippingTime = clippingFrame / 16;
                //当前声道，声道从1开始计算
                int channelCurrent = (inputLenth / 2) % chanelNumber + 1;
                Log.e(TAG, "截幅时间" + clippingTime + "ms  采样值" + volum + " 声道" + channelCurrent);
            }
        }
    }


    /**
     * 检测有无声音
     */
    public static void voiceTest(byte[] input) {
        int lastvolum = 0;
        int count = 0;  //存储连续几次音量差值小于一定值
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次，用局部变量提高效率
            int volum = getShort(input, i);//从short类型获取int类型的音频采样值(音量大小)
            //人喊的时候，采样点差值不断波动，说明麦克风能正常接收到输入信号
            if (Math.abs(lastvolum - volum) > 1500) {
                count++;
            }
            //保存历史音量，方便与下一次音量做比较，计算波形截幅
            lastvolum = volum;

            //最后一个字节判断是否有声音，累计20次音量波动，认为有声音
            if (i == (inputLenth - 2)) {
                if (count >= 20) {
                    Log.i(TAG, "音量检测通过");
                } else {
                    Log.e(TAG, "音量检测未通过，请检查麦克风或回采是否正常");
                }
            }
        }
    }


    /**
     * 测量输入音频的音量大小，单位db
     *
     * @param input 输入音频
     * @return
     */
    public static double calculateDB(byte[] input) {
        int sum = 0;//存放采样值总和
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次，用局部变量提高效率
            int volum = getShort(input, i);//从short类型获取int类型的音频采样值(音量大小)
            sum += Math.abs(volum);
        }
        int sample = input.length / 2;//采样点数目
        double mean = sum / sample;//采样点平均值
        return 20 * Math.log10(mean); //计算音量db，32767对应90db,数值每减小一半，下降6db，比如16383对应84db
    }

    /**
     * 将byte数值转换成short类型
     */
    private static short getShort(byte[] src, int start) {
        return (short) ((src[start] & 0xFF) | (src[start + 1] << 8));
    }
}

