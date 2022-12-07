package com.sendi.deliveredrobot.ros.client.map.special_area;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.ServiceClient;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.constant.RosResultEnum;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;

import java.util.concurrent.TimeUnit;

import navigation_base_msgs.CreateSlowAreaResponse;

/**
 * -- 1 "test" "area3" 0 0.0 0.0 0.0	 预览单个减速区域     返回的 减速区域路径坐标 及 类型、半径、速度、激光范围(类型不存在的参数，默认0)
 * -- 2 "test" "" 0 0.0 0.0 0.0          获取减速区域列表
 * -- 3 "" "" 0 0.0 0.0 0.0              (开始)录制            发布 /temp_obstacle 话题 消息格式： sensor_msgs::PointCloud
 * -- 4 "" "" 0 0.0 0.0 0.0              (重置)
 * -- 5 "" "area1" 0 0.0 0.0 0.0         记录路径(结束)      结束时会同时下发半径、速度、激光范围
 * -- 6 "" "area1" 0 0.5 0.4 0.5         下发类型、半径、速度、激光范围(类型不存在的参数，默认0)
 * -- 7 "test" "" 0 0.0 0.0 0.0          保存
 * -- 7 "" "" 0 0.0 0.0 0.0              (上一级)不保存, 并且所有清理缓存，复位状态
 * -- 8 "test" "area1" 0 0.0 0.0 0.0     删除单个文件
 * -- 8 "test" "" 0 0.0 0.0 0.0          删除整个目录
 * -- 9 "test" "area1" 0 0.0 0.0 0.0     点击修改单个文件(选中时，在弹框之前下发)    　  返回 类型、半径、速度、激光范围
 * 修改完成参数之后，点击保存，下发6指令， 还需要点击最外层的保存(指令7)才会写入文件
 */
@ServiceClient(ClientConstant.CREATE_SPEED_LIMIT_AREA)
public class CreateSpeedLimitAreaClient extends IAbstractClient {
    private CreateSlowAreaResponse response;

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        Boolean resultFlag = resJson.getBoolean(JRosbridgeConstant.FIELD_RESULT);
        if (resultFlag) {
            this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_VALUES), CreateSlowAreaResponse.class);
        }
        countDownLatch.countDown();
    }

    @Override
    public CreateSlowAreaResponse getResponse() {
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
                    SubManager.unsub(ClientConstant.TEMP_OBSTACLE);
                    message.setStatus(1);
                    data.put("msg", "限速区域指令下发-成功");
                    break;
                case 2:
                    message.setStatus(0);
                    data.put("msg", "限速区域指令下发-失败");
                    break;
                case 3:
                    message.setStatus(0);
                    data.put("msg", "限速区域指令下发-命令错误");
                    break;
                default:
                    message.setStatus(0);
                    data.put("msg", "限速区域指令下发-未定义错误");
                    break;
            }
        } else {
            data.put("msg", CREATE_SPEED_LIMIT_AREA + "服务不可用");
            message.setPara(data);
            message.setStatus(0);
        }
    }*/
}
