package com.sendi.deliveredrobot.ros;

import static com.sendi.deliveredrobot.ros.constant.ClientConstant.BATTERY_STATE;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.LASER_SCAN;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.NAVIGATION_STATE_TOPIC;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.SAFE_STATE_TOPIC;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.SCHEDULING_CHANGE_GOAL;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.SCHEDULING_PAGE;
import static com.sendi.deliveredrobot.ros.constant.ClientConstant.VOICE_PROMPT_TOPIC;
import static com.sendi.deliveredrobot.ros.constant.Constant.CMD;

import android.content.Context;

import com.sendi.deliveredrobot.MainActivity;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.dto.Client;
import com.sendi.deliveredrobot.ros.dto.RosResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import sendi_sensor_msgs.InfraredManageResponse;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // ======================================初始化（放在app启动时）======================================
        /**
         * 1.初始化webSocket连接和DispatchService类加载注入
         * 2.RosWebSocketManager.isConnect.get() == true 才能开始订阅 topic
         * setPreSub()  -> add(topic) -> init -> sub(list)
         */
        Context context = new MainActivity();// 随便写写
        List<String> preTopicList = Arrays.asList(SCHEDULING_PAGE, SAFE_STATE_TOPIC, LASER_SCAN, BATTERY_STATE, NAVIGATION_STATE_TOPIC, SCHEDULING_CHANGE_GOAL, VOICE_PROMPT_TOPIC);
        DispatchService.initRosBridge("com.sendi.deliveredrobot", context);
        DispatchService.subInitTopic(preTopicList);

        // ======================================调用服务======================================
        // 1.构建参数，无参调用可不传
        HashMap<String, Object> clientPara = new HashMap<>();
        clientPara.put(CMD, 2);
        Client client = new Client(ClientConstant.INFRARED_MANAGE, clientPara);
        // 2.调用ClientManager方法，接收返回值
        RosResult<InfraredManageResponse> rosResult = ClientManager.sendClientMsg(client, InfraredManageResponse.class);
        // response -> 根据调用不同的服务有不同的返回值类型<M extends Message>
        InfraredManageResponse response = rosResult.getResponse();
        // msg -> 失败时会返回错误类型
        String msg = rosResult.getMsg();
        // url
        String url = rosResult.getUrl();
        if (response != null && response.getResult() == 1) {
            /*
             * 业务代码...可以参考每个Client类的responseHandler方法或者注释
             * */
        }

        // ======================================订阅topic======================================
        /**
         * 代码初始化完成之后，会将所有topic添加到SubManager中
         */
        // 1.订阅所有topic
        SubManager.subUnsubscribeTopic();
        // 2.取消订阅并移除topic（只需要接收一次topic发布的消息，例如自检时镭射扫描状态上报，接收一次之后可以取消订阅）
        SubManager.delSub(LASER_SCAN);
        // 3.所有的topic消息发布会调用 callbackMessageHandle() 将 message 通过广播推送 -> 详见 ObserverPatternDemo
    }


}
