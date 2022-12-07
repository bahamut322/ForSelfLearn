package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import sensor_msgs.BatteryState;

@Subscribe(ClientConstant.BATTERY_STATE)
public class BatteryStateSub extends IAbstractClient {
    private BatteryState response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), BatteryState.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.BATTERY_STATE, response));
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

    // 处理逻辑
    /*public void uploadPower(int power, int powerSupplyStatus) {
        // 自检
        RobotStatus.changePowerManagerReport();
        // 比较与上次发送的数据
        int oldPower = RobotStatus.power.get();
        int oldPowerStatus = RobotStatus.powerStatus.get();
        if (oldPower == power && oldPowerStatus == powerSupplyStatus) {
            return;
        }
        // 刷新变量
        RobotStatus.power.compareAndSet(oldPower, power);
        RobotStatus.powerStatus.compareAndSet(oldPowerStatus, powerSupplyStatus);
        // 消息体构建
        MessageDto messageDto = new MessageDto();
        HashMap<String, Object> para = new HashMap<>();
        messageDto.setPara(para);
        messageDto.setPage_id("0x0000");
        messageDto.setSub_page_id("0x0000");
        messageDto.setCtrl_id("0x0002");
        para.put("percentage", power);
        // 是否是充电中
        int status = powerSupplyStatus == BatteryState.POWER_SUPPLY_STATUS_CHARGING ? 1 : 0;
        messageDto.setStatus(status);
        Subject.broadcast(messageDto.toString());
    }*/
}
