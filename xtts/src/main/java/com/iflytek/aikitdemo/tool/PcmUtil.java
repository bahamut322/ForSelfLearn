package com.iflytek.aikitdemo.tool;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * pcm 工具类
 */
public class PcmUtil {

    private final static int WAV_HEADER_LENGTH = 44;

    private final static byte[] RIFF_array = {0x52, 0x49, 0x46, 0x46};
    private final static byte[] WAVE_array = {0x57, 0x41, 0x56, 0x45};
    private final static byte[] fmt_array = {0x66, 0x6D, 0x74, 0x20};
    private final static byte[] data_array = {0x64, 0x61, 0x74, 0x61};

    /**
     * Add wave header at front pcm data.
     *
     * @param pcmDataSize size of pcm data
     * @param channel     pcm channel, e.g. 1 or 2
     * @param sampleRate  pcm sample rate, e.g. 22050, 44100 etc.
     * @param bits        pcm bits, e.g. 8 or 16
     * @return
     */
    public static byte[] getWavHeader(int pcmDataSize, int channel, int sampleRate, int bits) {
        int riffSize = pcmDataSize + WAV_HEADER_LENGTH;
        int chunk = channel * bits / 8;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // Write RIFF header
            stream.write(RIFF_array);
            stream.write(intToByteArray(riffSize));
            stream.write(WAVE_array);

            // Write format header
            stream.write(fmt_array);
            stream.write(intToByteArray(16));
            stream.write(shortToByteArray(1));
            stream.write(shortToByteArray(channel));
            stream.write(intToByteArray(sampleRate));
            stream.write(intToByteArray(sampleRate * chunk));
            stream.write(shortToByteArray(2));
            stream.write(shortToByteArray(bits));

            // Write data section
            stream.write(data_array);
            stream.write(intToByteArray(pcmDataSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    /**
     * Add wave header at front pcm data.
     *
     * @param pcmData    byte array of pcm data
     * @param channel    pcm channel, e.g. 1 or 2
     * @param sampleRate pcm sample rate, e.g. 22050, 44100 etc.
     * @param bits       pcm bits, e.g. 8 or 16
     * @return
     */
    public static byte[] pcm2Wave(byte[] pcmData, int channel, int sampleRate, int bits) {

        byte[] wavHeader = getWavHeader(pcmData.length, channel, sampleRate, bits);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.write(wavHeader);
            dos.write(pcmData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }


    private static byte[] intToByteArray(int value) {
        return new byte[]{(byte) value, (byte) (value >>> 8),
                (byte) (value >>> 16), (byte) (value >>> 24)};
    }

    private static byte[] shortToByteArray(int value) {
        return new byte[]{(byte) value, (byte) (value >>> 8)};
    }

    /**
     * 写入wav头文件
     */
    public static void writeWavHead(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream stream = new FileOutputStream(file);
        FileChannel channel = stream.getChannel();
        channel.write(ByteBuffer.allocate(WAV_HEADER_LENGTH));
        channel.close();
    }

    /**
     * 给wav文件写入数据
     */
    public static void write2wav(File file, byte[] data) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        FileChannel channel = stream.getChannel();
        channel.write(ByteBuffer.wrap(data));
        channel.close();
    }

    /**
     * 给wav文件修改文件头
     */
    public static void changeWavHead(File file) throws IOException {
        changeWavHead(file, 1, 16_000, 16);
    }

    /**
     * 给wav文件修改文件头
     */
    public static void changeWavHead(File file, int channelCount, int sampleRateInHz, int bits) throws IOException {
        RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomFile.getChannel();
        fileChannel.position(0);
        int dataLength = (int) (fileChannel.size() - WAV_HEADER_LENGTH);
        fileChannel.write(ByteBuffer.wrap(getWavHeader(dataLength, channelCount, sampleRateInHz, bits)));
        fileChannel.close();
    }
}
