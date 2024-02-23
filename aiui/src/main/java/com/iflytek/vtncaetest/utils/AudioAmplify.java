package com.iflytek.vtncaetest.utils;

/**
 * 音频振幅放大和直流偏置调整
 */
public class AudioAmplify {

    private static final int BytesPerSample = 2;   //2(16bit数据) 4(32bit数据)
    private static int[] channelTag;               //延迟的通道标号
    private static int inputLength = 0;            //输入数据长度
    private static int originSample;               //音频Sample数量，每n个通道为1个sample，每1通道16bit数据有2bytes
    private static int processChannelNums;         //放大的声道数量
    private static int inputDataLengthPerSample;   //1sample输入数据的字节数




    /**
     * 放大或缩小指定声道的音量，只能处理16bit格式
     * @param input        输入音频数据,放大后的音频会直接覆盖输入的数据
     * @param multiple     放大倍数，如1.5f
     * @param inputChannel 输入通道数
     * @param processChannel 要放大或缩小的声道编号,从0开始计数，例如“0,2”表示操作第1和第3声道
     * @apiNote 示例： AudioAmplify.amplify(audio, 1.5f,4,"0,2");
     */
    public static void amplify(byte[] input, float multiple, int inputChannel, String processChannel) {
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
                amplifyOneSample(input, srcPos, multiple);
            }
        }
    }


    /**
     * 放大或缩小所有声道的音量，只能处理16bit格式
     *
     * @param input    输入音频数据,放大后的音频会直接覆盖输入的数据
     * @param multiple 放大倍数，如1.5f
     * @apiNote 示例： AudioAmplify.amplify(audio, 1.5f);
     */
    public static void amplifyAll(byte[] input, float multiple) {
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次，用局部变量提高效率
            amplifyOneSample(input, i, multiple);
        }
    }

    /**
     * 对1个声道的1个sample调整增益，1个sample有2个字节
     *
     * @param input    音频数据
     * @param srcPos   数据位置
     * @param multiple 放大倍数
     */
    private static void amplifyOneSample(byte[] input, int srcPos, float multiple) {
        int volume = byteToShort(input, srcPos);//从short类型获取int类型的音频采样值(音量大小)
        volume = (int) (volume * multiple);//计算调整后的音量
        if (volume < -32767) {
            volume = -32767; //爆音的处理，16bit最小值-32767
        } else if (volume > 32767) {
            volume = 32767; //爆音的处理，16bit最大值32767
        }
        shortToByte(input, srcPos, volume);
    }


    /**
     * 调节音频直流偏置，只处理16bit数据
     * @param input 原始音频
     * @param value 偏移幅度，正值表示增加，负值表示相减
     * @apiNote 示例： AudioAmplify.adjustValue(audio, 200);
     */
    public static void adjustValue(byte[] input, int value) {
        for (int i = 0, inputLenth = input.length; i < inputLenth; i += 2) {//每2字节循环一次
            int volume = byteToShort(input, i);//从short类型获取int类型的音频采样值(音量大小)
            volume = volume + value;//计算调整后的音量
            if (volume < -32767) {
                volume = -32767; //爆音的处理，16bit最小值-32767
            } else if (volume > 32767) {
                volume = 32767; //爆音的处理，16bit最大值32767
            }
            shortToByte(input, i, volume);
        }
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

