package com.sendi.deliveredrobot.ros.dto;

import static com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant.OP_CODE_SUBSCRIBE;

import com.alibaba.fastjson.JSONObject;

/**
 * Ros Topic 请求参数类
 */
public class Topic {
    public Topic() {
    }

    /**
     * 订阅
     *
     * @param topic
     */
    public Topic(String topic) {
        this.op = OP_CODE_SUBSCRIBE;
        this.topic = topic;
    }

    /**
     * 操作
     */
    private String op;
    /**
     * topic名称
     */
    private String topic;

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

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
