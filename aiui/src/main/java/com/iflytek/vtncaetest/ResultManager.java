package com.iflytek.vtncaetest;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hj at 2023/8/22 16:46
 * <p>
 * 结果管理器。
 */
public class ResultManager {
    public static class SubResult {
        public static final int STATUS_BEGIN = 0;

        public static final int STATUS_CONTINUE = 1;

        public static final int STATUS_END = 2;

        public static final int STATUS_ALLONE = 3;

        public String mSid = "";

        public String mSub = "";

        public int mStatus = -1;

        public JSONObject mResultJson;

        public SubResult(String sid, String sub, int status, JSONObject json) {
            mSid = sid;
            mSub = sub;
            mStatus = status;
            mResultJson = json;
        }
    }

    public static class ResultStream {
        public String mSid = "";

        public String mSub = "";

        public boolean mIsCompleted;

        private final List<SubResult> mSubResults = new ArrayList<>();

        public boolean addSubResult(SubResult result) {
            if (TextUtils.isEmpty(mSid)) {
                mSid = result.mSid;
                mSub = result.mSub;
            }

            if (!mSub.equals(result.mSub)) {
                return false;
            }

            if (SubResult.STATUS_END == result.mStatus || SubResult.STATUS_ALLONE == result.mStatus) {
                mIsCompleted = true;
            }

            mSubResults.add(result);

            return true;
        }

        public int getResultSize() {
            return mSubResults.size();
        }
    }

    public static class SessionResult {
        public String mSid = "";

        // 是否有有效的老语义结果
        public boolean mHasValidSemantic;

        // 按添加顺序排列的所有的sub
        private final List<String> mSubs = new ArrayList<>();

        // Map<sub, ResultStream>
        private final Map<String, ResultStream> mSubResultStreams = new HashMap<>();

        // 同一个sid下所有结果组成的json数组
        private final JSONArray mSubResultJsonArray = new JSONArray();

        public boolean addSubResult(SubResult result) {
            if (TextUtils.isEmpty(mSid)) {
                mSid = result.mSid;
            }

            if (!mSid.equals(result.mSid)) {
                return false;
            }

            mSubResultJsonArray.put(result.mResultJson);

            ResultStream resultStream;
            if (!mSubResultStreams.containsKey(result.mSub)) {
                mSubs.add(result.mSub);

                resultStream = new ResultStream();
                mSubResultStreams.put(result.mSub, resultStream);
            } else {
                resultStream = mSubResultStreams.get(result.mSub);
            }

            assert resultStream != null;
            return resultStream.addSubResult(result);
        }

        public List<String> getSubs() {
            return mSubs;
        }

        public ResultStream getResultStream(String sub) {
            return mSubResultStreams.get(sub);
        }

        public boolean hasResultStream(String sub) {
            return mSubResultStreams.containsKey(sub);
        }

        public JSONArray getSubResultJsonArray() {
            return mSubResultJsonArray;
        }
    }

    private final List<String> mSidList = new ArrayList<>();

    // Map<sid, SessionResult>
    private final Map<String, SessionResult> mSessionResults = new HashMap<>();

    private static ResultManager sINSTANCE;

    public static ResultManager createInstance() {
        if (sINSTANCE == null) {
            sINSTANCE = new ResultManager();
        }

        return sINSTANCE;
    }

    public static ResultManager getInstance() {
        return sINSTANCE;
    }

    private ResultManager() {

    }

    public SessionResult addResult(SubResult subResult) {
        if (subResult == null || TextUtils.isEmpty(subResult.mSid)) {
            return null;
        }

        SessionResult sessionResult;
        if (!mSessionResults.containsKey(subResult.mSid)) {
            sessionResult = new SessionResult();

            mSessionResults.put(subResult.mSid, sessionResult);
            mSidList.add(subResult.mSid);
        } else {
            sessionResult = mSessionResults.get(subResult.mSid);
        }

        assert sessionResult != null;
        sessionResult.addSubResult(subResult);

        return sessionResult;
    }

    public boolean hasSessionResult(String sid) {
        return mSessionResults.containsKey(sid);
    }

    /**
     * 按sid顺序清除结果，直到保留left个。
     *
     * @param left
     */
    public void clearSessionResults(int left) {
        int count = mSidList.size() - left;
        if (count <= 0) {
            return;
        }

        int i = 0;
        while (i < count) {
            String sid = mSidList.remove(0);
            mSessionResults.remove(sid);

            i++;
        }
    }

    public void clearSessionResults() {
        mSessionResults.clear();
        mSidList.clear();
    }

    public SessionResult getSessionResult(String sid) {
        return mSessionResults.get(sid);
    }

    public SessionResult removeSessionResult(String sid) {
        return mSessionResults.remove(sid);
    }
}
