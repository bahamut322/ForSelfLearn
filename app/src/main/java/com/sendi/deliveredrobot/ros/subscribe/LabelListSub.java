package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import label_msgs.LabelList;


/**
 * 运行监听标签点
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/22
 */
@Subscribe(ClientConstant.LABEL_LIST)
public class LabelListSub extends IAbstractClient {
    private LabelList response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), LabelList.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.LABEL_LIST, response));
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

    /*public void onStart(ConnectedNode connectedNode) {
        Subscriber<LabelList> subscriber = connectedNode.newSubscriber(LABEL_LIST, LabelList._TYPE);
        subscriber.addMessageListener(new MessageListener<LabelList>() {
            @Override
            public void onNewMessage(LabelList labelList) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (Label label : labelList.getLabelList()) {
                    Map<String, Object> list = new HashMap<>();
                    list.put("num", String.valueOf(label.getNum()));
                    list.put("image_x", label.getImageX());
                    list.put("image_y", label.getImageY());
                    list.put("image_w", label.getImageW());
                    list.put("image_d", label.getImageD());
                    list.put("state", label.getState());
                    list.put("x", label.getPose().getX());
                    list.put("y", label.getPose().getY());
                    list.put("w", label.getPose().getTheta());
                    resultList.add(list);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("data", resultList);
                MessageDto messageDto = new MessageDto();
                messageDto.setNode_name("debugging");
                messageDto.setPage_id("0x0601");
                messageDto.setSub_page_id("0x0000");
                messageDto.setCtrl_id("0x0007");
                messageDto.setType("labelMapSend");
                messageDto.setStatus(1);
                messageDto.setPara(data);
                Subject.broadcast(messageDto.toString());
            }
        });
    }*/
}
