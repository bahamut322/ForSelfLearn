package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import org.ros.internal.message.Message;

import nav_msgs.Odometry;


@Subscribe(ClientConstant.SCHEDULING_PAGE)
public class SchedulingSub extends IAbstractClient {
//    @Autowired
//    private CallSchResumeOldGoalClient callSchResumeOldGoalClient;
//    @Autowired
//    private SetParamClient setParamClient;

    private Odometry response;

    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        // 假设收到订阅消息类moveToRequest
        // TODO: 2021/5/24 等待汉煊改好底层接口 1.修改已经ack 2.放入数据 3.修改已处理完成
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), Odometry.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.SCHEDULING_PAGE, response));
    }

    @Override
    public Message getResponse() {
        return this.response;
    }

    // 处理逻辑
    /*public void control(MoveToRequest moveToRequest) {
        double x = moveToRequest.getTargetPose().getPosition().getX();
        double y = moveToRequest.getTargetPose().getPosition().getY();
        double z = 0.0;
        Quaternion orientation = moveToRequest.getTargetPose().getOrientation();
        int dockDirection = moveToRequest.getDockDirection();
        // 清空标志位
        IntegrateConstant.dispatchStatus = 0;
        // call client
        HashMap<String, Object> clientPara = new HashMap<>();
        // target_pose
        JSONObject targetPose = new JSONObject();
        JSONObject position = new JSONObject();
        position.put("x", x);
        position.put("y", y);
        position.put("z", z);
        targetPose.put("position", position);
        targetPose.put("orientation", orientation);
        // clientPara
        clientPara.put("target_pose", targetPose);
        clientPara.put("dock_direction", dockDirection);
        ServiceClientPar service = new ServiceClientPar(SCH_CHANGE_GOAL, clientPara);
        callSchResumeOldGoalClient.send(service.toString());
        MoveToResponse response = callSchResumeOldGoalClient.getResponse();
        if (response.getResult() == 1) {
//                                         -2: "State machine error"
//                                         -3: "Running exists"
//                                        -24: "Not label"
            IntegrateConstant.dispatchStatus = 1;
            //2. 把当前配送中的任务改为未配送
            for (TaskEntity task : IntegrateConstant.taskVec) {
                if (task.getStatus().equals("配送中")) {
                    task.setStatus("未配送");
                    break;
                }
            }
            //3. 获取到调度任务的坐标信息，并插入到头部
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setName("调度");
            taskEntity.setStatus("配送中");
            taskEntity.setDirection(dockDirection == 1 ? "双向" : "单向");
            taskEntity.setRoom_type("调度");
            taskEntity.setX(x);
            taskEntity.setY(y);
            taskEntity.setW(0.0);//不需要使用的角度
            IntegrateConstant.taskVec.addFirst(taskEntity);
        }
    }*/
}
