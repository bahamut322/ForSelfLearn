package com.sendi.deliveredrobot.ros.client.map.laser;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import map_msgs.New_empty_mapResponse;

/**
 * create by yujx
 *
 * @data 2021/07/23
 */
@ServiceClient(ClientConstant.NEW_EMPTY_MAP)
public class NewEmptyMapClient extends IAbstractClient {
    private New_empty_mapResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), New_empty_mapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public New_empty_mapResponse getResponse() {
        try {
            countDownLatch.await(LIVE_VIEW_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return this.response;
    }
}
