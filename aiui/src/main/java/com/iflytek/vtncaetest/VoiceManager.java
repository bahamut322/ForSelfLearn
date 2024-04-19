package com.iflytek.vtncaetest;


import static com.iflytek.vtncaetest.chat.RawMessage.FromType.AIUI;
import static com.iflytek.vtncaetest.chat.RawMessage.MsgType.TEXT;

import android.util.Log;

import com.iflytek.aiui.AIUIEvent;
import com.iflytek.vtncaetest.chat.RawMessage;
import com.iflytek.vtncaetest.nlp.StreamNlpResultReorder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * AIUI交互功能处理。
 */
//@Singleton
public class VoiceManager {
//    private static final String TAG = "VoiceManager";

//    private final ChatRepo mChatRepo;
//    private final AIUIWrapper mAgent;
//    private final AIUIConfigCenter mConfigManager;
//    private final TTSManager mTTSManager;
//    private final AIUIPlayerWrapper mAIUIPlayer;

    // 记录自开始录音是否有vad前端点事件抛出
//    private boolean mHasBOSBeforeEnd = false;
    // 当前未结束的语音交互消息，更新语音消息的听写内容时使用
//    private RawMessage mAppendVoiceMsg = null;
    // 当前未结束的翻译结果消息
//    private RawMessage mAppendTransMsg = null;

    // 语音消息开始时间，用于计算语音消息持续长度
//    private long mAudioStart = System.currentTimeMillis();

    // 当前应用设置
//    private boolean mWakeUpEnable = false;

//    private SingleLiveEvent<Boolean> isActiveInteract = new SingleLiveEvent<>();
//    private MutableLiveData<Integer> mLiveVolume = new MutableLiveData<>();
//    private MutableLiveData<Boolean> mLiveWakeup = new SingleLiveEvent<>();

    // 处理PGS听写(流式听写）的数组
//    private final String[] mIATPGSStack = new String[256];
//    private final List<String> mInterResultStack = new ArrayList<>();
//    private final Context mContext;

    private final ResultManager mResultManager;

    /**************语义结果处理相关变量****************/
    // 当前未结束的结果消息
    private RawMessage mAppendResultMsg = null;

    // 上个大模型结果sid
    private String mLastCbmResultSid = "";

    // 上个stream_nlp结果的sid
    private String mLastStreamNlpSid = "";

    // stream_nlp结果重排工具。由于流式结果不一定按顺序到达，所以必须重排
    private final StreamNlpResultReorder mStreamNlpResultReorder;

    // 用于stream_nlp结果中的answer合成
//    private final StreamNlpTtsHelper mStreamNlpTtsHelper;

//    private final StreamNlpTtsHelper.Listener mTtsHelperListener =
//            new StreamNlpTtsHelper.Listener() {
//                @Override
//                public void onText(StreamNlpTtsHelper.OutTextSeg textSeg) {
//                    if (mTTSManager != null) {
//                        String addedParams = "tag=" + textSeg.mTag;
//                        if (!textSeg.isBegin()) {
//                            addedParams = addedParams + ",cancel_last=false";
//                        }
//
//                        mTTSManager.startTTS(textSeg.mText, null, false,
//                                addedParams);
//                    }
//                }
//
//                @Override
//                public void onTtsData(JSONObject bizParamJson, byte[] audio) {
//
//                }
//
//                @Override
//                public void onFinish(String fullText) {
//
//                }
//            };

//    @Inject
    public VoiceManager(/*Context context, AIUIWrapper wrapper, ChatRepo chatRepo,
                        AIUIConfigCenter configManager, TTSManager ttsManager,
                        AIUIPlayerWrapper aiuiPlayer*/) {
//        mContext = context;
//        mChatRepo = chatRepo;
//          mChatRepo = new ChatRepo();
//        mAgent = wrapper;
//        mConfigManager = configManager;
//        mTTSManager = ttsManager;
//        mAIUIPlayer = aiuiPlayer;

//        mConfigManager.isWakeUpEnable().observeForever((enable -> {
//            mWakeUpEnable = enable;
//        }));

        //AIUI事件回调监听器
//        mAgent.getLiveAIUIEvent().observeForever(event -> {
//            switch (event.eventType) {
//                case AIUIConstant.EVENT_RESULT: {
//                    processResult(event);
//                }
//                break;
//
//                case AIUIConstant.EVENT_VAD: {
//                    processVADEvent(event);
//                }
//                break;
//
//                case AIUIConstant.EVENT_ERROR: {
//                    processError(event);
//                }
//                break;
//
//                case AIUIConstant.EVENT_WAKEUP: {
//                    //唤醒添加语音消息
//                    if (mWakeUpEnable) {
//                        //唤醒自动停止播放
//                        mAIUIPlayer.autoPause();
//                        mTTSManager.stopTTS();
//
//                        beginAudio();
//                        mLiveWakeup.setValue(true);
//                    }
//                }
//                break;
//
//                case AIUIConstant.EVENT_SLEEP: {
//                    //休眠结束语音
//                    if (mWakeUpEnable) {
//                        endAudio();
//                        mLiveWakeup.setValue(false);
//                    }
//                }
//                break;
//            }
//        });

        // 创建结果重排对象
        mStreamNlpResultReorder = new StreamNlpResultReorder();
        // 文本限制越小界面刷新越频繁
        mStreamNlpResultReorder.setTextMinIncLimit(20);

        // 创建合成管理对象
//        mStreamNlpTtsHelper = new StreamNlpTtsHelper(mTtsHelperListener);
        // 文本限制越小越早合成
//        mStreamNlpTtsHelper.setTextMinLimit(10);

        // 创建结果管理器
        mResultManager = ResultManager.createInstance();
    }

