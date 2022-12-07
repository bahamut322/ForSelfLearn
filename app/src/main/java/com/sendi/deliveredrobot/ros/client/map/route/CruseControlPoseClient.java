package com.sendi.deliveredrobot.ros.client.map.route;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.CruseControlPointsResponse;


/**
 * 保存地图
 *
 * @author yujx
 * @version 1.0
 * @CreateDate: 2021/3/5
 */
@ServiceClient(ClientConstant.CRUSE_CONTROL_POINTS)
public class CruseControlPoseClient extends IAbstractClient {
    private CruseControlPointsResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CruseControlPointsResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public CruseControlPointsResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }
/*
    public void responseHandle(MessageDto message, String mapName) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int result = response.getResult();
            if (result == 1) {
                int sequence = 0;
                List<Pose2D> result_list = response.getPosesOnCrusePath();
                Log.i(this.getClass().getSimpleName(), "PosesOnCrusePath: " + result_list);
                int mapId = bmMapBusinessDao.selectIdByName(mapName);
                for (Pose2D d : result_list) {
                    BmMapInfoCruise bmMapInfoCruise = new BmMapInfoCruise();
                    bmMapInfoCruise.setX(d.getX());
                    bmMapInfoCruise.setY(d.getY());
                    bmMapInfoCruise.setW(d.getTheta());
                    bmMapInfoCruise.setSequence(sequence++);
                    bmMapInfoCruise.setMapId(mapId);
                    bmMapInfoCruiseDao.insert(bmMapInfoCruise);
                }
                message.setStatus(1);
                data.put("msg", "成功");
                message.setPara(data);
            } else {
                data.put("msg", "调用CruseControlPoints服务错误");
                message.setStatus(0);
                message.setPara(data);
            }
        } else {
            data.put("msg", "CruseControlPoints服务不可用");
            message.setStatus(0);
            message.setPara(data);
        }
    }*/
}
