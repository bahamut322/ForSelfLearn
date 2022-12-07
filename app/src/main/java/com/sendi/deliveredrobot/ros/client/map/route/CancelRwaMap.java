package com.sendi.deliveredrobot.ros.client.map.route;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import route_map_msgs.InitResponse;

/**
 * 清除地图信息--退出调试
 */
@ServiceClient(ClientConstant.CANCEL_RAW_MAP)
public class CancelRwaMap extends IAbstractClient {

    private InitResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), InitResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public InitResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
    /*public void responseHandler(MessageDto message){
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int insult = initResponse.getResult();
            message.setPara(data);
            switch (insult) {
                case 1:
                    message.setStatus(1);
                    data.put("msg", "成功");
                case -1:
                    message.setStatus(0);
                    data.put("msg", "未知错误");
                case -2:
                    message.setStatus(0);
                    data.put("msg", "状态错误");
                default:
                    message.setStatus(0);
                    data.put("msg", "未定义");
            }
        } else {
            message.setStatus(0);
        }
    }*/
}
