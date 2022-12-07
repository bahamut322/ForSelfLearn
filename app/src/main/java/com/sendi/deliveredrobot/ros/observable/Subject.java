package com.sendi.deliveredrobot.ros.observable;

import com.sendi.deliveredrobot.ros.dto.RosResult;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Subject {
    /**
     * 客户端容器
     */
    private static final List<Observer> observers = new CopyOnWriteArrayList<>();

    private Subject() {
        if (SingleSubject.INSTANCE != null) {
            throw new RuntimeException("不允许反射破坏单例");
        }
    }

    public static Subject getInstance() {
        return SingleSubject.INSTANCE;
    }

    //内部类
    private static class SingleSubject {
        private static final Subject INSTANCE = new Subject();
    }

    /**
     * 广播消息
     *
     * @param rosResult
     */
    public static void broadcast(RosResult rosResult) {
        notifyAllObservers(rosResult);
    }

    public void attach(Observer observer) {
        observers.add(observer);
    }

    private static void notifyAllObservers(RosResult rosResult) {
        for (Observer observer : observers) {
            observer.receivedMessage(rosResult);
        }
    }
}