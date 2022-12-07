package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import navigation_base_msgs.State;

@Subscribe(ClientConstant.NEAR_INDOOR_LIFT)
public class NearIndoorLiftSub extends IAbstractClient {
    /**
     * State:
     * state = 1 正常
     * state = -1 异常
     * infoCode :
     * SUB_INFO_CODE_GO_TO_LIFT_OBSTACLE_ERROR = -61 #进电梯被围堵
     * SUB_INFO_CODE_OUT_OF_LIFT_OBSTACLE_ERROR = -62 #出电梯被围堵
     */
    private State response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), State.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.NEAR_INDOOR_LIFT, response));
    }

    @Override
    public State getResponse() {
        return this.response;
    }
}

