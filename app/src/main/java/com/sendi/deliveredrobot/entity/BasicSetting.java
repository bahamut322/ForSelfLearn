package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;


/**
 * @author swn
 * 基础设置的LitePal数据库
 */
public class BasicSetting extends LitePalSupport {
    private String defaultValue;//首页功能显示
    private String robotMode;//机器人音色

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRobotMode() {
        return robotMode;
    }

    public void setRobotMode(String robotMode) {
        this.robotMode = robotMode;
    }
}

