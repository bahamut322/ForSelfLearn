package com.sendi.deliveredrobot.ros.client.map.special_area;


import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.GetLocationStateResponse;

/**
 * 标签地图定位状态
 */
@ServiceClient(ClientConstant.LOCATION_STATE)
public class LocationStateClient extends IAbstractClient {
    private GetLocationStateResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        this.response = null;
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), GetLocationStateResponse.class);
        }
        countDownLatch.countDown();
    }

    @Override
    public GetLocationStateResponse getResponse() {
        try {
            countDownLatch.await(LIVE_VIEW_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

   /* public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int state = response.getState();
            switch (state) {
                case 0:
                    data.put("msg", "查询标签文件状态-通讯失败");
                    message.setStatus(0);
                    break;
                case 1:
                    message.setStatus(1);
                    break;
                case 2:
                    data.put("msg", "查询标签文件状态-未找到标签地图");
                    message.setStatus(0);
                    break;
                case 3:
                    data.put("msg", "查询标签文件状态-定位失败");
                    message.setStatus(0);
                    break;
                default:
                    data.put(Constant.MSG, "查询标签文件状态-未定义异常");
                    message.setStatus(0);
                    break;
            }
        } else {
            data.put(Constant.MSG, LOCATION_STATE + "不可用");
            message.setPara(data);
            message.setStatus(0);
        }
        message.setPara(data);
    }*/
}
