package com.iflytek.vtncaetest.utils;

//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONException;
//import com.alibaba.fastjson.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 返回流式识别的完整结果
 */
public class StreamingAsrUtil {
    //存储asr中间结果
    private static StringBuilder iatText = new StringBuilder();
    //存储asr最终结果
    private static String asrResult = "";

    /**
     * 解析听写结果更新当前语音消息的听写内容,返回当前完整结果
     */
    public static String processIATResult(JSONObject text) throws JSONException {

        JSONArray words = null;
        try {
            words = text.getJSONArray("ws");
        } catch (JSONException e) {

        }
        //第一句话(sn==1),清除上一句话的asr结果
        if (text.getInt("sn") == 1) {
            asrResult = "";
        }
        for (int i = 0; i < words.length(); i++) {
            JSONArray charWord = words.getJSONObject(i).getJSONArray("cw");
            for (int j = 0; j < charWord.length(); j++) {
                iatText.append(charWord.getJSONObject(j).getString("w"));
            }
        }
        //中间结果长度≥最终结果，替换最终结果
        if (iatText.toString().length() >= asrResult.length()) {
            asrResult = iatText.toString();
        }
        //清除中间结果，方便下次存储
        iatText.delete(0, iatText.length());

        return asrResult;
    }

}