    /**
     * 交互是否有效(从开始到结束是否有vad前端点事件）
     *
     * @return
     */
//    public LiveData<Boolean> isActiveInteract() {
//        return isActiveInteract;
//    }

    /**
     * 唤醒是否开启
     *
     * @return
     */
//    public LiveData<Boolean> isWakeUpEnable() {
//        return mConfigManager.isWakeUpEnable();
//    }

    /**
     * 音量变化
     *
     * @return
     */
//    public LiveData<Integer> volume() {
//        return mLiveVolume;
//    }

    /**
     * 唤醒和休眠变化
     *
     * @return
     */
//    public LiveData<Boolean> wakeUp() {
//        return mLiveWakeup;
//    }

    /**
     * 文本语义
     *
     * @param message 输入文本
     */
//    public void writeText(String message) {
//        mTTSManager.stopTTS();
//        mAIUIPlayer.autoPause();
//
//        if (mAppendVoiceMsg != null) {
//            //更新上一条未完成的语音消息内容
//            updateChatMessage(mAppendVoiceMsg);
//            mInterResultStack.clear();
//            mAppendVoiceMsg = null;
//        }
//        //pers_param用于启用动态实体和所见即可说功能
//        String params = "data_type=text,pers_param={\"appid\":\"\",\"uid\":\"\"}";
//        sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0,
//                params, message.getBytes()));
//        addChatMessage(new RawMessage(USER, TEXT, message.getBytes()));
//    }

    /**
     * 开始说话
     */
//    public void startSpeak() {
//        mTTSManager.stopTTS();
//        mAIUIPlayer.autoPause();
//        requestAudioFocus();
//        mAgent.startRecordAudio();
//        if (!mWakeUpEnable) {
//            beginAudio();
//        }
//        mHasBOSBeforeEnd = false;
//    }

//    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
//    };
//    AudioManager mAudioMgr;

//    private void requestAudioFocus() {
//        if (mAudioMgr == null) {
//            mAudioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        }
//        if (mAudioMgr != null) {
//            Log.i("VoiceManager", "Request audio focus");
//            int ret = mAudioMgr.requestAudioFocus(audioFocusChangeListener,
//                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//            if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                Log.i("VoiceManager", "request audio focus fail. " + ret);
//            }
//        }
//    }

//    private void abandonAudioFocus() {
//        if (mAudioMgr != null) {
//            Log.i("VoiceManager", "Abandon audio focus");
//            mAudioMgr.abandonAudioFocus(audioFocusChangeListener);
//            mAudioMgr = null;
//        }
//
//    }

