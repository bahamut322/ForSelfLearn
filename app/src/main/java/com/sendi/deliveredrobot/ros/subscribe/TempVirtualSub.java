package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import sensor_msgs.PointCloud;

@Subscribe(ClientConstant.TEMP_OBSTACLE)
public class TempVirtualSub extends IAbstractClient {
    private PointCloud response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), PointCloud.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.TEMP_OBSTACLE, response));
    }

    @Override
    public Message getResponse() {
        return null;
    }

    // 处理逻辑
    /*public void control(List<Point32> point32List) {
        String pageId = RobotStatus.pageId.get();
        MessageDto messageDto = new MessageDto();
        HashMap<String, Object> para = new HashMap<>();
        messageDto.setPara(para);
        messageDto.setType("pathMapSend");
        if (pageId.equals("0x0606")) {
            messageDto.setPage_id("0x0606");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0009");
        } else if (pageId.equals("0x0607")) {
            messageDto.setPage_id("0x0607");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x000E");
        }
        messageDto.setStatus(1);
        // 只取最后一个坐标点
        Point32 lastPoint = point32List.get(point32List.size() - 1);
        para.put(Constant.X, lastPoint.getX());
        para.put(Constant.Y, lastPoint.getY());
        para.put(Constant.Z, lastPoint.getZ());
        Subject.broadcast(messageDto.toString());
    }*/
}