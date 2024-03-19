package com.iflytek.vtncaetest.engine;

import com.iflytek.aiui.AIUIConstant;

/**
 * 引擎配置参数
 */
public class EngineConstants {


    /**
     * 录音机类型，初始化不同的recorder
     * 0 (单麦，系统录音,录制1mic数据，取系统回声消除后的音频，如果无效果是系统或硬件不支持),代码在SystemRecorder.java
     * 1 (多麦阵列,Alsa单声卡录音),代码在SingleAlsaRecorder.java
     * 2 (多麦阵列,Alsa双声卡录音，mic数据和回采数据不在一个声卡),代码在DoubleAlsaRecorder.java
     * 3 (单麦，系统录音,录制1mic+1回采数据,声道1是MIC,声道2是回采),代码在SystemRecorder.java
     **/
    public final static int recorderType = 0;

    /**
     * alsa录音参数，参考文档：https://www.yuque.com/iflyaiui/zzoolv/fp0444
     * 示例参数：tinycap /sdcard/test.pcm -D 0 -d 0 -c 8 -r 16000 -b 16 -p 1536 -n 8
     * -D card 声卡号
     * -d device 设备号
     * -c channel  通道数
     */
    //*********Alsa单声卡配置，代码在SingleAlsaRecorder.java中*********
    public final static int Card = 4;      //声卡号
    public final static int Device = 0;    //声卡设备号
    public final static int Channel = 8;   //输入音频的声道数量
    public final static boolean changeChannel = false;    //true(对输入音频做处理),false(不处理），搭配channleParams使用
    /**
     * 规则1：填写要保留的原始音频声道号，有几个数字输出音频就有几个声道，第1声道是声道0，第2声道是声道1，以此类推
     * 示例：“1,0”表示输出音频保留原始音频的声道1和0,原始音频的声道1放在输出音频的第1声道，声道0放在输出音频的第2声道
     * 示例：“0,1"表示输出音频保留原始音频的声道0和1,原始音频声道0放在输出音频的第1声道，声道1放在输出音频的第2声道
     * 示例：“2,1,0"表示输出音频保留原始音频的声道2和1,原始音频声道2放在输出音频的第1声道，声道1放在输出音频的第2声道，声道0放在输出音频的第3声道
     * 规则2：-1表示保留声道，但是数据清空
     * 示例：”0,-1"：输出数据保留2个声道，原始音频声道0放在输出音频的第1声道，输出音频的第2声道是空数据
     * 示例："7,-1"：输出数据保留2个声道，原始音频声道7放在输出音频的第1声道，输出音频的第2声道是空数据
     * 示例："5,3,2"：输出数据保留2个声道，原始音频声道5放在输出音频的第1声道，声道3放在输出音频的第2声道，声道2放在输出音频的第3声道
     **/
    public final static String channleParams = "0,1,2,3,6,7";
    //*********单声卡配置*********

    //*********Alsa双声卡配置，代码在DoubleAlsaRecorder.java*********
    public final static int mic_Card = 0;      //声卡号
    public final static int mic_Device = 1;    //声卡设备号
    public final static int mic_Channel = 4;   //mic声道数量
    public final static int ref_Card = 0;      //回采声卡号
    public final static int ref_Device = 1;    //回采声卡设备号
    public final static int ref_Channel = 2;   //回采声道数量
    //*********Alsa双声卡配置，代码在DoubleAlsaRecorder.java*********

    //原始音频音量倍数,默认1.0f
    public static final float rawAudioGain = 1.2f;
    //识别音频音量倍数,默认1.0f
    public static final float asrAudioGain = 1.2f;

    // Alsa-Jni录音模块日志控制 1(debug) 2(info) 3(warn) 4(error) 5(fatal)
    public static final int wakeEngineLogLevel = 4;

    /*aiui唤醒类型，Voice(唤醒引擎送数据给云端识别),TEXT(唤醒引擎不送音频给云端识别，只响应文本请求)
    WakeupEngine.java判断代码：
        if (AIUIConstant.STATE_WORKING == mAIUIState&& AiuiUtils.wakeupType==AiuiUtils.WAKEUPTYPE_VOICE) {
            AiuiEngine.MSG_writeAudio(caeOutputAudio);
        }
     */
    public static int wakeupType = 1;
    //唤醒类型，1(唤醒后引擎送数据给云端识别)
    public static final int WAKEUPTYPE_VOICE = 1;
    //唤醒类型：2(唤醒后引擎不送音频给云端识别，只处理文本语义)
    public static final int WAKEUPTYPE_TEXT = 2;


    //设备sn，需要保证每台设备sn唯一，参考文档：https://www.yuque.com/iflyaiui/zzoolv/tgftb5
    public static String serialNumber;
    // 录音机工作状态
    public static boolean isRecording = false;
    // 写音频线程工作中
    public static boolean isWriting = false;
    // 音频数据保存开关
    public static boolean saveAudio = false;
    //无意义词过滤功能：当前识别结果是否有意义
    public static boolean meaningful = false;

    //保存云端下发的tts音频
    public static boolean saveTTS = false;
    //aiui状态，idle(空闲状态)，ready(待唤醒状态,不能对话)，working(已唤醒状态，可以对话)
    public static int mAIUIState = AIUIConstant.STATE_IDLE;
    //记录初始化次数，ota刷机后设备信息改变，第一次初始化会报错11217，需要再次初始化aiui绑定当前设备
    public static boolean firstInit = true;

    //唤醒资源存储目录，需要与vtn.ini中设置一致
    public static final String wakeupEngineDir = "/sdcard/Android/data/com.iflytek.vtncaetest";
    //ini配置文件名,真实路径是/wakeupEngineDir/iniPath，例如 /sdcard/Android/data/com.iflytek.vtncaetest/vtn-4mic.ini

    public static String iniPath = "vtn-1mic.ini"; //降噪唤醒配置
//    public static String iniPath = "vtn-2mic.ini";
//    public static String iniPath = "vtn-4mic.ini";
//    public static String iniPath = "vtn-4mic-ZBS.ini";//4mic窄波束
//    public static String iniPath = "vtn-8mic.ini";

    public static String sdk_version = "2023-08-15"; //版本号，用日期标注
    public static boolean gainTest = false; //振幅测试
    public static boolean wakeInfoOn = true; //唤醒调试开关，关闭后提高唤醒响应速度，但不抛出角度，得分等唤醒信息

    public static String DenoiseLibName = "libvtn_V317_line_1mic_v7a.so";//唤醒算法so名称，sdk自动到jnilib目录下找
//    public static String DenoiseLibName = "libvtn_V317_line_2mic_v7a_phone.so";
//    public static String DenoiseLibName = "libvtn_V317_line_2mic_v7a_gevd_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_line_2mic_v7a_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_line_4mic_v7a.so";
//    public static String DenoiseLibName = "libvtn_V317_line_4mic_v7a_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_line_4mic_v7a_ZBS_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_line_6mic_v7a.so";
//    public static String DenoiseLibName = "libvtn_V317_line_8mic_v7a_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_round_4mic_v7a_vtn2.0.so";
//    public static String DenoiseLibName = "libvtn_V317_round_6mic_v7a_vtn2.0.so";


    /* 唤醒词+识别文本连说功能，true(开启,直接说小飞小飞今天天气。缺点是单说唤醒词的时候，噪声环境下可能直接结束识别) ，false(关闭，小飞小飞，停顿300ms，再说今天天气)
       说明文档：https://www.yuque.com/iflyaiui/zzoolv/egon7vb08s91qoa2
     */
    public static boolean freeAsr = false;
}
