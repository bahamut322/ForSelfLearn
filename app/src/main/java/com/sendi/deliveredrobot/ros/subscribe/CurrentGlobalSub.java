package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import map_msgs.Current_global_laser;

/**
 * create by yujx
 *
 * @data 2021/07/26
 */
@Subscribe(ClientConstant.GLOBAL_LASER)
public class CurrentGlobalSub extends IAbstractClient {
    /**
     * GlobalLaser：当前激光匹配
     */
    private Current_global_laser response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Current_global_laser.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.GLOBAL_LASER, response));
    }

    @Override
    public Current_global_laser getResponse() {
        return this.response;
    }
}
