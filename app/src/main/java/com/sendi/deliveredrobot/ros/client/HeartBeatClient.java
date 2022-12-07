package com.sendi.deliveredrobot.ros.client;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import rosapi.GetTimeResponse;


/**
 * 查看当前有多少客户端连接
 *
 * @author Sunzecong
 * @version 1.0
 * @CreateDate: 2021/2/25
 * <p>
 * 2021/4/9 修改 webSocket版
 * liangjy
 */
@ServiceClient(ClientConstant.HEART_BEAT)
public class HeartBeatClient extends IAbstractClient {
    private GetTimeResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), GetTimeResponse.class);
        } else {
            this.response = null;
        }
    }

    @Override
    public GetTimeResponse getResponse() {
        return response;
    }
}
