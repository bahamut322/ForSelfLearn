package com.sendi.deliveredrobot.ros.client.map.label;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.InitResponse;

@ServiceClient(ClientConstant.LABEL_INIT)
public class LabelInitClient extends IAbstractClient {
    private InitResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), InitResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public InitResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
    /* public void responseHandler(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            switch (response.getResult()) {
                case 1:
                    data.put("msg", "成功");
                    message.setStatus(1);
                    //运行监听标签点  开启订阅
//                    nodeMainExecutor.execute(labelListSub, nodeConfiguration);
                    SubManager.sub(ClientConstant.LABEL_LIST);
                    break;
                case -1:
                    data.put("msg", "未知错误");
                    break;
                case -2:
                    data.put("msg", "状态错误");
                    break;
                case -3:
                    data.put("msg", "传感器失败");
                    break;
                case -5:
                    data.put("msg", "加载地图失败");
                    break;
                case -6:
                    data.put("msg", "原始数据失败");
                    break;
                case -7:
                    data.put("msg", "保存地图失败");
                    break;
                case -8:
                    data.put("msg", "文件名缺失失败");
                    break;
                case -10:
                    data.put("msg", "运行线程重复失败");
                    break;
                default:
                    data.put("msg", "未定义失败");
                    break;
            }
            message.setPara(data);
        } else {
            message.setStatus(0);
        }
    }*/
}
