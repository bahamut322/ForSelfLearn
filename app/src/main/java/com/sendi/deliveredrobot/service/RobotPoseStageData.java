package com.sendi.deliveredrobot.service;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @description: 机器人任务节点数据上报
 * @author: Sunzecong
 * @date: 2021/11/8
 */
public class RobotPoseStageData {
    /**
     * 任务列表
     */
    public List<TaskDto> taskList;

    private String type;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
