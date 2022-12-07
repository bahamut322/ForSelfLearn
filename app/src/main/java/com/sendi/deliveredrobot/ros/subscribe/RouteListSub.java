package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import geometry_msgs.Pose2D;


/**
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/25
 */
@Subscribe(ClientConstant.ROBOT_POSE)
public class RouteListSub extends IAbstractClient {
    private Pose2D response;


    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Pose2D.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.ROBOT_POSE, response));
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

    // false-关  true-开
//    private boolean flag = false;

//    @Override
//    public GraphName getDefaultNodeName() {
//        return GraphName.of(this.getClass().getSimpleName()+"_"+ cn.gzsendi.constant.RobotStatus.process++);
//    }

    /*public void onStart(ConnectedNode connectedNode) {
        Subscriber<Pose2D> subscriber = connectedNode.newSubscriber(ROBOT_POSE, Pose2D._TYPE);
        subscriber.addMessageListener(new MessageListener<Pose2D>() {
            @Override
            public void onNewMessage(Pose2D pose2D) {
                Map<String, Object> data = new HashMap<>();
                data.put("x", pose2D.getX());
                data.put("y", pose2D.getY());
                data.put("w", pose2D.getTheta());

                MessageDto messageDto = new MessageDto();
                messageDto.setNode_name("debugging");
                messageDto.setPage_id("0x0602");
                messageDto.setSub_page_id("0x0000");
                messageDto.setCtrl_id("0x0008");
                messageDto.setType("pathMapSend");
                messageDto.setStatus(1);
                messageDto.setPara(data);
                Subject.broadcast(messageDto.toString());
            }
        });
    }*/

//    @Override
//    public synchronized boolean startListenTopic(String topicName) {
//        if (!flag) {
//            Topic topic = new Topic(topicName);
//            flag = send(topic.toString());
//        }
//        return flag;
//    }
//
//    @Override
//    public synchronized boolean stopListenTopic(String topicName) {
//        //订阅为开的时候，执行关闭，泽聪悟了
//        if (flag) {
//            Topic topic = new Topic(topicName);
//            topic.setOp(JRosbridgeConstant.OP_CODE_UNSUBSCRIBE);
//            //websocket 发送成功返回true，关闭订阅标志flag置为flase
//            flag = !send(topic.toString());
//        }
//        //flag是false是关闭成功的意思
//        return !flag;
//    }
}
