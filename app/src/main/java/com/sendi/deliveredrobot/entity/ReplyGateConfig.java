package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

public class ReplyGateConfig  extends LitePalSupport {

    /**
     * 1 `robotId` (32) '机器人ID',
     * 2 `temperatureThreshold` float '温度阈值',
     * 3 `bigScreenType` int(2) '大屏应用类型 1-图片 2-视频 3-文字 4-图片+文字',
     * 4 `pics` (255) '文件名称，逗号分割',
     * 5 `picPlayType` int(1) '图片播放方式 1-轮播',
     * 6 `picPlayTime` int(5) '轮播间隔X秒',
     * 8 `videoFrame` (255) '视频第一帧关联图片ID',
     * 9 `videoAudio` tinyint(1) '视频是否播放声音',
     * 10 `fontContent` (255) '文字',
     * 11 `fontColor` (50) '文字颜色' 1～7 白黑蓝红绿橙紫,
     * 12 `fontSize` '文字大小 1-大，2-中，3-小',
     * 13 `fontLayout`  '文字布局 1-横向，2-纵向',
     * 14 `fontBackGround` '背景色  1～7 白黑蓝红绿橙紫',
     * 15 `tipsTemperatureInfo` (255) '温度正常提示',
     * 16 `tipsTemperatureWarn` (255) '温度异常提示',
     * 17 `tipsMaskWarn` (255) '口罩异常提示',
     * 18 `timeStamp` `配置生成时间戳`
     */

    private float temperatureThreshold;
    private int picPlayType;
    private int picPlayTime;
    private int videoAudio;
    private String fontContent;
    private String fontColor;
    private int fontSize;
    private int fontLayout;
    private String fontBackGround;
    private String tipsTemperatureInfo;
    private String tipsTemperatureWarn;
    private String tipsMaskWarn;
    private long timeStamp;
    private int bigScreenType;
    private String pics;
    private int picType;
    private int textPosition;

    private int videolayout;

    public int getVideolayout() {
        return videolayout;
    }

    public void setVideolayout(int videolayout) {
        this.videolayout = videolayout;
    }

    public int getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int textPosition) {
        this.textPosition = textPosition;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }

    public String getPics() {
        return pics;
    }

    public void setPics(String pics) {
        this.pics = pics;
    }

    public int getBigScreenType() {
        return bigScreenType;
    }

    public void setBigScreenType(int bigScreenType) {
        this.bigScreenType = bigScreenType;
    }

    public float getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public void setTemperatureThreshold(float temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }

    public int getPicPlayType() {
        return picPlayType;
    }

    public void setPicPlayType(int picPlayType) {
        this.picPlayType = picPlayType;
    }

    public int getPicPlayTime() {
        return picPlayTime;
    }

    public void setPicPlayTime(int picPlayTime) {
        this.picPlayTime = picPlayTime;
    }

    public int getVideoAudio() {
        return videoAudio;
    }

    public void setVideoAudio(int videoAudio) {
        this.videoAudio = videoAudio;
    }

    public String getFontContent() {
        return fontContent;
    }

    public void setFontContent(String fontContent) {
        this.fontContent = fontContent;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getFontLayout() {
        return fontLayout;
    }

    public void setFontLayout(int fontLayout) {
        this.fontLayout = fontLayout;
    }

    public String getFontBackGround() {
        return fontBackGround;
    }

    public void setFontBackGround(String fontBackGround) {
        this.fontBackGround = fontBackGround;
    }

    public String getTipsTemperatureInfo() {
        return tipsTemperatureInfo;
    }

    public void setTipsTemperatureInfo(String tipsTemperatureInfo) {
        this.tipsTemperatureInfo = tipsTemperatureInfo;
    }

    public String getTipsTemperatureWarn() {
        return tipsTemperatureWarn;
    }

    public void setTipsTemperatureWarn(String tipsTemperatureWarn) {
        this.tipsTemperatureWarn = tipsTemperatureWarn;
    }

    public String getTipsMaskWarn() {
        return tipsMaskWarn;
    }

    public void setTipsMaskWarn(String tipsMaskWarn) {
        this.tipsMaskWarn = tipsMaskWarn;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
