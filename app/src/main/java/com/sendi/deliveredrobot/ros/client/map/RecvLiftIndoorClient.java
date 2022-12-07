package com.sendi.deliveredrobot.ros.client.map;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.MoveToResponse;


/**
 * 发送电梯内外点给底盘
 *
 * @author Sunzecong
 * @CreateDate: 2021/10/22
 */
@ServiceClient(ClientConstant.RECV_LIFT_INDOOR)
public class RecvLiftIndoorClient extends IAbstractClient {
    private MoveToResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), MoveToResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public MoveToResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

//    public void responseHandle(MessageDto message) {
//        Map<String, Object> data = new HashMap<>();
//        if (null != getResponse()) {
//            int result = response.getResult();
//            if (result == 1) {
//                data.put("msg", "成功");
//                message.setStatus(1);
//            } else {
//                data.put("msg", "原始文件删除失败");
//                message.setStatus(0);
//            }
//        } else {
//            data.put("msg", "原始文件删除失败");
//            message.setStatus(0);
//        }
//        message.setPara(data);
//    }

}
