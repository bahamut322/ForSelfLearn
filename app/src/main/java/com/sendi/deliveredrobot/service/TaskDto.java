package com.sendi.deliveredrobot.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @description: 机器人任务节点数据实体类
 * @author: Sunzecong
 * @date: 2021/11/8
 */
public class TaskDto {
    /**
     * 任务类型: 引领 - 0/送物 - 1
     * 任务ID:
     * 类型 + 仓门 + 年月日时分
     * 引领: G0202111021637
     * 送物: D1202111021637、D2202111021637（0 - 无使用仓门/1 - 1号仓/2 - 2号仓）
     * 迎宾：U0202111021637
     */
    private String taskId;
    /**
     * 目标点
     */
    private String target;
    /**
     * 机器人位置
     */
    private float[] robotPose;
    /**
     * 实时激光地图
     */
    private float[] updateMap;
    /**
     * 任务节点
     */
    private int taskStage;
    /**
     * 任务状态：1 - 完成/-1 - 异常/0 - 失败
     */
    private int status;
    /**
     * 时间
     */
    private long time;

    /**
     * 舱门编号
     */
    private int gate;

    /**
     * 最终目标点
     */
    private String endTarget;

    /**
     * 里程数
     */
    private int mileage;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public int getTaskStage() {
        return taskStage;
    }

    public void setTaskStage(int taskStage) {
        this.taskStage = taskStage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getGate() {
        return gate;
    }

    public void setGate(int gate) {
        this.gate = gate;
    }

    public String getEndTarget() {
        return endTarget;
    }

    public void setEndTarget(String endTarget) {
        this.endTarget = endTarget;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
