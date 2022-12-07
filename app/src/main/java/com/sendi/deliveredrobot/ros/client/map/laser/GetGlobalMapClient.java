package com.sendi.deliveredrobot.ros.client.map.laser;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.GET_GLOBAL_MAP;

import com.sendi.deliveredrobot.ros.RosPointArrUtil;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ContainerTypeEnum;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import map_msgs.Init_locationResponse;

/**
 * create by yujx
 *
 * @data 2021/07/26
 */
@ServiceClient(GET_GLOBAL_MAP)
public class GetGlobalMapClient extends IAbstractClient {
    private Init_locationResponse response;
    private boolean dealResult;

    @Override
    public void callbackMessageHandle(String message) {
        dealResult = false;
        int result = RosPointArrUtil.setResult(message);
        if (result == 1) {
            // --------------- 思政看这里 - PointCloud小矮机子专用 ---------------
//            dealResult = RosPointArrUtil.parsePointCloudMapPoint(message, ContainerTypeEnum.STATIC_MAP);
            // 设置总图
            dealResult = RosPointArrUtil.parseIntArrMapPoint(message, ContainerTypeEnum.STATIC_MAP);
            // ---------------------------------------------------------------
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
