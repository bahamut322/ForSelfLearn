package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import map_msgs.Sub_map_info;

/**
 * 显示实时激光地图
 * create by yujx
 *
 * @data 2021/07/26
 */
@Subscribe(ClientConstant.SUB_MAP_INFO)
public class SubMapInfoSub extends IAbstractClient {
    /**
     * Id: 当前处理的激光帧序号
     * Status:  1-正常 2-超时
     * sub_map: 当前子图点云
     * robot_pose：机器人的子图坐标
     */
    private Sub_map_info response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Sub_map_info.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.SUB_MAP_INFO, response));
    }

    @Override
    public Sub_map_info getResponse() {
        return this.response;
    }
}
