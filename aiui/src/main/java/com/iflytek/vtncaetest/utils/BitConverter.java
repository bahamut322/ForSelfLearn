package com.iflytek.vtncaetest.utils;

/**
 * 音频格式16bit和32bit相互转换
 */
public class BitConverter {
    public static int outputLength = 0;
    public static byte[] output;

    /*
    input: 任意声道-16bit
    output:任意声道-32bit
    输出与输入的数据格式：
    i=0,2,4...
    output[2*i] = 0;
    output[2*i+1] = 0;
    output[2*i+2] = input[i];
    output[2*i+3] = input[i+1]
*/
    public static byte[] Bit16ToBit32(byte[] input) {
        //output只初始化一次，避免重复创建对象
        if (outputLength == 0) {
            outputLength = input.length * 2; //数据从16bit变为32bit ，处理后数据量变成2倍
            output = new byte[outputLength];
        }

        //每次循环复制input的2byte到output
        for (int i = 0; i < input.length; i += 2) {
            output[2 * i + 2] = input[i];
            output[2 * i + 3] = input[i];
        }
        return output;
    }

    /*
     input: 任意声道-32bit
     output:任意声道-16bit
     输出与输入的数据格式：
     i=0,4,8...
     丢弃 = input[i];
     丢弃 = input[i+1];
     output[i/2] = input[i+2];
     output[i/2+1] = input[i+3]
    */
    public static byte[] Bit32ToBit16(byte[] input) {
        //output只初始化一次，避免重复创建对象
        if (outputLength == 0) {
            outputLength = input.length / 2; //数据从32bit变为16bit ，处理后数据量变成1/2
            output = new byte[outputLength];
        }

        //每次循环4byte,丢弃2byte,复制2byte到output
        for (int i = 0; i < input.length; i += 4) {
            output[i / 2] = input[i + 2];
            output[i / 2 + 1] = input[i + 3];
        }
        return output;
    }
}
