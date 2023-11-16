package com.sendi.deliveredrobot.entity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;


/**
 * @author swn
 * 基础设置的LitePal数据库
 */
public class BasicSetting extends LitePalSupport {

    private String defaultValue = "智能引领 智能讲解 轻应用 业务办理 礼仪迎宾 ";//首页功能显示
    @Column(defaultValue = "1")
    private int id = 1;
    @Column(defaultValue = "女声")
    private String robotMode = "女声";//机器人音色
    @Column(defaultValue = "false")
    private Boolean Intelligent = false;//智能语音
    @Column(defaultValue = "false")
    private Boolean Etiquette = false;//礼仪迎宾
    @Column(defaultValue = "0")
    private float voiceVolume = 10;//语音音量
    @Column(defaultValue = "0")
    private float videoVolume = 10; //视频音量
    @Column(defaultValue = "true")
    private Boolean expression = true;//操作屏表情
    @Column(defaultValue = "true")
    private Boolean identifyVip = true;//识别Vip人脸迎宾
    @Column(defaultValue = "0.3")
    private float leadingSpeed = (float) 0.8;//引领速度
    @Column(defaultValue = "0.3")
    private float goExplanationPoint = (float) 0.8;//去往讲解点速度
    @Column(defaultValue = "1")
    private int speechSpeed = 10;//讲解语速
    @Column(defaultValue = "1")
    private int stayTime = 20;//逗留时间
    @Column(defaultValue = "1")
    private int unArrive = 1;//不能到达点 0：直接下一个;1：重复第二次到达
    @Column(defaultValue = "1")
    private int explanationFinish  = 1;//讲解结束允许 0、再讲一遍 1、选择其他路线
    @Column(defaultValue = "true")
    private Boolean explainInterrupt = true;//讲解过程中允许打断
    @Column(defaultValue = "1")
    private int explainWhetherTime = 1;//打断任务暂停时间
    @Column(defaultValue = "1")
    private String patrolContent = "1";//巡逻内容 0：口罩检测;1：体温检测;2：人脸识别;
    @Column(defaultValue = "1")
    private String Error = "1";//异常警告方式 0、语音播报 1、就近跟随
    @Column(defaultValue = "0.3")
    private float patrolSpeed = (float) 0.8;//巡逻速度
    @Column(defaultValue = "1")
    private int patrolStayTime = 20;//巡逻过程中暂停时间
    @Column(defaultValue = "1")
    private int tempMode = 1;//测温模式：0、单人测温 1、多人测温
    @Column(defaultValue = "false")
    private Boolean voiceAnnouncements = false;//智能测温语音播报
    @Column(defaultValue = "0.3")
    private float goBusinessPoint = (float) 0.8;//去往导购速度
    @Column(defaultValue = "true")
    private boolean businessInterrupt = true;//导购过程中允许打断
    @Column(defaultValue = "1")
    private int businessWhetherTime = 20;//打断任务暂停时间


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getVoiceVolume() {
        return voiceVolume;
    }

    public void setVoiceVolume(float voiceVolume) {
        this.voiceVolume = voiceVolume;
    }

    public float getVideoVolume() {
        return videoVolume;
    }

    public void setVideoVolume(float videoVolume) {
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