    /**
     * 停止说话
     */
//    public void endSpeak() {
//        mAgent.stopRecordAudio();
//        abandonAudioFocus();
//        if (!mWakeUpEnable) {
//            endAudio();
//        }
//
//        isActiveInteract.postValue(mHasBOSBeforeEnd);
//    }

    /**
     * 继续
     * 恢复到前台后，如果是唤醒模式下重新开启录音
     */
//    public void onResume() {
//        if (mWakeUpEnable) {
//            mAgent.startRecordAudio();
//        }
//    }

    /**
     * 暂停
     * 唤醒模式下录音常开，pause时停止录音，避免不再前台时占用录音
     */
//    public void onPause() {
//        if (mWakeUpEnable) {
//            mAgent.stopRecordAudio();
//        }
//    }


    /**
     * 录音开始，生成新的录音消息
     */
//    private void beginAudio() {
//        mAudioStart = System.currentTimeMillis();
//        if (mAppendVoiceMsg != null) {
//            //更新上一条未完成的语音消息内容
//            updateChatMessage(mAppendVoiceMsg);
//            mAppendVoiceMsg = null;
//            mInterResultStack.clear();
//        }
//        if (mAppendTransMsg != null) {
//            updateChatMessage(mAppendTransMsg);
//            mAppendTransMsg = null;
//        }
//
//        //清空PGS听写中间结果
//        for (int index = 0; index < mIATPGSStack.length; index++) {
//            mIATPGSStack[index] = null;
//        }
//
//        mAppendVoiceMsg = new RawMessage(USER, Voice, new byte[]{});
//        mAppendVoiceMsg.cacheContent = "";
//        //语音消息msgData为录音时长
//        mAppendVoiceMsg.msgData = ByteBuffer.allocate(4).putFloat(0).array();
//        addChatMessage(mAppendVoiceMsg);
//    }

    /**
     * 录音结束，更新录音消息内容
     */
//    private void endAudio() {
//        if (mAppendVoiceMsg != null) {
//            mAppendVoiceMsg.msgData =
//                    ByteBuffer.allocate(4).putFloat((System.currentTimeMillis() - mAudioStart) / 1000.0f).array();
//            updateChatMessage(mAppendVoiceMsg);
//        }
//    }


    /**
     * 发送AIUI消息
     *
     * @param message
     */
//    private void sendMessage(AIUIMessage message) {
//        mAgent.sendMessage(message);
//    }

    /**
     * 新增聊天消息
     *
     * @param rawMessage
     */
//    private void addChatMessage(RawMessage rawMessage) {
//        mChatRepo.addMessage(rawMessage);
//    }

    /**
     * 更新聊天界面消息
     *
     * @param message
     */
//    private void updateChatMessage(RawMessage message) {
//        mChatRepo.updateMessage(message);
//    }


    /**
     * 处理vad事件，音量更新
     *
     * @param aiuiEvent
     */
//    private void processVADEvent(AIUIEvent aiuiEvent) {
//        if (aiuiEvent.arg1 == AIUIConstant.VAD_BOS) {
//            mHasBOSBeforeEnd = true;
//        }
//        if (aiuiEvent.eventType == AIUIConstant.EVENT_VAD) {
//            if (aiuiEvent.arg1 == AIUIConstant.VAD_VOL) {
//                mLiveVolume.setValue(5000 + 8000 * aiuiEvent.arg2 / 100);
//            }
//        }
//    }

    // 首个结果的时间
    private long mFirstRespTime;

    /**
     * 处理AIUI结果事件（听写结果和语义结果）。
     *
     * @param event 结果事件
     */
    public void processResult(AIUIEvent event) {
        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);

            long rspTime = event.data.getLong("eos_rslt", -1);  //响应时间
            String sid = event.data.getString("sid", "");
            String tag = event.data.getString("tag", "");

            String sub = params.optString("sub");
            String cnt_id = content.optString("cnt_id");

