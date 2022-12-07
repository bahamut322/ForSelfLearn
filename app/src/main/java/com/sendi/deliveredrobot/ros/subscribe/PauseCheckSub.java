package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import map_msgs.Pause_check;

/**
 * create by yujx
 *
 * @data 2021/07/26
 */
@Subscribe(ClientConstant.PAUSE_CHECK)
public class PauseCheckSub extends IAbstractClient {
    /**
     * subMap:子图
     */
    private Pause_check response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Pause_check.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.PAUSE_CHECK, response));
    }

    @Override
    public Pause_check getResponse() {
        return this.response;
    }
}
