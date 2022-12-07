package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import chassis_msgs.SafeState;

@Subscribe(ClientConstant.SAFE_STATE_TOPIC)
public class SafeStateSub extends IAbstractClient {
    private SafeState response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), SafeState.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.SAFE_STATE_TOPIC, response));
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

    /*private void control(SafeState response) {
        AtomicBoolean emergencyStop = RobotStatus.emergencyStop;
        byte safeState = response.getSafeState();
        byte safeType = response.getSafeType();
        MessageDto messageDto = new MessageDto();
        HashMap<String, Object> para = new HashMap<>();
        messageDto.setPara(para);
        messageDto.setType(Constant.TEXT_WRITE);
        messageDto.setPage_id("0x0000");
        messageDto.setCtrl_id("0x0001");
        messageDto.setStatus((int) safeState);
        String msg = "";
        if (safeType == SafeState.TYPE_ADAPTER) {
            if (safeState == SafeState.STATE_IS_TRIGGING) {
                if (safeState != last_trigging_state) {
                    messageDto.setSub_page_id("0x0012");
                    msg = "适配器已接入";
                    para.put(MSG, msg);
                    Subject.broadcast(messageDto.toString());
                }
                last_trigging_state = safeState;
            } else if (safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                if (safeState != last_trigging_state) {
                    messageDto.setSub_page_id("0x0012");
                    msg = "适配器已拔除";
                    para.put(MSG, msg);
                    Subject.broadcast(messageDto.toString());
                }
                last_trigging_state = safeState;
            }
            return;
        }
        if (safeType == SafeState.TYPE_EMERGENCY_STOP) {
            if (safeState == SafeState.STATE_IS_TRIGGING) {
                messageDto.setSub_page_id("0x0010");
                msg = "机器人已急停，请先复位，再操作!";
                emergencyStop.set(true);
                if (integrateControl.setNavigationManageMsg(2)) {
                    musicControlService.pauseBackgroundMusicPlay();
                }
                para.put(MSG, msg);
                Subject.broadcast(messageDto.toString());
            } else if (safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                messageDto.setSub_page_id("0x0010");
                msg = "急停恢复";
                emergencyStop.set(false);
                if (integrateControl.setNavigationManageMsg(2)) {
                    musicControlService.continueBackgroundMusic();
                }
                para.put(MSG, msg);
                Subject.broadcast(messageDto.toString());
            }
            return;
        }
        if (safeType == SafeState.TYPE_MOTOR_CURRENT) {
            if (safeState == SafeState.STATE_IS_NOT_TRIGGING) {
                messageDto.setSub_page_id("0x0006");
                msg = "机器人电机恢复";
                para.put(MSG, msg);
                Subject.broadcast(messageDto.toString());
            } else if (safeState == SafeState.STATE_IS_TRIGGING) {
                messageDto.setSub_page_id("0x0006");
                msg = "机器人电机异常!";
                para.put(MSG, msg);
                Subject.broadcast(messageDto.toString());
            }
        }
    }*/
}
