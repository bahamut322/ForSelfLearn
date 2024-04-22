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

    public VoiceManager() {
        // 创建结果重排对象
        mStreamNlpResultReorder = new StreamNlpResultReorder();
        // 文本限制越小界面刷新越频繁
        mStreamNlpResultReorder.setTextMinIncLimit(20);
        // 创建结果管理器
        mResultManager = ResultManager.createInstance();
    }
    // 首个结果的时间
    private long mFirstRespTime;

    /**
     * 处理AIUI结果事件（听写结果和语义结果）。
     *
     * @param event 结果事件
     */
    public String processNlpResult(AIUIEvent event) {
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
                } else if ("nlp".equals(sub)) {
                    if (!cntJson.has("nlp")) {
                        // AIUI v1的通用语义结果
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
                            }
                        } else {
                            // 大模型语义结果
                            ResultManager.SubResult subResult = new ResultManager.SubResult(sid, sub,
                                    status, cntJson);
                            ResultManager.SessionResult sessionResult =
                                    mResultManager.addResult(subResult);
                            if (!mLastStreamNlpSid.equals(sid)) {
                                mFirstRespTime = rspTime;
                                mLastStreamNlpSid = sid;
                                // 来了新结果，清空之前
                                mStreamNlpResultReorder.clear();
                                mAppendResultMsg = null;
                            }
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
                                } else {
                                    mAppendResultMsg.setCacheContent(orderedText);
                                }

                                if (mStreamNlpResultReorder.isAddCompleted()) {
                                    // 结果全部收完
                                    if (mAppendResultMsg != null) {
                                        JSONArray allResults = sessionResult.getSubResultJsonArray();
                                        mAppendResultMsg.setMsgData(allResults.toString().getBytes());
                                        return mAppendResultMsg.cacheContent;
                                    }

                                    mStreamNlpResultReorder.clear();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
        return null;
    }
}
