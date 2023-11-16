package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

//机器人默认配置数据库
public class RobotConfigSql extends LitePalSupport {
    //注释原因：在基础设置统一管理
//    private int audioType;//声音类型
    private String wakeUpWord = "小迪小迪";//唤醒词
    private int sleep = 0;//是否启动用待机
    private  int sleepTime = 30;//多少分钟没操作进入待机
    private String wakeUpList;//唤醒方式
    private int sleepType;//待机内容
    private int picType;//图片布局
    private long timeStamp;//时间戳
    private String mapName;//地图名字
    private String password = "8888";//密码
    private String chargePointName;//充电桩点名称
    private String waitingPointName;//待命点名称

    public String getChargePointName() {
        return chargePointName;
    }

    public void setChargePointName(String chargePointName) {
        this.chargePointName = chargePointName;
    }

    public String getWaitingPointName() {
        return waitingPointName;
    }

    public void setWaitingPointName(String waitingPointName) {
        this.waitingPointName = waitingPointName;
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

//    public int getAudioType() {
//        return audioType;
//    }
//
//    public void setAudioType(int audioType) {
//        this.audioType = audioType;
//    }

    public String getWakeUpWord() {
        return wakeUpWord;
    }

    public void setWakeUpWord(String wakeUpWord) {
        this.wakeUpWord = wakeUpWord;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getSleepType() {
        return sleepType;
    }

    public void setSleepType(int sleepType) {
        this.sleepType = sleepType;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }
}
