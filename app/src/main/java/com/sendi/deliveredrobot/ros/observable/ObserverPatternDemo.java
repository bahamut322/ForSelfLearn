package com.sendi.deliveredrobot.ros.observable;

import com.sendi.deliveredrobot.ros.dto.RosResult;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;

public class ObserverPatternDemo {
    public static void main(String[] args) {
        // 1.客户端注册
        BinaryObserver demoObserver = new BinaryObserver(Subject.getInstance()) {
            @Override
            public void receivedMessage(RosResult rosResult) {
                super.receivedMessage(rosResult);
            }
        };
        // 2.广播
        Subject.broadcast(RosResultUtil.failure("test"));
        // 3.demoObserver.receivedMessage被调用，前端接收到ros发布消息

    }
}