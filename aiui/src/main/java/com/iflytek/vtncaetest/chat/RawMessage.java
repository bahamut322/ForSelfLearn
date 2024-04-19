package com.iflytek.vtncaetest.chat;

import android.text.TextUtils;

import java.nio.ByteBuffer;

/**
 * 交互消息原始数据。
 */
public class RawMessage {
    public enum FeedbackType {
        NOT_YET,
        PRO,
        CON
    }

    private FeedbackType mFeedbackType = FeedbackType.NOT_YET;

    private static int sMsgIDStore = 0;

    public enum MsgType {
        TEXT, Voice
    }

    public enum FromType {
        USER, AIUI
    }

    public long msgID;
    private int msgVersion;
    public long responseTime;
    public FromType fromType;
    public MsgType msgType;
    public String cacheContent;
    public byte[] msgData;

    public int rc;
    public String service = "";

    public String sid = "";

    public RawMessage(FromType fromType, MsgType msgType, byte[] msgData, String cacheContent
            , long responseTime) {
        this.msgID = sMsgIDStore++;
        this.fromType = fromType;
        this.msgType = msgType;
        this.msgData = msgData;
        this.responseTime = responseTime;
        this.msgVersion = 0;
        this.cacheContent = cacheContent;
    }

    public RawMessage(FromType fromType, MsgType msgType, byte[] msgData) {
        this(fromType, msgType, msgData, null, 0);
    }

    public boolean isText() {
        return msgType == MsgType.TEXT;
    }

    public boolean isEmptyContent() {
        return TextUtils.isEmpty(cacheContent);
    }

    public boolean isFromUser() {
        return fromType == FromType.USER;
    }

    public int version() {
        return msgVersion;
    }

    public void versionUpdate() {
        msgVersion++;
    }

    public void setMsgData(byte[] data) {
        msgData = data;
    }

    public void setCacheContent(String content) {
        cacheContent = content;
    }

    public void appendCacheContent(String content) {
        if (cacheContent != null) {
            cacheContent = cacheContent + content;
        } else {
            cacheContent = content;
        }
    }

    public int getAudioLen() {
        if (msgType == MsgType.Voice) {
            return Math.round(ByteBuffer.wrap(msgData).getFloat());
        } else {
            return 0;
        }
    }


    public void setFeedbackType(FeedbackType type) {
        mFeedbackType = type;
    }

    public FeedbackType getFeedbackType() {
        return mFeedbackType;
    }
}
