package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author swn
 * @describe 讲解配置大屏
 */
public class BigScreenConfigDB extends LitePalSupport {

    //配置类型
    private int type;
    //图片布局
    private int picType;
    //轮播时间
    private int picPlayTime;
    //文字
    private String fontContent;
    //文字颜色
    private String fontColor;
    //文字大小 1-大，2-中，3-小,
    private int fontSize;
    //文字方向 1-横向，2-纵向
    private int fontLayout;
    //背景颜色
    private String fontBackGround;
    //文字显示位置  0-居中 1-居上 2-居下
    private int textPosition;
    //视频是否播放声音
    private int videoAudio;
    //视频储存位置
    private String videoFile;
    //图片存储名字
    private String imageFile;

    private int videolayout;

    public int getVideolayout() {
        return videolayout;
    }

    public void setVideolayout(int videolayout) {
        this.videolayout = videolayout;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPicType() {
        return picType;
    }

    public void setPicType(int picType) {
        this.picType = picType;
    }

    public int getPicPlayTime() {
        return picPlayTime;
    }

    public void setPicPlayTime(int picPlayTime) {
        this.picPlayTime = picPlayTime;
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

    public int getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int textPosition) {
        this.textPosition = textPosition;
    }

    public int getVideoAudio() {
        return videoAudio;
    }

    public void setVideoAudio(int videoAudio) {
        this.videoAudio = videoAudio;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
}
