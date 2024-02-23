package com.iflytek.vtncaetest.utils;

import android.util.Log;

/**
 * 合并多个音频文件
 * 示例：byte[] result= AudioMerge.merge(audio1,1,audio2,2);
 * 说明：audio1(声道a1)
 *      audio2(声道b1,声道b2)
 *      结果(声道a1,声道b1,声道b2)
 */
public class AudioMerge {
    private static final String TAG = AudioMerge.class.getSimpleName();
    private static final int BytesPerSample = 2; //2(16bit数据) 4(32bit数据)
    private static byte[] outputData;            //输出数据
    private static int inputDataLength;          //输入数据长度
    private static int inputSamples;             //输入音频Sample数量，1sample包含多个声道，每1通道16bit数据有2bytes
    private static int inputTotal_channels;      //输入音频，总通道数
    private static int inputTotal_SampleLength;  //汇总通道后，1sample的字节数,1sample包含多个声道
    private static int input1_SampleLength;     //输入音频1,1sample的字节数,1sample包含多个声道
    private static int input2_SampleLength;     //输入音频2,1sample的字节数,1sample包含多个声道


    /**
     * 把2个音频合并成1个音频
     * 示例： byte[] result= AudioMerge.merge(audio1,2,audio2,2);
     * 说明：音频1(声道a1,声道a2) 音频2(声道b1,声道b2),最终音频(a1,a2,b1,b2)
     *
     * @param data1           音频1
     * @param input1_channels 音频1通道数
     * @param data2           音频2
     * @param input2_channels 音频2通道数
     * @return
     */
    public static byte[] merge(byte[] data1, int input1_channels, byte[] data2, int input2_channels) {
        //避免重复new
        if (inputDataLength!=(data1.length + data2.length)) {
            //如果sample不一样，音频无法合并，抛出异常
            if ((data1.length / input1_channels) != (data2.length / input2_channels)) {
                Log.e(TAG, "输入音频sample数量不同，无法合并");
                return null;
            }
            inputDataLength = data1.length + data2.length;
            outputData = new byte[inputDataLength];
            inputTotal_channels = input1_channels + input2_channels;
            inputSamples = inputDataLength / BytesPerSample / inputTotal_channels;
            inputTotal_SampleLength = inputTotal_channels * BytesPerSample;
            input1_SampleLength = input1_channels * BytesPerSample;
            input2_SampleLength = input2_channels * BytesPerSample;
        }

        for (int i = 0; i < inputSamples; ++i) {
            //复制音频1数据
            System.arraycopy(data1, input1_SampleLength * i, outputData, inputTotal_SampleLength * i, input1_SampleLength);
            //复制音频2数据
            System.arraycopy(data2, input2_SampleLength * i, outputData, inputTotal_SampleLength * i + input1_SampleLength, input2_SampleLength);
        }
        return outputData;
    }
}
