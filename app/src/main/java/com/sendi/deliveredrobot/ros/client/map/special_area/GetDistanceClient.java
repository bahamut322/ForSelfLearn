package com.sendi.deliveredrobot.ros.client.map.special_area;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.ObstacleMessageResponse;

/**
 * 斜坡测距
 */
@ServiceClient(ClientConstant.GET_DISTANCE)
public class GetDistanceClient extends IAbstractClient {
    private ObstacleMessageResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ObstacleMessageResponse.class);
        }
        countDownLatch.countDown();
    }

    @Override
    public ObstacleMessageResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
/*

    public void responseHandler(MessageDto messageDto) {
        if (getResponse() != null) {
            messageDto.setStatus(1);
            double data = response.getResult();
            BigDecimal b = new BigDecimal(data);
            data = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
            messageDto.getPara().put(Constant.DATA, data);
        } else {
            messageDto.setStatus(0);
            messageDto.getPara().put(Constant.MSG, "获取斜坡距离-请求超时");
        }
    }
*/

}
