package com.sendi.deliveredrobot.ros.subscribe;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.model.ChassisLogModel;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.service.CloudMqttService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * create by yujx
 * 底盘日志上传
 *
 * @date 2022/02/18
 */
@Subscribe(ClientConstant.CHASSIS_MSGS_TOPIC)
public class UploadErrorLogSub extends IAbstractClient {
    private static String error_repeat_log = "";

    @Override
    public boolean send(String request) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        String error_log = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), std_msgs.String.class).getData();
        //判断报文是否是重复报文
        if (error_repeat_log.equals(error_log)) {
            return;
        }
        error_repeat_log = error_log;
        LocalDateTime localDateTime = LocalDateTime.now();
        String time = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ChassisLogModel chassisLogModel = new ChassisLogModel(time, error_log);
        CloudMqttService.Companion.publish(chassisLogModel.toString(),true,2);
    }

    @Override
    public <T> T getResponse() {
        return null;
    }
}
