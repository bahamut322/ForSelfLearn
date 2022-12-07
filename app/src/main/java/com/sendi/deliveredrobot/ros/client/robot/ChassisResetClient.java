package com.sendi.deliveredrobot.ros.client.robot;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import chassis_msgs.ChassisResetResponse;

/**
 * 恢复出厂
 */
@ServiceClient(ClientConstant.CHASSIS_RESET)
public class ChassisResetClient extends IAbstractClient {
    private ChassisResetResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ChassisResetResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ChassisResetResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

/*    public void responseHandler(MessageDto messageDto) {
        if (getResponse() == null) {
            messageDto.setStatus(Constant.FAIL_CLIENT_STATUS);
            return;
        }
        messageDto.setStatus(this.response.getSuccess() ? Constant.SUCCESS_CLIENT_STATUS : Constant.FAIL_CLIENT_STATUS);
        if (!this.response.getSuccess())
            messageDto.getPara().put(Constant.MSG, this.response.getStatusMessage());
    }*/
}
