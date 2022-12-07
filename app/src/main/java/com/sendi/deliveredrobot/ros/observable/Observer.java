package com.sendi.deliveredrobot.ros.observable;

import com.sendi.deliveredrobot.ros.dto.RosResult;

public abstract class Observer {
    /**
     * 订阅主题
     */
    protected Subject subject;

    /**
     * 接收信息
     *
     * @param rosResult
     */
    public abstract void receivedMessage(RosResult rosResult);
}