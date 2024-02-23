package com.iflytek.vtncaetest.utils;

/**
 * 延迟声道音频,适用于16bit音频，示例：AudioDelay.convert(audio,4,"0,2",300);
 * audio:输入音频数据
 * 4:输入音频声道数量
 * ”0,2":延迟第1和第3声道
 * 300:延迟时间，单位ms
 */
public class AudioDelay {
    private static final int BytesPerSample = 2;   //2(16bit数据) 4(32bit数据)
    private static final int SampleRatePerMs = 16; // 采样率 16k/s = 16/ms
    private static int[] channelTag;                //延迟的通道标号
    private static byte[][] tmpData;               //缓存的音频
    private static int[] tmpDataIndex;             //输出音频保留的声道
    private static int inputLength = 0;            //输入数据长度
    private static int originSample;               //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
    private static int delayChannelNums;           //延迟到声道数量
    private static int inputDataLengthPerSample;   //1sample输入数据的字节数
    private static int bufferSizePerChannel;       //延迟数据的缓存

    /**
     * 对具体声道进行延迟，示例：AudioDelay.convert(audio,4,"0,2",300);
     *
     * @param inputData         输入音频数据
     * @param inputChannel      输入音频声道数量
     * @param filterParams      要延迟的通道编号，例如"0,2"表示延迟第1和第3声道
     * @param delayMilliseconds 延迟时间，单位ms
     */
    public static void convert(byte[] inputData, int inputChannel, String filterParams, int delayMilliseconds) {
        //初始化一次，避免重复创建
        if (inputLength!= inputData.length) {
            inputLength = inputData.length;
            String[] channels = filterParams.split(",");
            //存储延迟的通道序号
            channelTag = new int[channels.length];
            //初始化队列为空数据
            bufferSizePerChannel = delayMilliseconds * SampleRatePerMs * BytesPerSample;
            for (int i = 0; i < channels.length; i++) {
                channelTag[i] = Integer.parseInt(channels[i]);
                //用环形数组存数据
                tmpData = new byte[inputChannel][bufferSizePerChannel];
                tmpDataIndex = new int[inputChannel];
            }
            //要延迟的通道数
            delayChannelNums = channelTag.length;
            //音频Sample数量，每个通道为1个sample，每1通道16bit数据有2bytes
            originSample = inputData.length / (inputChannel * BytesPerSample);
            inputDataLengthPerSample = inputChannel * BytesPerSample;
        }

        //一次处理一个通道所有数据
        for (int i = 0; i < delayChannelNums; ++i) {
            int channelId = channelTag[i];
            //遍历1个通道的每个sample值
            for (int j = 0; j < originSample; ++j) {
                int srcPos = inputDataLengthPerSample * j + channelId * BytesPerSample;

                //取出缓存数据，避免被覆盖
                byte temp1 = tmpData[channelId][tmpDataIndex[channelId]];
                byte temp2 = tmpData[channelId][tmpDataIndex[channelId] + 1];
                //原始数据放到缓存
                tmpData[channelId][tmpDataIndex[channelId]] = inputData[srcPos];
                tmpData[channelId][tmpDataIndex[channelId] + 1] = inputData[srcPos + 1];

                //缓存数据覆盖原始数据
                inputData[srcPos] = temp1;
                inputData[srcPos + 1] = temp2;

                //环形数组下标递增，超过最大值后复位
                tmpDataIndex[channelId] += 2;
                if (tmpDataIndex[channelId] >= bufferSizePerChannel) {
                    tmpDataIndex[channelId] = 0;
                }
            }
        }
    }
}
