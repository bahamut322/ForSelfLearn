package com.sendi.deliveredrobot.ros.client.map;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.AdvanMoveToResponse;


/**
 * 移动到修复点
 *
 * @CreateDate: 2021/2/25
 */
@ServiceClient(ClientConstant.ADVAN_MOVE_TO)
public class AdvanMoveToClient extends IAbstractClient {
    private AdvanMoveToResponse response;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        JSONObject resJson = JSONObject.parseObject(resultMessage);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), AdvanMoveToResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public AdvanMoveToResponse getResponse() {
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
