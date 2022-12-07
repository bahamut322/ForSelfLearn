package com.sendi.deliveredrobot.ros.client.navigation;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.ManageResponse;


/**
 * 设置导航控制状态
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/5
 */
@ServiceClient(ClientConstant.MANAGE)
public class CallNavigationManageClient extends IAbstractClient {
    private ManageResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), ManageResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public ManageResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
    /*switch (response.getResult()) {
        case 1:
            status = 1;
            msg = "成功";
            break;
        case -2:
            LogUtil.e("State machine error");
            status = 0;
            msg = "导航异常-不符合状态机";
            break;
        default:
            LogUtil.e("Other mistakes, result = " + result);
            status = 0;
            msg = "导航异常-未知错误";
            break;
    }*/
}
