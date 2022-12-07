package com.sendi.deliveredrobot.ros.client.robot;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import sendi_sensor_msgs.InfraredManageResponse;


/**
 * 检查红外摄像头
 */
@ServiceClient(ClientConstant.INFRARED_MANAGE)
public class InfraredManageClient extends IAbstractClient {
    private InfraredManageResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), InfraredManageResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public InfraredManageResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
}
