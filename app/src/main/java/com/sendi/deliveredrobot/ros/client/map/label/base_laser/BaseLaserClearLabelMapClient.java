package com.sendi.deliveredrobot.ros.client.map.label.base_laser;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.ClearRuntimeMapResponse;


/**
 * 清除标签点
 */
@ServiceClient(ClientConstant.LABEL_BASE_LASER_CLEAR_RUNTIME_MAP)
public class BaseLaserClearLabelMapClient extends IAbstractClient {
    private ClearRuntimeMapResponse response;


    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ClearRuntimeMapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ClearRuntimeMapResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
/*
    public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = response.getResult();
            switch (result) {
                case 1:
                    data.put("msg", "成功");
                    message.setStatus(1);
                    message.setPara(data);
                    break;
                case -1:
                    data.put("msg", "未知错误");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                case -2:
                    data.put("msg", "状态错误");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                default:
                    data.put("msg", "未定义失败");
                    message.setStatus(0);
                    message.setPara(data);
                    break;

            }
        } else {
            data.put("msg", ClearRuntimeMap._TYPE + "服务运行错误");
            message.setPara(data);
        }
    }*/
}
