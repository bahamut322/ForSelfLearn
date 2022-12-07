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
 * 路径绘制初始化
 */
@ServiceClient(ClientConstant.ROUTE_MAP_INIT)
public class StartRouteClient extends IAbstractClient {

    private InitResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
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

    /* public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = response.getResult();
            switch (result) {
                case 1:
                    data.put("msg", "成功");
                    message.setStatus(1);
                    //运行监听路径点
//                        nodeMainExecutor.execute(routeListSub, nodeConfiguration);
//                routeListSub.startListenTopic(ClientConstant.ROBOT_POSE);
                    SubManager.sub(ClientConstant.ROBOT_POSE);
                    break;
                case -1:
                    data.put("msg", "未知错误");
                    message.setStatus(0);
                    break;
                case -2:
                    data.put("msg", "状态错误");
                    message.setStatus(0);
                    break;
                default:
                    data.put("msg", "未定义");
                    message.setStatus(0);
                    break;
            }
        } else {
            message.setStatus(0);
            data.put(MSG, "ROUTE_MAP_INIT 服务不可用");
        }
        message.setPara(data);
    }*/
}
