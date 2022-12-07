package com.sendi.deliveredrobot.ros.client.map.target;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.ShowPathMapResponse;


/**
 * 获取路径目标点列表
 */
@ServiceClient(ClientConstant.SHOW_PATH_MAP)
// 2021-4-19 CallCruiseShowPathMapClient和此类url相同，注释掉前者
public class ShowMapPathClient extends IAbstractClient {
    private ShowPathMapResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ShowPathMapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ShowPathMapResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

/*    public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = response.getResult();
            if (result == 1) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                List<PathMapInfo> listMap = response.getMap();
                for (PathMapInfo temp : listMap) {
                    Map<String, Object> tempMap = new HashMap<>();
                    tempMap.put("x", temp.getX());
                    tempMap.put("y", temp.getY());
                    resultList.add(tempMap);
                }
                data.put("data", resultList);
                message.setType("overviewMapSend");
                message.setStatus(1);
                message.setPara(data);
                Subject.broadcast(message.toString());
            } else {
                message.setStatus(0);
            }
        }
    }*/
}