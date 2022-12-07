package com.sendi.deliveredrobot.ros.subscribe;

import com.alibaba.fastjson.JSONObject;
import com.sendi.deliveredrobot.ros.annotation.Subscribe;
import com.sendi.deliveredrobot.ros.constant.ClientConstant;
import com.sendi.deliveredrobot.ros.constant.JRosbridgeConstant;
import com.sendi.deliveredrobot.ros.dto.IAbstractClient;
import com.sendi.deliveredrobot.ros.dto.RosResultUtil;
import com.sendi.deliveredrobot.ros.observable.Subject;

import navigation_base_msgs.State;

/**
 * 进入感应门控制区域
 */
@Subscribe(ClientConstant.DOOR_CONTROL)
public class DoorControlSub extends IAbstractClient {
    private State response;


    @Override
    public boolean send(String request) {
        return false;
    }

    @Override
    public void callbackMessageHandle(String message) {
        JSONObject resJson = JSONObject.parseObject(message);
        this.response = JSONObject.parseObject(resJson.getString(JRosbridgeConstant.FIELD_MESSAGE), State.class);
        Subject.broadcast(RosResultUtil.success(ClientConstant.DOOR_CONTROL, response));
    }

    @Override
    public State getResponse() {
        return this.response;
    }

    // 处理逻辑
    /*
    private final int openCmd = 1;
    private final int closeCmd = 0;

    public void control(int state) throws InterruptedException {
        // deal topic once before different cmd
        if (RobotStatus.autoDoorCmd == state) return;
        RobotStatus.autoDoorCmd = state;
        if (openCmd <= state) {
            boolean hasPause = false;
            LogUtil.saveLogsAndOut(Constant.DOOR_CONTROL, "感应门控制-开启");
            // navigation is turning, can't stop
            RobotStatus.transNavigationMode.compareAndSet(false, true);
            // 1.stop navigation
            int retry = 0;
            while (true) {
                int navigationState = RobotStatus.navigationState.get();
                if (navigationState != 3 && navigationState != 5) break;
                ClientResult clientResult = iIntegrateControl.setNavigationManage(2);
                if (clientResult.getStatus() == Constant.SUCCESS_CLIENT_STATUS) {
                    LogUtil.saveLogsAndOut(Constant.DOOR_CONTROL, "感应门控制-暂停导航成功");
                    hasPause = true;
                    break;
                }
                if (++retry > 3) {
                    LogUtil.e(Constant.DOOR_CONTROL + "感应门控制-暂停导航失败");
                    ICommonPageService.stopTheWorld("感应门控制-暂停导航失败");
                    return;
                }
                TimeUnit.MILLISECONDS.sleep(300L);
            }
            // 2.send open order
            TimerController.start(TimerEnum.DOOR_CONTROL_TIMER, 3000L, true);
            // 3.wait for 3 seconds
            TimeUnit.SECONDS.sleep(7);
            // 4.resume navigation
            if (hasPause && RobotStatus.navigationState.get() == 2) {
                ClientResult clientResult = iIntegrateControl.setNavigationManage(3);
                if (clientResult.getStatus() == Constant.FAIL_CLIENT_STATUS) {
                    TimerController.stop(TimerEnum.DOOR_CONTROL_TIMER);
                    ICommonPageService.stopTheWorld("感应门控制-恢复导航失败");
                }
            }
        } else if (closeCmd == state) {
            LogUtil.saveLogsAndOut(Constant.DOOR_CONTROL, "感应门控制-关门");
            TimerController.stop(TimerEnum.DOOR_CONTROL_TIMER);
        }
        RobotStatus.transNavigationMode.compareAndSet(true, false);
    }*/
}