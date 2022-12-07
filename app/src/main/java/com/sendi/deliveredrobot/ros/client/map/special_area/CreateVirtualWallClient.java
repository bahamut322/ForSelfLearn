package com.sendi.deliveredrobot.ros.client.map.special_area;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.CreateVirtualWallResponse;

/**
 * 0 "chaohui" ""               加载虚拟墙
 * 1 "chaohui" ""		        预览全部虚拟墙        返回的虚拟墙坐标
 * 1 "chaohui" "line1"          预览单个虚拟墙        返回的虚拟墙坐标
 * 2 "chaohui" ""		        获取虚拟墙列表
 * 3 "" ""		                开始录制              发布 /temp_obstacle 话题 消息格式： sensor_msgs::PointCloud
 * 4 "" "line1"		            结束录制并保存
 * 4 "" ""                      结束录制不保存
 * 5 "chaohui" ""               保存
 * 5 "" ""                      不保存, 并且所有清理缓存，复位状态
 * 6 "chaohui" "line1"		    删除单个文件
 * 6 "chaohui" ""		        删除整个目录
 */
@ServiceClient(ClientConstant.CREATE_VIRTUAL_WALL)
public class CreateVirtualWallClient extends IAbstractClient {
    private CreateVirtualWallResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CreateVirtualWallResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public CreateVirtualWallResponse getResponse() {
        try {
            countDownLatch.await(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException | NullPointerException e) {
            throw new RuntimeException(RosResultEnum.GET_RESPONSE_ERROR.getMsg());
        }
        return response;
    }

    /*public void responseHandler(MessageDto message) {
        Map<String, Object> data = new HashMap<>();
        if (null != getResponse()) {
            int state = response.getState();
            message.setPara(data);
            switch (state) {
                case 1:
                    // 1. start_sign = false
                    // 2. unsub
                    SubManager.unsub(ClientConstant.TEMP_OBSTACLE);
                    message.setStatus(1);
                    data.put("msg", "成功");
                    break;
                case 0:
                    message.setStatus(0);
                    data.put("msg", "失败");
                    break;
                case 2:
                    message.setStatus(0);
                    data.put("msg", "命令错误");
                    break;
                default:
                    message.setStatus(0);
                    data.put("msg", "未定义");
                    break;
            }
        } else {
            data.put("msg", "CREATE_VIRTUAL_WALL 服务不可用");
            message.setStatus(0);
        }
    }*/
}