            if (!"tts".equals(sub)) {
                if (mLastCbmResultSid != sid) {
                    mLastCbmResultSid = sid;

                    // 清理之前的结果
                    mResultManager.clearSessionResults(5);
                }

                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id),
                        "utf-8"));
                cntJson.put("sid", sid);
                cntJson.put("eos_rslt", rspTime);

                if ("cbm_semantic".equals(sub)) {
                    // 从cbm_meta中解析出untrusted字段（为true时表示通用主义结果不可信，应该采用nlp结果）
//                    JSONObject cbmMetaJson = cntJson.optJSONObject("cbm_meta");
//                    JSONObject cbmTextJson = new JSONObject(cbmMetaJson.optString("text"));
//                    JSONObject metaCbmSemanticJson = cbmTextJson.optJSONObject(sub);
//                    boolean untrusted = metaCbmSemanticJson.optBoolean("untrusted", false);
//
//                    // 大模型SDK的通用语义结果
//                    JSONObject semanticJson = cntJson.optJSONObject(sub);
//                    int status = semanticJson.optInt("status", -1);
//                    ResultManager.SubResult subResult = new ResultManager.SubResult(sid, sub,
//                            status,
//                            cntJson);
//                    ResultManager.SessionResult sessionResult = mResultManager.addResult(subResult);
//
//                    if (untrusted) {
//                        // 为true时不处理通用主义结果（不管rc是否为0），应该采用nlp结果
//                        return;
//                    }
//
//                    String text = semanticJson.optString("text", "");
//                    if (!TextUtils.isEmpty(text)) {
//                        try {
//                            JSONObject intentJson = new JSONObject(text);
//                            int rc = intentJson.optInt("rc", -1);
//                            if (rc == 0) {
//                                // 有效语义结果，将语义结果作为消息插入到消息列表中
//                                mAppendResultMsg = new RawMessage(AIUI, TEXT,
//                                        intentJson.toString().getBytes(), null, rspTime);
//                                mAppendResultMsg.service = intentJson.optString("service", "");
//                                mAppendResultMsg.sid = sid;
//                                addChatMessage(mAppendResultMsg);
//
//                                // 设置标记
//                                sessionResult.mHasValidSemantic = true;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
                } else if ("nlp".equals(sub)) {
                    if (!cntJson.has("nlp")) {
                        // AIUI v1的通用语义结果
//                        JSONObject intentJson = cntJson.optJSONObject("intent");
//                        if (intentJson.length() != 0) {
                            // 解析得到语义结果，将语义结果作为消息插入到消息列表中
//                            RawMessage rawMessage = new RawMessage(AIUI, TEXT,
//                                    intentJson.toString().getBytes(), null, rspTime);
//                            addChatMessage(rawMessage);
//                        }
                    } else {
                        // AIUI v2的结果
                        // 解析出索引，状态和文本字段
                        JSONObject nlpJson = cntJson.optJSONObject("nlp");
                        int seq = nlpJson.getInt("seq");
                        int status = nlpJson.getInt("status");
                        String text = nlpJson.getString("text");

                        if (text.startsWith("{\"intent\":")) {
                            // AIUI通用语义结果
                            JSONObject textJson = new JSONObject(text);
                            JSONObject intentJson = textJson.optJSONObject("intent");
                            if (intentJson.length() != 0) {
                                // 解析得到语义结果，将语义结果作为消息插入到消息列表中
//                                RawMessage rawMessage = new RawMessage(AIUI, TEXT,
//                                        intentJson.toString().getBytes(), null, rspTime);
//                                addChatMessage(rawMessage);
                            }
                        } else {
                            // 大模型语义结果
                            ResultManager.SubResult subResult = new ResultManager.SubResult(sid, sub,
                                    status, cntJson);
                            ResultManager.SessionResult sessionResult =
                                    mResultManager.addResult(subResult);

//                            if (sessionResult.mHasValidSemantic) {
//                                ResultManager.ResultStream resultStream =
//                                        sessionResult.getResultStream("nlp");
//                                if (resultStream.mIsCompleted) {
//                                    // 把同一个sid下的结果更新到消息中
//                                    Log.i("heky", "orderedText1: " + mAppendResultMsg.cacheContent);
////                                    JSONArray allResults = sessionResult.getSubResultJsonArray();
////                                    mAppendResultMsg.setMsgData(allResults.toString().getBytes());
////                                    updateChatMessage(mAppendResultMsg);
//                                }
//
//                                return;
//                            }

                            if (!mLastStreamNlpSid.equals(sid)) {
                                mFirstRespTime = rspTime;
                                mLastStreamNlpSid = sid;

                                // 来了新结果，清空之前
                                mStreamNlpResultReorder.clear();
//                                mStreamNlpTtsHelper.clear();
                                mAppendResultMsg = null;
                            }

//                            mStreamNlpTtsHelper.addText(text, seq, status);

                            List<StreamNlpResultReorder.StreamNlpResult> addedResultList =
                                    mStreamNlpResultReorder.addResult(new StreamNlpResultReorder.StreamNlpResult(cntJson,
                                            text, seq, status));
                            if (addedResultList != null) {
                                String orderedText = mStreamNlpResultReorder.getOrderedText();
                                if (mAppendResultMsg == null) {
                                    JSONObject firstResult = addedResultList.get(0).mIntentJson;
                                    int rc = firstResult.optInt("rc", 0);
                                    String service = firstResult.optString("service", "");

                                    mAppendResultMsg = new RawMessage(AIUI, TEXT, null,
                                            orderedText, mFirstRespTime);
                                    mAppendResultMsg.rc = rc;
                                    mAppendResultMsg.service = service;
                                    mAppendResultMsg.sid = sid;

//                                    addChatMessage(mAppendResultMsg);
                                } else {
                                    mAppendResultMsg.setCacheContent(orderedText);

//                                    updateChatMessage(mAppendResultMsg);
                                }

                                if (mStreamNlpResultReorder.isAddCompleted()) {
                                    // 结果全部收完
                                    if (mAppendResultMsg != null) {
                                        JSONArray allResults = sessionResult.getSubResultJsonArray();
                                        mAppendResultMsg.setMsgData(allResults.toString().getBytes());
                                        Log.i("heky", "orderedText2: " + mAppendResultMsg.cacheContent);
//                                        updateChatMessage(mAppendResultMsg);
                                    }

                                    mStreamNlpResultReorder.clear();
                                }
                            }
                        }
                    }
                } else if ("iat".equals(sub)) {
                    Log.i("heky", "IAT ======");
//                    processIATResult(cntJson);
                }
            }
