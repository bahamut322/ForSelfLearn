package com.sendi.deliveredrobot.ros.client.robot;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import chassis_msgs.ChassisStateResponse;


/**
 * 检测急停按钮是否在开机前被按下，正常被按下会主动发送，开机前被按下不会发送
 */
@ServiceClient(ClientConstant.CHASSIC_STATE)
public class ChassicStateClient extends IAbstractClient {
    private ChassisStateResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ChassisStateResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ChassisStateResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

     /*
    if (state == ChassisStateResponse.TRGGERED) {
                    dto.setStatus(1);
                    dtoPara.put(Constant.MSG, "机器人已急停，请先复位，再操作!");
                    Subject.broadcast(dto.toString());
                } else {
                    dto.setStatus(0);
                    dtoPara.put(Constant.MSG, "急停恢复");
                    isPush = false;
                    Subject.broadcast(dto.toString());
                }*/

}
