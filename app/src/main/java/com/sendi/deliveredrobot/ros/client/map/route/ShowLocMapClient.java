package com.sendi.deliveredrobot.ros.client.map.route;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.ShowLocMapResponse;


/**
 * 显示标签点地图
 */
@ServiceClient(ClientConstant.SHOW_LOCATION_MAP)
public class ShowLocMapClient extends IAbstractClient {
    private ShowLocMapResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ShowLocMapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ShowLocMapResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

   /* public void responseHandle(String type) {
        while (true) {
            if (RobotStatus.isSendShowLocMapClient.get() && null != response) {
                Map<String, Object> data = new HashMap<>();
                int result = response.getResult();
                if (result == 1) {
                    List<Map<String, Object>> resultList = new ArrayList<>();
                    List<LabelMapInfo> listMap = response.getMap();
                    for (LabelMapInfo temp :
                            listMap) {
                        Map<String, Object> tempMap = new HashMap<>();
                        tempMap.put("num", String.valueOf(temp.getCode()));
                        tempMap.put("x", temp.getX());
                        tempMap.put("y", temp.getY());
                        resultList.add(tempMap);
                    }
                    data.put("data", resultList);
                    MessageDto messageDto = new MessageDto();
                    if (type.equals("0x0601")) {
                        messageDto.setNode_name("debugging");
                        // 注意这里是0x0601
                        messageDto.setPage_id("0x0601");
                        messageDto.setSub_page_id("0");
                        messageDto.setCtrl_id("0x0008");
                    } else if (type.equals("0x0602")) {
                        messageDto.setNode_name("debugging");
                        messageDto.setPage_id("0x0602");
                        messageDto.setSub_page_id("0");
                        messageDto.setCtrl_id("0x0003");
                    } else if (type.equals("0x0605")) {
                        messageDto.setNode_name("debugging");
                        messageDto.setPage_id("0x0605");
                        messageDto.setSub_page_id("0");
                        messageDto.setCtrl_id("0x0003");
                    }
                    messageDto.setType("overviewMapSend");
                    messageDto.setStatus(1);
                    messageDto.setPara(data);
                    try {
                        TimeUnit.MILLISECONDS.sleep(88);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Subject.broadcast(messageDto.toString());
                    RobotStatus.isSendShowLocMapClient.compareAndSet(true, false);
                } else {
                    LogUtil.e("show_loc_map_client_ call fails");
                }
                return;
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(88);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }*/
}
