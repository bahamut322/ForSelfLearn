package com.sendi.deliveredrobot.ros.client.map.label.base_laser;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.ShowRunningLocMapResponse;


/**
 * 获取标签总图
 */
@ServiceClient(ClientConstant.LABEL_BASE_LASER_GET_RUNNING_LOCATION_MAP)
public class BaseLaserOverviewViewClient extends IAbstractClient {
    private ShowRunningLocMapResponse response;


    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ShowRunningLocMapResponse.class);
        } else {
            response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ShowRunningLocMapResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

    /*public void responseHandle(MessageDto message) {
        if (null != getResponse()) {
            int result = response.getResult();
            if (result == 1) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                List<LabelMapInfo> listMap = response.getMap();
                for (LabelMapInfo temp : listMap) {
                    Map<String, Object> tempMap = new HashMap<>();
                    tempMap.put("num", String.valueOf(temp.getCode()));
                    tempMap.put("x", temp.getX());
                    tempMap.put("y", temp.getY());
                    resultList.add(tempMap);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("data", resultList);
                MessageDto messageDto = new MessageDto();
                messageDto.setNode_name("debugging");
                messageDto.setPage_id("0x0601");
                messageDto.setSub_page_id("0x0000");
                messageDto.setCtrl_id("0x0008");
                messageDto.setType("overviewMapSend");
                messageDto.setStatus(1);
                messageDto.setPara(data);
                Subject.broadcast(messageDto.toString());
            }

        } else {
//            message.setStatus(0);
            //todo 不用设置失败吗？
        }
    }*/
}
