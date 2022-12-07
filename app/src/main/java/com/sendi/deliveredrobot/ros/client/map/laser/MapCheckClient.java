package com.sendi.deliveredrobot.ros.client.map.laser;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import map_msgs.Laser_map_managerResponse;

/**
 * create by yujx
 *
 * @data 2021/07/26
 */
@ServiceClient(ClientConstant.MAP_CHECK)
public class MapCheckClient extends IAbstractClient {
    private Laser_map_managerResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), Laser_map_managerResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public Laser_map_managerResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return this.response;
    }
}
