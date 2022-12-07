package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import std_msgs.Float64;
import std_msgs.String;

@Subscribe(ClientConstant.ROBOT_MILEAGE)
public class RobotMileageSub extends IAbstractClient {
    private Float64 response;

    @Override
    public boolean send(java.lang.String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(java.lang.String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Float64.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.ROBOT_MILEAGE, response));
    }

    @Override
    public Float64 getResponse() {
        return this.response;
    }
}

