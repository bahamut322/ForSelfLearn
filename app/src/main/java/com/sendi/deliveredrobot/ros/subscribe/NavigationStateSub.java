package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import navigation_base_msgs.State;


@Subscribe(ClientConstant.NAVIGATION_STATE_TOPIC)
public class NavigationStateSub extends IAbstractClient {
    private State response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), State.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.NAVIGATION_STATE_TOPIC, response));
    }

    @Override
    public Message getResponse() {
        return null;
    }

    /**
     * 智能送餐订阅处理两个合并
     *
     * @param message
     */
   /* private void control(State message) {
        String pageId = RobotStatus.pageId.get();
        int state = message.getState();
        int infoCode = message.getInfoCode();
        RobotStatus.navigationState.set(state);
        RobotStatus.infoCode.set(infoCode);
        INavigationStateStrategy navigationStateStrategy;
        MessageDto messageDto = new MessageDto();
        // 设置全局变量
        if ("0x0301".equals(pageId)) {
            messageDto.setPage_id("0x0301");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(0);
            navigationStateStrategy = (PauseDeliveryBirthdayPage) DispathService.getInstance().getBeanByName(PauseDeliveryBirthdayPage.class.getName());
        } else if ("0x0302".equals(pageId)) {
            messageDto.setPage_id("0x0302");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(0);
            navigationStateStrategy = (BirthdayReturnPage) DispathService.getInstance().getBeanByName(BirthdayReturnPage.class.getName());
        } else if ("0x0103".equals(pageId)) {
            messageDto.setPage_id("0x0103");
            messageDto.setSub_page_id("0x0001");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(1);
            navigationStateStrategy = (ManualReturnPage) DispathService.getInstance().getBeanByName(ManualReturnPage.class.getName());
        } else if ("0x0201".equals(pageId)) {
            messageDto.setPage_id("0x0201");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(1);
            navigationStateStrategy = (DeliveryPage) DispathService.getInstance().getBeanByName(DeliveryPage.class.getName());
        } else if ("0x0204".equals(pageId)) {
            messageDto.setPage_id("0x0204");
            messageDto.setSub_page_id("0x0001");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(1);
            navigationStateStrategy = (ReturnPage) DispathService.getInstance().getBeanByName(ReturnPage.class.getName());
        } else if ("0x0401".equals(pageId)) {
            messageDto.setPage_id("0x0401");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(0);
            navigationStateStrategy = (CruisingPage) DispathService.getInstance().getBeanByName(CruisingPage.class.getName());
        } else if ("0x0403".equals(pageId)) {
            messageDto.setPage_id("0x0403");
            messageDto.setSub_page_id("0x0000");
            messageDto.setCtrl_id("0x0000");
            messageDto.setStatus(0);
            navigationStateStrategy = (CruiseEndPage) DispathService.getInstance().getBeanByName(CruiseEndPage.class.getName());
        } else {
            LogUtil.e(ResultEnum.UNKNOWN_NAVIGATION_STATE.getMsg());
            return;
        }
        if (state == 1) {
            MessageDto arrive = navigationStateStrategy.arrive(messageDto);
            if (arrive.getStatus() == 1) {
                Subject.broadcast(arrive.toString());
            }
        } else if (state == -20 || state == -21) {
            Subject.broadcast(navigationStateStrategy.treatableFault(messageDto).toString());
        } else if (state == 3 || state == 5) {
            Subject.broadcast(navigationStateStrategy.successProcessed(messageDto).toString());
        } else {
            LogUtil.e(ResultEnum.UNKNOWN_NAVIGATION_STATE.getMsg());
        }
    }*/

}
