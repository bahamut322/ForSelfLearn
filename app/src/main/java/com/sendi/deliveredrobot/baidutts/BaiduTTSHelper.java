package com.sendi.deliveredrobot.baidutts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;
import com.sendi.deliveredrobot.MyApplication;
import com.sendi.deliveredrobot.baidutts.control.InitConfig;
import com.sendi.deliveredrobot.baidutts.control.MySyntherizer;
import com.sendi.deliveredrobot.baidutts.listener.MessageListener;
import com.sendi.deliveredrobot.baidutts.util.Auth;
import com.sendi.deliveredrobot.baidutts.util.IOfflineResourceConst;
import com.sendi.deliveredrobot.baidutts.util.OfflineResource;
import com.sendi.deliveredrobot.entity.entitySql.QuerySql;
import com.sendi.deliveredrobot.helpers.AudioMngHelper;
import com.sendi.deliveredrobot.helpers.MediaPlayerHelper;
import com.sendi.deliveredrobot.navigationtask.RobotStatus;
import com.sendi.deliveredrobot.utils.LogUtil;
import com.sendi.deliveredrobot.view.widget.Order;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaiduTTSHelper {

    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected String sn; // 纯离线合成SDK授权码；离在线合成SDK没有此参数

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； TtsMode.OFFLINE 纯离线合成，需要纯离线SDK
    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;

    protected boolean isOnlineSDK = TtsMode.ONLINE.equals(IOfflineResourceConst.DEFAULT_SDK_TTS_MODE);

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_vXXXXXXX.dat为离线男声模型文件；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_vXXXXX.dat为离线女声模型文件;
    // assets目录下bd_etts_common_speech_yyjw_mand_eng_high_am-mix_vXXXXX.dat 为度逍遥模型文件;
    // assets目录下bd_etts_common_speech_as_mand_eng_high_am_vXXXX.dat 为度丫丫模型文件;
    // 在线合成sdk下面的参数不生效
    protected String offlineVoice = OfflineResource.VOICE_DUYY;

    protected MySyntherizer synthesizer;

    private final Context context;

    @SuppressLint("StaticFieldLeak")
    private static BaiduTTSHelper instance;
    public static BaiduTTSHelper getInstance(){
        if (instance == null) {
            instance = new BaiduTTSHelper(MyApplication.Companion.getInstance());
        }
        return instance;
    }

    //通过构造函数初始化百度账号相关配置
    public BaiduTTSHelper(Context context) {
        this.context = context;
        try {
            Auth.getInstance(context);
        } catch (Auth.AuthCheckException e) {
            e.printStackTrace();
            return;
        }
        appId = Auth.getInstance(context).getAppId();
        appKey = Auth.getInstance(context).getAppKey();
        secretKey = Auth.getInstance(context).getSecretKey();
        sn = Auth.getInstance(context).getSn();
        LogUtil.INSTANCE.d(sn);
        initialTts(); // 初始化TTS引擎
        if (!isOnlineSDK) {
            LogUtil.INSTANCE.i("BaiduTTSHelper so version:" + SynthesizerTool.getEngineInfo());
        }
    }

    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new MessageListener();
        InitConfig config = getInitConfig(listener,getParams());
        synthesizer = new MySyntherizer(context, config); // 此处可以改为MySyntherizer 了解调用过程
    }

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener,Map<String, String> params) {
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        if (sn == null) {
            initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
        }
        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
//        AutoCheck.getInstance(context.getApplicationContext()).check(initConfig, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == 100) {
//                    AutoCheck autoCheck = (AutoCheck) msg.obj;
//                    synchronized (autoCheck) {
//                        String message = autoCheck.obtainDebugMessage();
//                        LogUtil.INSTANCE.w("AutoCheckMessage "+message);
//                    }
//                }
//            }
//
//        });
        return initConfig;
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "4");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "7");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        if (!isOnlineSDK) {
            // 在线SDK版本没有此参数。

            /*
            params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
            // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。
            */
            // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
            OfflineResource offlineResource = createOfflineResource(offlineVoice);
            // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        }
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            LogUtil.INSTANCE.e("BaiduTTSHelper 【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void speak(String text) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(text)) {
            text = "没有指定名称的话，小迪不知道怎么走啦";
        }
        Order.setFlage("1");
        //播报语音音量
        MediaPlayerHelper.getInstance().pause();
        RobotStatus.INSTANCE.getIdentifyFace().postValue(0);
        new AudioMngHelper(context).setVoice100(QuerySql.QueryBasic().getVoiceVolume());
        int result = synthesizer.speak(text);
//        LogUtil.INSTANCE.i(text);
    }

    public void stop(){
        synthesizer.stop();
        Order.setFlage("0");
        MediaPlayerHelper.getInstance().resume();
    }

    public void pause(){
        Order.setFlage("0");
        synthesizer.pause();
    }

    public void speaks(String text, String utteranceId) {
        Order.setFlage("1");
        MediaPlayerHelper.getInstance().pause();
        RobotStatus.INSTANCE.getIdentifyFace().postValue(0);
        new AudioMngHelper(context).setVoice100(QuerySql.QueryBasic().getVoiceVolume());//设置语音音量
        synthesizer.speak(text, utteranceId);

    }

    public void setParam(Map<String, String> params, String voiceType){
//        synthesizer.setParams(params);
        synthesizer.release();
        OfflineResource offlineResource = createOfflineResource(voiceType);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        SpeechSynthesizerListener listener = new MessageListener();
        InitConfig config = getInitConfig(listener, params);
        synthesizer = new MySyntherizer(context, config); // 此处可以改为MySyntherizer 了解调用过程
    }
    public void resume(){
        Order.setFlage("1");
        synthesizer.resume();
    }
    public void resetParam(){
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "4");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "7");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        setParam(params, offlineVoice);
    }
}




