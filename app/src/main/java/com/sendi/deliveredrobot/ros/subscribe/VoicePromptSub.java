package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import navigation_base_msgs.VoicePrompt;

@Subscribe(ClientConstant.VOICE_PROMPT_TOPIC)
public class VoicePromptSub extends IAbstractClient {
    private VoicePrompt response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), VoicePrompt.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.VOICE_PROMPT_TOPIC, response));
    }

    @Override
    public Message getResponse() {
        return response;
    }

    /*private void controlDelivery(VoicePrompt message) {
        // 当前被挡情况
        int type = message.getType();
        // 被挡等级
        int grade = message.getGrade();
        String pageId = RobotStatus.pageId.get();
        int blockGrade;
        switch (type) {
            case 1:
                // 正常运行
                RobotStatus.blockGrade.set(0);
                // 取消等待的语音
                musicControlService.cancelWaitVoicePlay();
                if ("0x0201".equals(pageId) || "0x0301".equals(pageId)) {
                    TimerController.stop(DELIVERY_TIMER);
                } else if ("0x0204".equals(pageId) || "0x0302".equals(pageId) || "0x0103".equals(pageId)) {
                    TimerController.stop(RETURN_TIMER);
                }
                break;
            // 太靠近障碍物，不可行走，进行左右旋转，寻找方向
            case 2:
                if (2 != grade && 3 != grade) {
                    break;
                }
                blockGrade = 2 == grade ? 1 : 2;
                RobotStatus.blockGrade.set(blockGrade);
                musicControlService.playBeBlockedVoice(blockGrade);
                if ("0x0201".equals(pageId) || "0x0301".equals(pageId)) {
                    TimerController.start(DELIVERY_TIMER, ONE_THOUSAND_MILLIS);
                } else if ("0x0204".equals(pageId) || "0x0302".equals(pageId) || "0x0103".equals(pageId)) {
                    TimerController.start(RETURN_TIMER, ONE_THOUSAND_MILLIS);
                }
                break;
            // 前方0.5m外有障碍物挡住，直接停下来等待
            case 3:
                if (1 != grade && 2 != grade) {
                    break;
                }
                blockGrade = 1 == grade ? 3 : 4;
                RobotStatus.blockGrade.set(blockGrade);
                musicControlService.playBeBlockedVoice(blockGrade);
                if ("0x0201".equals(pageId) || "0x0301".equals(pageId)) {
                    TimerController.start(DELIVERY_TIMER, ONE_THOUSAND_MILLIS);
                } else if ("0x0204".equals(pageId) || "0x0302".equals(pageId) || "0x0103".equals(pageId)) {
                    TimerController.start(RETURN_TIMER, ONE_THOUSAND_MILLIS);
                }
                break;
        }
    }*/
}
