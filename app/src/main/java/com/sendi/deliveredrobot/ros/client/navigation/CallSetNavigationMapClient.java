package com.sendi.deliveredrobot.ros.client.navigation;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.SetMoveMapResponse;


/**
 * 设置导航地图
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/2/7
 */
@ServiceClient(ClientConstant.SET_NAVIGATION_MAP)
public class CallSetNavigationMapClient extends IAbstractClient {
    private SetMoveMapResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), SetMoveMapResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public SetMoveMapResponse getResponse() {
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
                msg = "设置地图成功";
                break;
            case -2:
                status = 0;
                msg = "导航异常-状态错误";
                break;
            case -20:
                status = 0;
                msg = "导航异常-读图错误";
                break;
            case -22:
                status = 0;
                msg = "导航异常-地图名错误";
                break;
            case -23:
                status = 0;
                msg = "导航异常-地图为空";
                break;
            default:
                status = 0;
                msg = "导航异常-未知错误";
                break;
        }*/
}
