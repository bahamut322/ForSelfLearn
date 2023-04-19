package com.sendi.deliveredrobot.model;

/**
 * @Author Swn
 * @describe 云平台——机器人基础配置实体类
 * @Data 2023-04-19 10:24
 */
public class RobotConfigModel {
    private int id;//id
    private int sleep;//是否进入待机 1；启用 0：不启用
    private String password;//进入设置密码
    private String wakeUpList;// "唤醒方式 1-点击屏幕，2-检测到人脸，3-唤醒词",
    private String mapName;//当前选择地图名称
    private int sleepTime;//待机时间

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWakeUpList() {
        return wakeUpList;
    }

    public void setWakeUpList(String wakeUpList) {
        this.wakeUpList = wakeUpList;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
}
