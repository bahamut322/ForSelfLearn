package com.sendi.deliveredrobot.model;

import org.litepal.annotation.Column;

public class BasicModel {

    private String defaultValue = "智能引领 智能讲解 更多服务 业务办理 礼仪迎宾 ";//首页功能显示
    private int id;
    private String robotMode;//机器人音色
    private Boolean Intelligent;//智能语音
    private Boolean Etiquette;//礼仪迎宾
    private int voiceVolume = 12;//语音音量
    private int videoVolume = 0; //视频音量
    private Boolean expression;//操作屏表情
    private Boolean identifyVip;//识别Vip人脸迎宾
    private float leadingSpeed = 0.8F;//引领速度
    private float goExplanationPoint = 0.8F;//去往讲解点速度
    private int speechSpeed = 12;//讲解语速
    private int stayTime = 30;//逗留时间
    private int unArrive;//不能到达点 0：直接下一个;1：重复第二次到达
    private int explanationFinish;//讲解结束允许 0、再讲一遍 1、选择其他路线
    private Boolean explainInterrupt = true;//讲解过程中允许打断
    private int explainWhetherTime = 30;//打断任务暂停时间
    private String patrolContent ;//巡逻内容 0：口罩检测;1：体温检测;2：人脸识别;
    private String Error;//异常警告方式 0、语音播报 1、就近跟随
    private float patrolSpeed;//巡逻速度
    private int patrolStayTime;//巡逻过程中暂停时间
    private int tempMode;//测温模式：0、单人测温 1、多人测温
    private Boolean voiceAnnouncements;//智能测温语音播报
    private float goBusinessPoint = 0.8F;//去往导购速度
    private boolean businessInterrupt = true;//导购过程中允许打断
    private int businessWhetherTime = 30;//打断任务暂停时间
    private int oneKeyCallPhone = 1;//一键呼叫电话 0、不开启 1、开启


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRobotMode() {
        return robotMode;
    }

    public void setRobotMode(String robotMode) {
        this.robotMode = robotMode;
    }

    public Boolean getIntelligent() {
        return Intelligent;
    }

    public void setIntelligent(Boolean intelligent) {
        Intelligent = intelligent;
    }

    public Boolean getEtiquette() {
        return Etiquette;
    }

    public void setEtiquette(Boolean etiquette) {
        Etiquette = etiquette;
    }

    public int getVoiceVolume() {
        return voiceVolume;
    }

    public void setVoiceVolume(int voiceVolume) {
        this.voiceVolume = voiceVolume;
    }

    public int getVideoVolume() {
        return videoVolume;
    }

    public void setVideoVolume(int videoVolume) {
        this.videoVolume = videoVolume;
    }

    public Boolean getExpression() {
        return expression;
    }

    public void setExpression(Boolean expression) {
        this.expression = expression;
    }

    public Boolean getIdentifyVip() {
        return identifyVip;
    }

    public void setIdentifyVip(Boolean identifyVip) {
        this.identifyVip = identifyVip;
    }

    public float getLeadingSpeed() {
        return leadingSpeed;
    }

    public void setLeadingSpeed(float leadingSpeed) {
        this.leadingSpeed = leadingSpeed;
    }

    public float getGoExplanationPoint() {
        return goExplanationPoint;
    }

    public void setGoExplanationPoint(float goExplanationPoint) {
        this.goExplanationPoint = goExplanationPoint;
    }

    public int getSpeechSpeed() {
        return speechSpeed;
    }

    public void setSpeechSpeed(int speechSpeed) {
        this.speechSpeed = speechSpeed;
    }

    public int getStayTime() {
        return stayTime;
    }

    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }

    public int getUnArrive() {
        return unArrive;
    }

    public void setUnArrive(int unArrive) {
        this.unArrive = unArrive;
    }

    public int getExplanationFinish() {
        return explanationFinish;
    }

    public void setExplanationFinish(int explanationFinish) {
        this.explanationFinish = explanationFinish;
    }

    public Boolean getExplainInterrupt() {
        return explainInterrupt;
    }

    public void setExplainInterrupt(Boolean explainInterrupt) {
        this.explainInterrupt = explainInterrupt;
    }

    public int getExplainWhetherTime() {
        return explainWhetherTime;
    }

    public void setExplainWhetherTime(int explainWhetherTime) {
        this.explainWhetherTime = explainWhetherTime;
    }

    public float getGoBusinessPoint() {
        return goBusinessPoint;
    }

    public void setGoBusinessPoint(float goBusinessPoint) {
        this.goBusinessPoint = goBusinessPoint;
    }

    public boolean getBusinessInterrupt() {
        return businessInterrupt;
    }

    public void setBusinessInterrupt(boolean businessInterrupt) {
        this.businessInterrupt = businessInterrupt;
    }

    public int getBusinessWhetherTime() {
        return businessWhetherTime;
    }

    public void setBusinessWhetherTime(int businessWhetherTime) {
        this.businessWhetherTime = businessWhetherTime;
    }

    public String getPatrolContent() {
        return patrolContent;
    }

    public void setPatrolContent(String patrolContent) {
        this.patrolContent = patrolContent;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    public float getPatrolSpeed() {
        return patrolSpeed;
    }

    public void setPatrolSpeed(float patrolSpeed) {
        this.patrolSpeed = patrolSpeed;
    }

    public int getPatrolStayTime() {
        return patrolStayTime;
    }

    public void setPatrolStayTime(int patrolStayTime) {
        this.patrolStayTime = patrolStayTime;
    }

    public int getTempMode() {
        return tempMode;
    }

    public void setTempMode(int tempMode) {
        this.tempMode = tempMode;
    }

    public Boolean getVoiceAnnouncements() {
        return voiceAnnouncements;
    }

    public void setVoiceAnnouncements(Boolean voiceAnnouncements) {
        this.voiceAnnouncements = voiceAnnouncements;
    }

    public void setOneKeyCallPhone(int oneKeyCallPhone) {
        this.oneKeyCallPhone = oneKeyCallPhone;
    }
    public int getOneKeyCallPhone() {
        return oneKeyCallPhone;
    }
}
