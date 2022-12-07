package com.sendi.deliveredrobot.service;

/**
 * @author heky
 * @describe liveData
 * @since 2021-11-17
 */
public enum RobotStageEnum {
    CHARGING(0, "充电中"),
    TASKING(1, "任务中"),
    BACKING(2, "返回中"),
    IDLE(-2, "空闲"),
    ERROR(-1, "异常");

    private int code;
    private String name;

    RobotStageEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
