package com.sendi.deliveredrobot.ros.client.navigation;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import chassis_msgs.PeripheralsCtrlResponse;


/**
 * 感应门控制
 */
@ServiceClient(ClientConstant.DOOR_CONTROL_SERVICE)
public class DoorControlClient extends IAbstractClient {
    private PeripheralsCtrlResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), PeripheralsCtrlResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public PeripheralsCtrlResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
    /*    public void responseHandler(MessageDto message) {
        HashMap<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            switch (response.getResult()) {
                case 1:
                    message.setStatus(1);
                    data.put(MSG, "成功");
                    break;
                case -2:
                    message.setStatus(0);
                    data.put(MSG, "导航异常-状态错误");
                    break;
                case -3:
                    message.setStatus(0);
                    data.put(MSG, "导航异常-已经运行");
                    break;
                case -24:
                    message.setStatus(0);
                    data.put(MSG, "导航异常-看不到地图对应的标签");
                    break;
                default:
                    message.setStatus(0);
                    data.put(MSG, "导航异常-其他错误");
                    break;
            }
        } else {
            message.setStatus(0);
            data.put(MSG, "MOVE_TO 服务不可用");
        }
        message.setPara(data);
    }*/
}
