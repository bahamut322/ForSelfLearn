package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import navigation_base_msgs.State;

/**
 * 完成一次巡航
 */
@Subscribe(ClientConstant.FINISH_ONCE_CRUISE)
public class FinishOnceCruiseSub extends IAbstractClient {
    private State response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), State.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.FINISH_ONCE_CRUISE, response));
    }

    @Override
    public State getResponse() {
        return this.response;
    }

}
