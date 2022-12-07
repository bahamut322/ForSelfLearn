package com.sendi.deliveredrobot.ros.dto;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Ros Publich广播参数类
 */
public class Publish {
    /**
     * 操作
     */
    private String op;
    /**
     * topic
     */
    private String topic;
    /**
     * 发送内容
     */
    private Map<String, Object> msg;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Map<String, Object> getMsg() {
        return msg;
    }

    public void setMsg(Map<String, Object> msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
