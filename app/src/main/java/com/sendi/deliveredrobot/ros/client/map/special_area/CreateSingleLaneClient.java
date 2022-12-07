package com.sendi.deliveredrobot.ros.client.map.special_area;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.CreateOneWayResponse;

/**
 * -- 1 "chaohui" "area1" 0.0          预览单个单行道      返回单个单行道的 路径点坐标,pose1,pose2,range
 * -- 2 "chaohui" "" 0.0               获取单行道列表
 * -- 3 "" "" 0.0                      (开始)录制        发布 /temp_obstacle 话题 消息格式： sensor_msgs::PointCloud
 * -- 4 "" "" 0.0                      (重置)
 * -- 5 "" "area1" 0.0                 记录路径(结束)
 * -- 6 "" "area1" 0.0                 标记控制点          返回控制点 pose1 坐标
 * -- 7 "" "area1" 0.0                 标记停靠点          返回停靠点 pose2 坐标
 * -- 8 "" "area1" 2.0                 记录半径
 * -- 9 "chaohui" "" 0.0               保存
 * -- 9 "" "" 0.0                      (上一级)不保存, 并且所有清理缓存，复位状态
 * -- 10 "chaohui" "area1" 0.0         删除单个文件
 * -- 10 "chaohui" "" 0.0              删除整个目录
 * -- 11 "chaohui" "area1" 0.0         点击修改单个文件(选中时，在弹框之前下发) 返回range
 * 涉及控制点和停靠点修改发送指令6和7，点击参数页面的保存按钮下发指令8，修改之后要点保存才有效指令9
 */
@ServiceClient(ClientConstant.CREATE_SINGLE_LANE)
public class CreateSingleLaneClient extends IAbstractClient {
    private CreateOneWayResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CreateOneWayResponse.class);
        } else {
            this.response = null;
        }
        countDownLatch.countDown();
    }

    @Override
    public CreateOneWayResponse getResponse() {
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
                    SubManager.unsub(ClientConstant.CREATE_SINGLE_LANE);
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
