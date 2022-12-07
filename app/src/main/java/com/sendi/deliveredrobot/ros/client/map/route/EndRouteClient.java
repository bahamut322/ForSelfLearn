package com.sendi.deliveredrobot.ros.client.map.route;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import org.ros.internal.message.Message;

import java.util.concurrent.TimeUnit;

import route_map_msgs.EndResponse;


/**
 * 关闭路径绘制
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/23
 */
@ServiceClient(ClientConstant.ROUTE_MAP_END)
public class EndRouteClient extends IAbstractClient {
    private EndResponse endResponse;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.endResponse = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), EndResponse.class);
        } else {
            endResponse = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public Message getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return endResponse;
    }
/*
    public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = endResponse.getResult();
            message.setType("eventAck");
            switch (result) {
                case 1:
                    message.setStatus(1);
                    data.put("msg", "建图成功");
                    message.setPara(data);
                    //全局变量，以便随时shutdown
                    RouteDrawingPageServiceImpl.base_original_name = endResponse.getMapName();
                    // 关闭订阅
//                    if (StartRouteClient.routeListSub != null)
//                        nodeMainExecutor.shutdownNodeMain(StartRouteClient.routeListSub);
                    SubManager.unsub(ClientConstant.ROBOT_POSE);
                    break;
                case -1:
                    data.put("msg", "建图失败");
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
            message.setStatus(0);
        }
    }*/
}
