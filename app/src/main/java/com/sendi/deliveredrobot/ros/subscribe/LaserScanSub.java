package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import sensor_msgs.LaserScan;

/**
 * 镭射扫描
 */
@Subscribe(ClientConstant.LASER_SCAN)
public class LaserScanSub extends IAbstractClient {
    private LaserScan response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        // 2021/5/31 只需要上报一次，之后可以取消订阅
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), LaserScan.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.LASER_SCAN, response));

        // 业务逻辑
//        RobotStatus.scanAvaliable.compareAndSet(false, true);
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

}