//            else {
//                int isUrl = content.optInt("url", 0);
//                if (isUrl == 1) {
//                    Timber.tag(TAG).d("receive tts url: %s",
//                            new String(event.data.getByteArray(cnt_id), "utf-8"));
//                } else {
//                    if (mStreamNlpTtsHelper != null) {
//                        mStreamNlpTtsHelper.onOriginTtsData(tag, bizParamJson, null);
//                    }
//                }
//            }
        } catch (Throwable e) {
//            e.printStackTrace();
        }
    }

    /**
     * 解析听写结果更新当前语音消息的听写内容
     */
//    private void processIATResult(JSONObject cntJson) throws JSONException {
//        if (mAppendVoiceMsg == null) return;
//
//        JSONObject text = cntJson.optJSONObject("text");
//        // 解析拼接此次听写结果
//        StringBuilder iatText = new StringBuilder();
//        JSONArray words = text.optJSONArray("ws");
//        boolean lastResult = text.optBoolean("ls");
//        for (int index = 0; index < words.length(); index++) {
//            JSONArray charWord = words.optJSONObject(index).optJSONArray("cw");
//            for (int cIndex = 0; cIndex < charWord.length(); cIndex++) {
//                iatText.append(charWord.optJSONObject(cIndex).opt("w"));
//            }
//        }
//
//        String voiceIAT = "";
//        String pgsMode = text.optString("pgs");
//        //非PGS模式结果
//        if (TextUtils.isEmpty(pgsMode)) {
//            if (TextUtils.isEmpty(iatText)) return;
//
//            //和上一次结果进行拼接
//            if (!TextUtils.isEmpty(mAppendVoiceMsg.cacheContent)) {
//                voiceIAT = mAppendVoiceMsg.cacheContent;//+ "\n";
//            }
//            voiceIAT += iatText;
//        } else {
//            int sn = text.optInt("sn");
//            mIATPGSStack[sn] = iatText.toString();
//            //pgs结果两种模式rpl和apd模式（替换和追加模式）
//            if ("rpl".equals(pgsMode)) {
//                //根据replace指定的range，清空stack中对应位置值
//                JSONArray replaceRange = text.optJSONArray("rg");
//                int start = replaceRange.getInt(0);
//                int end = replaceRange.getInt(1);
//
//                for (int index = start; index <= end; index++) {
//                    mIATPGSStack[index] = null;
//                }
//            }
//
//            StringBuilder PGSResult = new StringBuilder();
//            //汇总stack经过操作后的剩余的有效结果信息
//            for (int index = 0; index < mIATPGSStack.length; index++) {
//                if (TextUtils.isEmpty(mIATPGSStack[index])) continue;
//
////                if(!TextUtils.isEmpty(PGSResult.toString())) PGSResult.append("\n");
//                PGSResult.append(mIATPGSStack[index]);
//                //如果是最后一条听写结果，则清空stack便于下次使用
//                if (lastResult) {
//                    mIATPGSStack[index] = null;
//                }
//            }
//            voiceIAT = join(mInterResultStack) + PGSResult.toString();
//
//            if (lastResult) {
//                mInterResultStack.add(PGSResult.toString());
//            }
//        }
//
//        if (!TextUtils.isEmpty(voiceIAT)) {
//            mAppendVoiceMsg.cacheContent = voiceIAT;
//            updateChatMessage(mAppendVoiceMsg);
//        }
//    }

    /**
     * 错误处理
     * <p>
     * 在聊天对话消息中添加错误消息提示
     *
     * @param aiuiEvent
     */
