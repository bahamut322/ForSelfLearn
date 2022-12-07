package com.sendi.deliveredrobot.ros.client.navigation;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import map_msgs.Set_poseResponse;
import navigation_base_msgs.CruiseMoveToResponse;


/**
 * 参数说明：
 * string label_map_name
 * string cruise_map_name
 * geometry_msgs/Pose target_pose
 * ---
 * int32 result
 *
 *  label_map_name 为需要设置的地图名称
 *  target_pose.position的x,y,z表示充电桩的x,y,w
 *  target_pose.orientation的x,y,w表示上一次接触充电片时里程计的x,y,w
 *
 *
 *  返回值result 为1 表示地图已经设置成功，后面可以直接导航
 *  返回值result 不为1 时，表示客户需要把机器人推到充电桩接触充电片，需要弹出提示
 */
@ServiceClient(ClientConstant.CALCULATE_CHARGE_POSE)
public class CalculateChargePoseClient extends IAbstractClient {
    private CruiseMoveToResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CruiseMoveToResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public CruiseMoveToResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

}
