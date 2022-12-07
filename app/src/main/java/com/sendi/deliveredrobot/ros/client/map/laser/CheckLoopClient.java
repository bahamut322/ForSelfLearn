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
@ServiceClient(ClientConstant.CHECK_LOOP)
public class CheckLoopClient extends IAbstractClient {
    private Laser_map_managerResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), Laser_map_managerResponse.class);
        } else {
            response = null;
        }
        countDownLatch.countDown();
    }
//    备用方法，如果Loop_pairResponse中的static_map不能自动载入
//    @Override
//    public void callbackMessageHandle(String message) {
//        JSONObject resJson = JSONObject.parseObject(message);
//        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
//        if (resultFlag) {
//            JSONObject static_map = resJson.getJSONObject(Constant.VALUES).getJSONObject("static_map");
//            this.data = static_map.getBytes(Constant.DATA);
//        }
//        countDownLatch.countDown();
//    }

    @Override
    public Laser_map_managerResponse getResponse() {
        try {
            countDownLatch.await(LIVE_VIEW_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return this.response;
    }
}
