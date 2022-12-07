package com.sendi.deliveredrobot.ros.client.map.label;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import label_msgs.EndResponse;


/**
 * 关闭标签
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/23
 */
@ServiceClient(ClientConstant.LABEL_END)
public class EndLabelClient extends IAbstractClient {

    private EndResponse endResponse;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.endResponse = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), EndResponse.class);
        } else {
            endResponse = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public EndResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return endResponse;
    }

   /* public void responseHandle(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = endResponse.getResult();
            switch (result) {
                case 1:
                    //全局变量，以便随时shutdown  关闭订阅
//                    if (InitClient.labelListSub != null)
//                        nodeMainExecutor.shutdownNodeMain(InitClient.labelListSub);
                    SubManager.unsub(ClientConstant.LABEL_LIST);
                    break;
                case -1:
                    data.put("msg", "建图失败");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                case -2:
                    data.put("msg", "状态错误");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                default:
                    data.put("msg", "未定义失败");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
            }
        } else {
            message.setStatus(0);
        }
    }*/

    // AckServiceImpl
   /* public void responseHandle(MessageDto message, String name, BmMapLabelDao bmMapLabelDao) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = endResponse.getResult();
            switch (result) {
                case 1:
                    String original_name = endResponse.getListName();
                    BmMapLabel record = new BmMapLabel();
                    record.setName(name);
                    record.setOriginalName(original_name);
                    bmMapLabelDao.insert(record);
                    //关闭订阅
//                                    if (InitClient.labelListSub != null){
//                                        nodeMainExecutor.shutdownNodeMain(InitClient.labelListSub);
//                                    }
                    SubManager.unsub(ClientConstant.LABEL_LIST);
                    data.put("msg", "成功");
                    message.setStatus(1);
                    message.setPara(data);
                    break;
                case -1:
                    data.put("msg", "建图失败");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                case -2:
                    data.put("msg", "状态错误");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
                default:
                    data.put("msg", "未定义失败");
                    message.setStatus(0);
                    message.setPara(data);
                    break;
            }
        } else {
            message.setStatus(0);
        }
    }*/
}
