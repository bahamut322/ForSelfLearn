package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import std_msgs.String;

/**
 * 获取lora接收到的透传信息
 * ------------------
 * this.response.getData()：透传数据
 */
@Subscribe(ClientConstant.LORA_RECEIVE)
public class LoraReceiveSub extends IAbstractClient {
    private String response;

    @Override
    public boolean send(java.lang.String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(java.lang.String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), String.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.LORA_RECEIVE, response));
    }

    @Override
    public String getResponse() {
        return this.response;
    }
}

