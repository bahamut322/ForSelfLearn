package com.iflytek.vtncaetest.utils;

/**
 * 音频通道过滤
 * 示例：AudioFilter.convert(audio,8,"7,-1")：
 * audio：输入音频
 * 8：输入8声道
 * "7,-1"：声道从0开始计算，输出一共2个声道，输出的0号声道数据来源于输入的7号声道，输出的1号声道数据为空(-1表示空数据)
 */
public class AudioFilter {
    private static final int BytesPerSample = 2; //2(16bit数据) 4(32bit数据)
    private static int[] needChannels;        //输出音频保留的声道
    private static int inputLength = 0;         //输入数据长度
    private static String filterParams = "";    //要处理的通道编号，示例“0，1，2，3”
    private static byte[] outputData;         //输出数据
    private static int originSample;         //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
    private static int outputChannel;         //过滤后通道
    private static int outputDataLengthPerSample;      //1sample输出数据的字节数
    private static int inputDataLengthPerSample;       //1sample输入数据的字节数

    /**
     * 示例1：AudioFilter.convert(audio,8,"7,-1")：输入8声道，保留2声道"7,-1"，输出的0号声道数据来源于输入的7号声道，输出的1号声道数据为空
     * 示例2：AudioFilter.convert(audio,5,"0,3,-1")：输入5声道，保留3声道"0,3,-1"，输出的0号声道来源于输入0号声道，输出的1号声道来源于输入的3号声道，输出的2号声道数据为空
     *
     * @param inputChannel 输入音频声道数量
     * @param filterParams 填输出的声道，-1表示空数据。
     */
    public static byte[] convert(byte[] inputData, int inputChannel, String filterParams) {
        //初始化一次，避免重复创建
        if (!AudioFilter.filterParams.equals(filterParams)) {
            AudioFilter.filterParams = filterParams;
            String[] channels = filterParams.split(",");
            needChannels = new int[channels.length];
            for (int i = 0; i < channels.length; i++) {
                needChannels[i] = Integer.parseInt(channels[i]);
            }
            //过滤后通道数
            outputChannel = needChannels.length;
            //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
            originSample = inputData.length / (inputChannel * BytesPerSample);

            inputDataLengthPerSample = inputChannel * BytesPerSample;
            outputDataLengthPerSample = outputChannel * BytesPerSample;

        }

        //初始化一次，避免重复创建数组outputData
        if (inputLength != inputData.length) {
            inputLength = inputData.length;
            //数据长度= 单通道长度(inputData.length/ maxChannel) * 保留的通道数(outputChannel)
            outputData = new byte[inputData.length / inputChannel * outputChannel];
        }

        for (int i = 0; i < outputChannel; ++i) {
            int channelId = needChannels[i];
            //配置-1就不处理输入数据，输出数据默认为0
            if (channelId < 0) {
                continue;
            }
            //将输入数据填充到输出
            for (int j = 0; j < originSample; ++j) {
                int resultPos = outputDataLengthPerSample * j + i * BytesPerSample;
                int srcPos = inputDataLengthPerSample * j + channelId * BytesPerSample;
                outputData[resultPos] = inputData[srcPos];
                outputData[resultPos + 1] = inputData[srcPos + 1];
            }
        }
        return outputData;
    }
}
