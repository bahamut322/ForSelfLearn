package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import geometry_msgs.Pose2D;
import map_msgs.Sub_map_info;

@Subscribe(ClientConstant.MAPPING_POSE)
public class MappingPoseSub extends IAbstractClient {
    private Pose2D response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Pose2D.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.MAPPING_POSE, response));
    }

    @Override
    public Pose2D getResponse() {
        return this.response;
    }
}
