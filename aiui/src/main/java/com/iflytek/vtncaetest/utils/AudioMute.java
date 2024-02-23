package com.iflytek.vtncaetest.utils;

/**
 * 采样值低于threshold的音频全部设置为0，只能处理16bit格式
 */
public class AudioMute {

    private static final int BytesPerSample = 2;   //2(16bit数据) 4(32bit数据)
    private static int[] channelTag;               //处理的通道标号
    private static int inputLength = 0;            //输入数据长度
    private static int originSample;               //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
    private static int processChannelNums;         //处理的声道数量
    private static int inputDataLengthPerSample;   //1sample输入数据的字节数

    /**
     * 采样值低于threshold的音频全部设置为0，只能处理16bit格式
     * @param input        输入音频数据,放大后的音频会直接覆盖输入的数据
     * @param threshold    低于threshold的音频都会被设置为0
     * @param inputChannel 输入通道数
     * @param processChannel 要处理声道编号,从0开始计数，例如“0,2”表示操作第1和第3声道
     * @apiNote 示例： AudioMute.convert(audio, 150,4,"0,2");
     */
    public static void convert(byte[] input, int threshold, int inputChannel, String processChannel) {
        //初始化一次，避免重复创建
        if (inputLength!=input.length) {
            inputLength = input.length;
            String[] channels = processChannel.split(",");
            //存储放大的通道序号
            channelTag = new int[channels.length];
            for (int i = 0; i < channels.length; i++) {
                channelTag[i] = Integer.parseInt(channels[i]);
            }
            //要处理的通道数
            processChannelNums = channelTag.length;
            originSample = input.length / (inputChannel * BytesPerSample);
            inputDataLengthPerSample = inputChannel * BytesPerSample;
        }

        //一次处理一个通道所有数据
        for (int i = 0; i < processChannelNums; ++i) {
            int channelId = channelTag[i];
            //遍历1个通道的每个sample值
            for (int j = 0; j < originSample; ++j) {
                //1sample有2byte，找到每个sample下标
                int srcPos = inputDataLengthPerSample * j + channelId * BytesPerSample;
                int volum = getShort(input, srcPos);//从short类型获取int类型的音频采样值(音量大小)
                if (Math.abs(volum) < threshold) {
                    input[srcPos] = 0x00;
                    input[srcPos + 1] =0x00;
                }
            }
        }
    }

    /**
     * 将byte数值转换成short类型
     *
     * @param audio 要处理的音频数据
     * @param index 音频数据索引位置
     * @return
     */
    private static int getShort(byte[] audio, int index) {
        return ((audio[index] & 0xFF) | (audio[index + 1] << 8));
    }
}