//    private void processError(AIUIEvent aiuiEvent) {
//        //向消息列表中添加AIUI错误消息
//        int errorCode = aiuiEvent.arg1;
//        //AIUI网络异常，不影响交互，可以作为排查问题的线索和依据
//        if (errorCode >= 10200 && errorCode <= 10215) {
//            Timber.e("AIUI Network Warning %d, Don't Panic", errorCode);
//            return;
//        }
//
//        if (!TextUtils.isEmpty(aiuiEvent.info) && aiuiEvent.info.contains("tts")) {
////            if (mStreamNlpTtsHelper != null) {
////                mStreamNlpTtsHelper.clear();
////            }
//        }
//
//        Map<String, String> semantic = new HashMap<>();
//        semantic.put("errorInfo", aiuiEvent.info);
//        switch (errorCode) {
//            case 10120: {
//                mChatRepo.addMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "网络有点问题 :(",
//                                semantic, null).getBytes()));
//                break;
//            }
//
//            case 11200: {
//                mChatRepo.addMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "11200 错误 " +
//                                "\n小娟发音人权限未开启，请在控制台应用配置下启用语音合成后等待一分钟生效后再重启应用", semantic, null).getBytes()));
//                break;
//            }
//
//            case 20006: {
//                addChatMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "录音启动失败 :" +
//                                "(，请检查是否有其他应用占用录音", semantic, null).getBytes()));
//                break;
//            }
//
//            case 600002: {
//                addChatMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "唤醒 600002 " +
//                                "错误\n 唤醒配置vtn.ini路径错误，请检查配置路径", semantic, null).getBytes()));
//                break;
//            }
//
//            case 600100: {
//                addChatMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "唤醒 600100 " +
//                                "错误\n 唤醒资源文件路径错误，请检查资源路径", semantic, null).getBytes()));
//                break;
//            }
//
//            case 600022: {
//                addChatMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR, "唤醒 600022 " +
//                                "错误\n 唤醒装机授权不足，请联系商务开通", semantic, null).getBytes()));
//                break;
//            }
//
//            default: {
//                addChatMessage(new RawMessage(AIUI, TEXT,
//                        SemanticUtil.fakeSemanticResult(0, Constant.SERVICE_ERROR,
//                                aiuiEvent.arg1 + " 错误", semantic, null).getBytes()));
//            }
//        }
//    }

//    private String join(List<String> data) {
//        StringBuilder builder = new StringBuilder();
//        for (int index = 0; index < data.size(); index++) {
//            builder.append(data.get(index));
//        }
//
//        return builder.toString();
//    }
}
