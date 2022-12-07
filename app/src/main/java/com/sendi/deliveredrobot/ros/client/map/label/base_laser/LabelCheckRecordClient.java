package com.sendi.deliveredrobot.ros.client.map.label.base_laser;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.InitResponse;

@ServiceClient(ClientConstant.CHECK_RECORD)
public class LabelCheckRecordClient extends IAbstractClient {
    private InitResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), InitResponse.class);
        }
        countDownLatch.countDown();
    }

    @Override
    public InitResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

    /*public void responseHandler(MessageDto message) {
        if (null != getResponse()) {
            if (response.getResult() == Constant.SUCCESS_CLIENT_STATUS) {
                message.setStatus(Constant.SUCCESS_CLIENT_STATUS);
            } else {
                message.setStatus(Constant.FAIL_CLIENT_STATUS);
            }
        } else {
            message.setStatus(Constant.FAIL_CLIENT_STATUS);
        }
    }*/
}
