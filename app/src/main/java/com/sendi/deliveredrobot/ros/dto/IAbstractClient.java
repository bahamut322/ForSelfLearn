package com.sendi.deliveredrobot.ros.dto;

import com.sendi.deliveredrobot.ros.RosWebSocketManager;

import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: Sunzecong
 * @date: 2021/2/3
 */

public abstract class IAbstractClient {
    //默认超时 15s
    public Integer TIME_OUT = 15;
    // 实景图
    public final Integer LIVE_VIEW_TIME_OUT = 15;
    // LASER MAP
    public final Integer LASER_TIME_OUT = 60;
    //阻塞
    public CountDownLatch countDownLatch;

    /**
     * 发送方法
     *
     * @param request
     * @return
     */
    public boolean send(String request) {
        boolean flag = RosWebSocketManager.getInstance().send(request);
        //发送成功，重置CountDownLatch为1
        if (flag) {
            this.countDownLatch = new CountDownLatch(1);
        }
        return flag;
    }

    /**
     * 处理异步回调
     *
     * @param message ros回传消息
     */
    public abstract void callbackMessageHandle(String message);

    /**
     * 阻塞获取response
     *
     * @return
     */
    public abstract <T> T getResponse();
}
