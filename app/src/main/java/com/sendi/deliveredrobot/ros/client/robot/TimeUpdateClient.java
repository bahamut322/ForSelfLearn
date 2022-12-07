package com.sendi.deliveredrobot.ros.client.robot;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import chassis_msgs.TimeUpdateResponse;

/**
 * 时间同步
 */
@ServiceClient(ClientConstant.TIME_UPDATE)
public class TimeUpdateClient extends IAbstractClient {
    private TimeUpdateResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        this.response = null;
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), TimeUpdateResponse.class);
        }
        countDownLatch.countDown();
    }

    @Override
    public TimeUpdateResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

    /*public void responseHandler(MessageDto messageDto) {
        if (getResponse() == null) {
            messageDto.setStatus(Constant.FAIL_CLIENT_STATUS);
            return;
        }
        messageDto.setStatus(this.response.getSuccess() ? Constant.SUCCESS_CLIENT_STATUS : Constant.FAIL_CLIENT_STATUS);
        if (!this.response.getSuccess())
            messageDto.getPara().put(Constant.MSG, this.response.getStatusMessage());
    }*/
}
