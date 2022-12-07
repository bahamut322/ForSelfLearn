package com.sendi.deliveredrobot.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @description: 机器人实时数据上报 per 5s
 * @author: Sunzecong
 * @date: 2021/11/2
 */
public class RobotPoseLiveData {
    private String type;
    /**
     * 机器人位置
     */
    private float[] robotPose;
    /**
     * 实时激光地图
     */
    private float[] updateMap;
    /**
     * 任务状态：RobotStageEnum
     */
    private int taskStatus;
    /**
     * 电量
     */
    private int power;
    /**
     * 当前楼层
     */
    private String floor;
    /**
     * 时间
     */
    private long time;
    /**
     * 配送点
     */
    private String target;
    /**
     * 正在前往目标点:TaskStageEnum
     */
    private int targetStage;

    private String endTarget;

    /**
     * 下一个最终目标点
     */
    private String nextTarget;

    /**
     * 任务模式 10-客房送物、11-引领、20-跑腿帮取、21-跑腿帮送
     */
    private int mode;

    /**
     * 剩余任务数
     */
    private int remain;

    public RobotPoseLiveData() {
    }

    public RobotPoseLiveData(float[] robotPose, float[] updateMap, int taskStatus, int power, String floor, long time, String target, int targetStage) {
        this.robotPose = robotPose;
        this.updateMap = updateMap;
        this.taskStatus = taskStatus;
        this.power = power;
        this.floor = floor;
        this.time = time;
        this.target = target;
        this.targetStage = targetStage;
    }

    public float[] getRobotPose() {
        return robotPose;
    }

    public void setRobotPose(float[] robotPose) {
        this.robotPose = robotPose;
    }

    public float[] getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(float[] updateMap) {
        this.updateMap = updateMap;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getTargetStage() {
        return targetStage;
    }

    public void setTargetStage(int targetStage) {
        this.targetStage = targetStage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndTarget() {
        return endTarget;
    }

    public void setEndTarget(String endTarget) {
        this.endTarget = endTarget;
    }

    public String getNextTarget() {
        return nextTarget;
    }

    public void setNextTarget(String nextTarget) {
        this.nextTarget = nextTarget;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getRemain() {
        return remain;
    }

    public void setRemain(int remain) {
        this.remain = remain;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
