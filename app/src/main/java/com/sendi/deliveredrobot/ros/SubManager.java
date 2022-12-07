package com.sendi.deliveredrobot.ros;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.LASER_SCAN;

import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 订阅管理工具类
 *
 * @author Sunzecong
 * @version 1.0
 */
public class SubManager {
    /**
     * key：topic（在注解中）
     * value：是否订阅
     */
    private static final Map<String, AtomicBoolean> buttonMap = new ConcurrentHashMap<>(16);

    /**
     * 添加topic到map中
     */
    public static boolean addSub(String subName) {
        if (null == subName || "".equals(subName) || buttonMap.containsKey(subName)) {
            return false;
        }
        if (subName.equals(LASER_SCAN)) {
            System.out.println("===========================================");
        }
        buttonMap.put(subName, new AtomicBoolean(false));
        return true;
    }

    /**
     * 删除topic
     */
    public static synchronized boolean delSub(String subName) {
        if (null == subName || "".equals(subName)) {
            return false;
        }
        if (!buttonMap.containsKey(subName) || buttonMap.get(subName) == null) {
            return true;
        }
        if (buttonMap.get(subName).get()) {
            Topic topic = new Topic(subName);
            topic.setOp(JRosbridgeConstant.OP_CODE_UNSUBSCRIBE);
            RosWebSocketManager.getInstance().send(topic.toString());
        }
        buttonMap.remove(subName);
        return true;
    }

    /**
     * 获取所有topic
     */
    private static List<String> getAllSub() {
        if (buttonMap.size() == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<>(buttonMap.keySet());
    }

    /**
     * 获取所有未订阅的topic
     */
    public static List<String> getUnSubList() {
        if (buttonMap.size() == 0) {
            return Collections.emptyList();
        }
        ArrayList<String> unsubList = new ArrayList<>();
        for (Map.Entry<String, AtomicBoolean> entry : buttonMap.entrySet()) {
            if (!entry.getValue().get()) {
                unsubList.add(entry.getKey());
            }
        }
        return unsubList;
    }

    /**
     * 订阅未订阅topic
     */
    public static void subUnsubscribeTopic() {
        List<String> topicList = getUnSubList();
        int retry = 0;
        while (topicList.size() > 0) {
            if (retry > 3) {
                System.out.println("帅哥，未订阅的ros没订阅成功！" + topicList);
                break;
            }
            retry++;
            for (String topic : topicList) {
                sub(topic);
            }
            topicList = getUnSubList();
        }
    }

    /**
     * 订阅topic
     */
    public static boolean sub(String topicName) {
        if (!buttonMap.containsKey(topicName)) {
            return false;
        }
//        if (buttonMap.get(topicName).get()) {
//            return true;
//        }
        Topic topic = new Topic(topicName);
        boolean flag = RosWebSocketManager.getInstance().send(topic.toString());
        return buttonMap.get(topicName).compareAndSet(!flag, flag);
    }

    public static List<String> subTopics(List<String> topicList) {
        if (topicList == null || topicList.isEmpty()) return Collections.emptyList();
        List<String> list = new ArrayList<>(topicList.size());
        for (String topic : topicList) {
            if (!sub(topic)) {
                list.add(topic);
            }
        }
        return list;
    }

    /**
     * 取消订阅topic
     */
    public static boolean unsub(String topicName) {
        if (!buttonMap.containsKey(topicName)) {
            return true;
        }
        if (!buttonMap.get(topicName).get()) {
            return true;
        }
        Topic topic = new Topic(topicName);
        topic.setOp(JRosbridgeConstant.OP_CODE_UNSUBSCRIBE);
        boolean flag = RosWebSocketManager.getInstance().send(topic.toString());
        return buttonMap.get(topicName).compareAndSet(flag, !flag);
    }
}
