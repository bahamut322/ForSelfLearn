package com.iflytek.vtncaetest.utils;

/**
 * 降采样算法
 */
public class DownSample {

    private static byte[] outputData;         //输出数据
    private static int inputLength = 0;         //输入数据长度

    public static byte[] convert_48kTo16K(byte[] inputData) {
        //避免重复创建
        if (outputData == null || inputLength != inputData.length) {
            if ((inputData.length % 3) != 0) {
                throw new Error("输入数据不是3的整数倍，48k无法降采样为16k");
            } else {
                outputData = new byte[inputData.length / 3];
            }
        }

        //每3个点取1个点
        for (int i = 0; i < inputData.length; i += 6) {
            outputData[i / 3] = inputData[i];
            outputData[i / 3 + 1] = inputData[i + 1];
        }
        return outputData;
    }
}
