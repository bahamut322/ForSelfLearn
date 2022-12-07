package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import navigation_base_msgs.State;

@Subscribe(ClientConstant.SCHEDULING_CHANGE_GOAL)
public class AvoidanceSub extends IAbstractClient {
    private State response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), State.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.SCHEDULING_CHANGE_GOAL, response));
    }

    @Override
    public State getResponse() {
        return this.response;
    }

    // 处理逻辑
    /*public void control(int cmd) {
        MessageDto messageDto = new MessageDto();
        HashMap<String, Object> para = new HashMap<>();
        messageDto.setPara(para);
        messageDto.setType(Constant.TEXT_WRITE);
        messageDto.setPage_id("0x0000");
        messageDto.setSub_page_id("0x0011");
        messageDto.setCtrl_id("0x0001");
        para.put(Constant.STATUS, cmd);
        if (1 == cmd) {
            para.put(Constant.MSG, "正在调度");
            Subject.broadcast(messageDto.toString());
        } else if (2 == cmd) {
            para.put(Constant.MSG, "结束调度");
            Subject.broadcast(messageDto.toString());
        }
    }*/
}

