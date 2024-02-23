package com.iflytek.vtncaetest.engine;

import static com.iflytek.vtncaetest.engine.EngineConstants.serialNumber;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.iflytek.vtncaetest.ContextHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class AiuiEngine {
    private static final int MODEL_USER = 1;

    private static final String TAG = AiuiEngine.class.getSimpleName();
    private volatile static AIUIAgent mAIUIAgent;
    //写入asr音频复用对象降低cpu占用，其他都要重新创建AIUIMessage对象
    public static AIUIMessage asrMsg = new AIUIMessage(0, 0, 0, "", null);

    //单例模式,私有构造器
    private AiuiEngine() {
    }

    /**初始化aiui，单例模式
     * @param listener   识别和语义结果回调
     * @param cfgFilePath  aiui配置文件路径
     * @return
     */
    public static AIUIAgent getInstance(AIUIListener listener, String cfgFilePath) {
        // 检查对象是否存在,不存在就进入同步区块
        if (mAIUIAgent == null) {
            // 同步区块里面的代码只有在第一次才会执行
            synchronized (AiuiEngine.class) {
                if (mAIUIAgent == null) {
                    //设置设备sn,WakeupEngine的sn和AIUI的sn要一致
                    AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, serialNumber);
                    //创建aiuiAgent
                    mAIUIAgent = AIUIAgent.createAgent(ContextHolder.getContext(), getAIUIParams(ContextHolder.getContext(), cfgFilePath), listener);
                }
            }
        }
        Log.i(TAG, "create aiui agent");
        return mAIUIAgent;
    }

    public static void destroy() {
        if (mAIUIAgent != null) {
            mAIUIAgent.destroy();//结束时一定要调用destroy()，否则aiui的navite方法状态异常
            mAIUIAgent = null;//mAIUIAgent要设置为null,否则再次初始化时，mAIUIAgent没有重新创建，aiuilistener无法正常回调
        }
    }


    /**
     * 读取assets/aiui.cfg配置文件
     */
    private static String getAIUIParams(Context context, String cfgFilePath) {
        String params = "";
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open(cfgFilePath);
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }


    /**
     * 唤醒aiui，未唤醒状态不识别
     */
    public static void MSG_wakeup(int wakeTyep) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        EngineConstants.wakeupType = wakeTyep;
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_WAKEUP;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.params = "";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 让aiui休眠(待唤醒状态),等待用户唤醒
     */
    public static void MSG_reset_wakeup() {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_RESET_WAKEUP;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.params = "";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 传音频给aiui识别，合成，语义理解
     */
    public static void MSG_writeAudio(byte[] inputData) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        asrMsg.msgType = AIUIConstant.CMD_WRITE;
        asrMsg.arg1 = 0;
        asrMsg.arg2 = 0;
        asrMsg.params = "data_type=audio,sample_rate=16000";
        asrMsg.data = inputData;
        mAIUIAgent.sendMessage(asrMsg);
    }

    /**
     * TODO 自定义请求级别
     * 传音频给aiui识别，合成，语义理解。
     */
    public static void MSG_writeAudio(byte[] inputData, int model) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        String pers_param = "";
        switch (model) {
            case MODEL_USER:
                pers_param = ",pers_param:{\"uid\":\"\"}";
                break;
            default:
                break;
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_WRITE;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.params = "data_type=audio,sample_rate=16000" + pers_param;
        msg.data = inputData;
    }

    /**
     * 传文本获取语义结果--后台请求，请求后不会进行语音识别(例：更新界面天气)
     *
     * @param text  发送文本获取nlp结果
     * @param scene 文本请求对应aiui平台的scene名称，需要跟识别的scene区别开
     */
    public static void MSG_sendTextForNlp(String text, String scene) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        //语音和文本同时请求的话，避免改变aiui的状态
        if (EngineConstants.mAIUIState != AIUIConstant.STATE_WORKING) {
            MSG_wakeup(EngineConstants.WAKEUPTYPE_TEXT);//先唤醒aiui
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_WRITE;
        msg.arg1 = 0;
        msg.arg2 = 0;
        // 请求参数设置tag，aiuilistener回调将携带tag，可用于关联输入输出
        msg.params = "data_type=text,tag=text-tag,scene=" + scene;
        msg.data = text.getBytes(StandardCharsets.UTF_8);
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 传文本获取语义结果--触屏请求
     *
     * @param text  发送文本获取nlp结果
     * @param scene 文本请求对应aiui平台的scene名称，需要跟识别的scene区别开
     */
    public static void MSG_sendTextForNlp_Touch(String text, String scene) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        //语音和文本同时请求的话，避免改变aiui的状态
        if (EngineConstants.mAIUIState != AIUIConstant.STATE_WORKING) {
            MSG_wakeup(EngineConstants.WAKEUPTYPE_TEXT);//先唤醒aiui
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_WRITE;
        msg.arg1 = 0;
        msg.arg2 = 0;
        // 请求参数设置tag，aiuilistener回调将携带tag，可用于关联输入输出
        msg.params = "data_type=text,tag=text-touch,scene=" + scene;
        msg.data = text.getBytes(StandardCharsets.UTF_8);
        mAIUIAgent.sendMessage(msg);
    }


    /**
     * 使用sdk内部录音机，不能与外部SystemRecord，AlsaRecord同时使用
     */
    public static void MSG_startSDKRecord(String text) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_START_RECORD;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.params = "data_type=audio,sample_rate=16000";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }


    /**
     * 开始合成
     *
     * @param text   所需合成的文本
     * @param params tts参数，包括发音人、语速、语调、音量，示例：
     *               StringBuffer params = new StringBuffer();
     *               params.append("vcn=x2_xiaojuan");//合成发音人，x2_xiaojuan默认免费，其他需要付费开通,发音人列表：https://www.yuque.com/iflyaiui/zzoolv/iwxf76
     *               params.append(",speed=55");//语速，取值范围[0,100]
     *               params.append(",pitch=50"); //音调，取值范围[0,100]
     *               params.append(",volume=55");  //音量，取值范围[0,100]
     */
    public static void TTS_start(String text, StringBuffer params) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        if(text.length()==0){
            Log.e(TAG, "tts文本为空，可能语义结果为\"\" ");
            return;
        }
        //所需合成的文本需要转为二进制数据
        byte[] ttsData = text.getBytes(StandardCharsets.UTF_8);
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_TTS;
        msg.arg1 = AIUIConstant.START;
        msg.arg2 = 0;
        msg.params = params.toString();
        msg.data = ttsData;
        mAIUIAgent.sendMessage(msg);
    }


    /**
     * 停止tts,无法恢复播放
     */
    public static void TTS_stop() {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_TTS;
        msg.arg1 = AIUIConstant.CANCEL;
        msg.arg2 = 0;
        msg.params = "";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 暂停tts，可以恢复播放
     */
    public static void TTS_pause() {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_TTS;
        msg.arg1 = AIUIConstant.PAUSE;
        msg.arg2 = 0;
        msg.params = "";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 从暂停状态恢复播放tts
     */
    public static void TTS_resume() {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_TTS;
        msg.arg1 = AIUIConstant.RESUME;
        msg.arg2 = 0;
        msg.params = "";
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 动态修改参数，文档：https://aiui.xfyun.cn/doc/aiui/3_access_service/access_interact/sdk/aiui_config.html#%E5%8A%A8%E6%80%81%E9%85%8D%E7%BD%AE
     * 示例：AiuiEngine.MSG_setParam("{\"tts\":{\"play_mode\":\"user\"}}");
     */
    public static void MSG_setParam(String param) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_SET_PARAMS;
        msg.arg1 = 0;
        msg.arg2 = 0;
        msg.params = param;
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 动态修改单个参数，文档：https://aiui.xfyun.cn/doc/aiui/3_access_service/access_interact/sdk/aiui_config.html#%E5%8A%A8%E6%80%81%E9%85%8D%E7%BD%AE
     * 示例：
     * "tts":{
     * "play_mode":"sdk"
     * },
     * 等同于 AiuiEngine.MSG_setParam("tts","play_mode","sdk");
     */
    public static void MSG_setSimpleParam(String param1, String param2, String param3) {
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put(param2, param3);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONObject outerJson = new JSONObject();
        try {
            outerJson.put(param1, innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MSG_setParam(outerJson.toString());
    }

    /**
     * 动态设置参数，将tts设置为手动播放
     */
    public static void TTS_playByUser() {
        /*  动态生成aiui.cfg的参数
         * "tts":{
         * 	"play_mode":"user"
         *  },
         */
        JSONObject emptyParams = new JSONObject();
        JSONObject ttsParam = new JSONObject();
        try {
            ttsParam.put("play_mode", "user");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            emptyParams.put("tts", ttsParam);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MSG_setParam(emptyParams.toString());
    }


    /**
     * 动态设置参数，将tts设置为sdk自动播放
     */
    public static void TTS_playBySdk() {
        /*  动态生成aiui.cfg的参数
         * "tts":{
         * 	"play_mode":"sdk"
         *  },
         */
        JSONObject emptyParams = new JSONObject();
        JSONObject ttsParam = new JSONObject();
        try {
            ttsParam.put("play_mode", "sdk");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            emptyParams.put("tts", ttsParam);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MSG_setParam(emptyParams.toString());
    }

    /**
     * 查询动态实体上传状态
     */
    public static void DynamicEntity_syncQuery(String sid) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        if (TextUtils.isEmpty(sid)) {
            Log.e(TAG, "sid为空");
            return;
        }

        JSONObject queryJson = new JSONObject();
        try {
            queryJson.put("sid", sid);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_QUERY_SYNC_STATUS;
        msg.arg1 = AIUIConstant.SYNC_DATA_SCHEMA;
        msg.arg2 = 0;
        msg.params = queryJson.toString();
        msg.data = null;
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 上传动态实体
     */
    public static void DynamicEntity_sync(JSONObject paramJson, JSONObject syncSchemaJson) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_SYNC;
        msg.arg1 = AIUIConstant.SYNC_DATA_SCHEMA;
        msg.arg2 = 0;
        msg.params = paramJson.toString();
        msg.data = syncSchemaJson.toString().getBytes(StandardCharsets.UTF_8); //数据必须utf-8编码
        mAIUIAgent.sendMessage(msg);
    }

    /**
     * 上传所见即所说数据
     */
    public static void SayWhenSeeing(byte[] syncData) {
        if (!AIUI_Initialized()) {
            return; //aiui未初始化就退出
        }
        AIUIMessage msg = new AIUIMessage(0, 0, 0, "", null);
        msg.msgType = AIUIConstant.CMD_SYNC;
        msg.arg1 = AIUIConstant.SYNC_DATA_SPEAKABLE;
        msg.arg2 = 0;
        //所见即可说使用用户级实体，设置uid,仅限该设备使用
        msg.params = "{\"audioparams\":{\"pers_param\":{\"uid\":\"\"}}}";
        msg.data = syncData; //数据必须utf-8编码
        mAIUIAgent.sendMessage(msg);
    }


    private static boolean AIUI_Initialized() {
        if (mAIUIAgent == null) {
            Log.e(TAG, "aiui未初始化");
            return false;
        }
        return true;
    }
}
