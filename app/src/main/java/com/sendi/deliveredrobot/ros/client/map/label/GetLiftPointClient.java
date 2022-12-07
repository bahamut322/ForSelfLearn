package com.sendi.deliveredrobot.ros.client.map.label;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.ClearRuntimeMapResponse;
import navigation_base_msgs.CreateOneWayResponse;


/**
 * 打电梯内点服务
 */
@ServiceClient(ClientConstant.GET_LIFT_POINT)
public class GetLiftPointClient extends IAbstractClient {
    /**
     * -- request
     * range (double):机器人离电梯内点的距离
     *
     * -- response
     * pose1 (Pose2D):内点坐标
     * pose2 (Pose2D):外点坐标
     * state (int):错误码 -10：传感器异常 -1：失败 1：成功
     */
    private CreateOneWayResponse response;


    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CreateOneWayResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public CreateOneWayResponse getResponse() {
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
