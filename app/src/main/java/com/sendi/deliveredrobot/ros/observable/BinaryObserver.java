package com.sendi.deliveredrobot.ros.observable;

import com.sendi.deliveredrobot.ros.dto.RosResult;

public class BinaryObserver extends Observer {
    public BinaryObserver(Subject subject) {
        this.subject = subject;
        this.subject.attach(this);
    }

    @Override
    public void receivedMessage(RosResult rosResult) {
//        System.out.println("监听客户端收到: " + rosResult);
    }
}