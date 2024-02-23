package com.iflytek.vtncaetest.utils;

/**
 * 音频振幅放大和直流偏置调整
 */
public class AudioRevert {

    private static final int BytesPerSample = 2;   //2(16bit数据) 4(32bit数据)
    private static int[] channelTag;               //延迟的通道标号
    private static int inputLength = 0;            //输入数据长度
    private static int originSample;               //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
    private static int processChannelNums;         //放大的声道数量
    private static int inputDataLengthPerSample;   //1sample输入数据的字节数


    /**
     * 指定声道反向，比如数值100，反向后是-100。只能处理16bit格式
     *
     * @param input          输入音频数据,放大后的音频会直接覆盖输入的数据
     * @param inputChannel   音频总通道数
     * @param processChannel 要反向的声道编号,从0开始计数，例如“0,2”表示操作第1和第3声道
     * @apiNote 示例： AudioRevert.revert(audio,4,"0,2");
     */
    public static void revert(byte[] input, int inputChannel, String processChannel) {
        //初始化一次，避免重复创建
        if (inputLength != input.length) {
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
                revertOneSample(input, srcPos);
            }
        }
    }


    /**
     * 对1个声道的1个sample反向，1个sample有2个字节
     *
     * @param input  音频数据
     * @param srcPos 数据位置
     */
    private static void revertOneSample(byte[] input, int srcPos) {
        int volume = byteToShort(input, srcPos);//从short类型获取int类型的音频采样值(音量大小)
        volume = -volume;//计算调整后的音量
        shortToByte(input, srcPos, volume);
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

    /**
     * short转byte
     *
     * @param bytes  要处理的音频数据
     * @param offset 音频数据索引位置
     * @param volume 数值
     * @return
     */
    private static void shortToByte(byte[] bytes, int offset, int volume) {
        bytes[offset] = (byte) (volume & 0xFF);
        bytes[offset + 1] = (byte) ((volume >> 8) & 0xFF);
    }
}

