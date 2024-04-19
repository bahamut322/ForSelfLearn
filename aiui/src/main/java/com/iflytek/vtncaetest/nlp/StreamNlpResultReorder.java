package com.iflytek.vtncaetest.nlp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hj at 2023/4/25 18:45
 *
 * 流式语义结果重排类。
 */
public class StreamNlpResultReorder {
    public static final int STATUS_BEGIN = 0;
    public static final int STATUS_CONTINUE = 1;
    public static final int STATUS_END = 2;

    public static class StreamNlpResult {
        public JSONObject mIntentJson;
        public String mText;
        public int mIndex;
        public int mStatus;

        public StreamNlpResult(JSONObject intentJson, String text, int index, int status) {
            mIntentJson = intentJson;
            mText = text;
            mIndex = index;
            mStatus = status;
        }

        public boolean isBegin() {
            return mStatus == STATUS_BEGIN;
        }

        public boolean isEnd() {
            return mStatus == STATUS_END;
        }
    }

    private final List<StreamNlpResult> mResultList = new ArrayList<>();

    private int mLastOrderedPos = -1;

    private int mTextMinIncLimit = 100;

    // 记录有序的文本
    private StringBuilder mOrderedTextSb = new StringBuilder();

    /**
     * 设置文本长度增加下限，值越小addResult越频繁返回新增的有序部分。
     *
     * @param limit
     */
    public void setTextMinIncLimit(int limit) {
        mTextMinIncLimit = limit;
    }

    /**
     * 添加结果。
     *
     * @param result
     * @return 新增的有序部分List，返回null则表示这次追加没有改变有序部分
     */
    public List<StreamNlpResult> addResult(StreamNlpResult result) {
        if (isAddCompleted()) {
            return null;
        }

        int begin = mResultList.size() - 1;
        int pos = begin;
        while (pos >= 0) {
            StreamNlpResult cur = mResultList.get(pos);
            if (result.mIndex < cur.mIndex) {
                pos--;
            } else {
                break;
            }
        }

        if (pos == begin) {
            // list为空，或者插入位置为尾部
            mResultList.add(result);
        } else {
            mResultList.add(pos + 1, result);
        }

        List<StreamNlpResult> newlyAddedOrderSeg = new ArrayList<>();
        int newLastOrderedPos = mLastOrderedPos;
        int addedTextLen = 0;
        StringBuilder addedTextSb = new StringBuilder();

        for (int i = newLastOrderedPos + 1; i < mResultList.size(); i++) {
            StreamNlpResult cur = mResultList.get(i);
            if (i == cur.mIndex) {
                newLastOrderedPos = i;
                newlyAddedOrderSeg.add(cur);
                addedTextSb.append(cur.mText);
                addedTextLen += cur.mText.length();
            } else {
                break;
            }
        }

        List<StreamNlpResult> finalResultList = null;
        if (isAddCompleted() || addedTextLen >= mTextMinIncLimit) {
            finalResultList = newlyAddedOrderSeg.isEmpty() ? null : newlyAddedOrderSeg;
        }

        if (finalResultList != null) {
            mLastOrderedPos = newLastOrderedPos;
            mOrderedTextSb.append(addedTextSb);
        }

        return finalResultList;
    }

    public String getOrderedText() {
        return mOrderedTextSb.toString();
    }

    public boolean isAddCompleted() {
        if (mResultList.isEmpty()) {
            return false;
        }

        int size = mResultList.size();
        StreamNlpResult last = mResultList.get(size - 1);
        if (last.isEnd() && size == last.mIndex + 1) {
            return true;
        }

        return false;
    }

    public JSONArray getAllIntentJsonArray() {
        JSONArray array = new JSONArray();
        for (StreamNlpResult result : mResultList) {
            array.put(result.mIntentJson);
        }

        return array;
    }

    public void clear() {
        mResultList.clear();
        mLastOrderedPos = -1;
        mOrderedTextSb = new StringBuilder();
    }
}
