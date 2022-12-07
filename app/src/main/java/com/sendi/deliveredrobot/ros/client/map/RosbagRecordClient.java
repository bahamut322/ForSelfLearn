package com.sendi.deliveredrobot.ros.client.map;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import sendi_nav_msgs.RosbagRecordResponse;


/**
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/3/5
 */
@ServiceClient(ClientConstant.ROSBAG_RECORD)
public class RosbagRecordClient extends IAbstractClient {
    private RosbagRecordResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), RosbagRecordResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public RosbagRecordResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

/*    public void responseHandler(MessageDto message) {
        if (getResponse() == null) {
            message.setStatus(0);
        } else {
            if (response.getState() == 1) {
                message.setStatus(1);
            } else {
                message.setStatus(0);
            }
        }
    }*/
}
