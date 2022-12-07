package com.sendi.deliveredrobot.ros.client.map.laser;

import com.sendi.deliveredrobot.ros.RosPointArrUtil;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.ContainerTypeEnum;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

/**
 * create by yujx
 *
 * @data 2021/07/26
 */
@ServiceClient(ClientConstant.MOVE_SUB_MAP)
public class MoveSubMapClient extends IAbstractClient {
    private boolean dealResult;

    @Override
    public void callbackMessageHandle(String resultMessage) {
        dealResult = false;
        int result = RosPointArrUtil.setResult(resultMessage);
        if (result == 1) {
            // 设置子图
            dealResult = RosPointArrUtil.parseIntArrMapPoint(resultMessage, ContainerTypeEnum.UPDATE_MAP);
        }
        countDownLatch.countDown();
    }

    @Override
    public Boolean getResponse() {
        try {
            countDownLatch.await(LASER_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return dealResult;
    }
}
