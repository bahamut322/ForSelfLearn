package com.sendi.deliveredrobot.ros.client.map;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.SetLocMapResponse;

/**
 * 设置路径文件地图
 */
@ServiceClient(ClientConstant.SET_LOCATION_MAP)
public class SetLocMapClient extends IAbstractClient {
    private SetLocMapResponse response;


    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), SetLocMapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public SetLocMapResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
    /*public void responseHandler(MessageDto message){
        Map<String, Object> data = new HashMap<>();
        if (getResponse() != null) {
            int result = response.getResult();
            if (result != 1) {
                message.setStatus(0);
                data.put("msg", "地图设置失败");
                message.setPara(data);
            }
        } else {
            message.setStatus(0);
            data.put("msg", "地图设置失败");
            message.setPara(data);
        }
    }*/
}
