package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

//讲解主屏配置
public class Table_Touch_Screen extends LitePalSupport {
    //配置类型
    private int touch_type = 0;
    //图片布局
    private int touch_picType = 1;
    //轮播时间
    private int touch_picPlayTime = 0;
    //文字
    private String touch_fontContent = "";
    //文字颜色
    private String touch_fontColor = "#000000";
    //文字大小 1-大，2-中，3-小,
    private int touch_fontSize = 2;
    //文字方向 1-横向，2-纵向
    private int touch_fontLayout = 1;
    //背景颜色
    private String touch_fontBackGround = "#000000";
    //文字显示位置  0-居中 1-居上 2-居下
    private int touch_textPosition = 0;
    //图片存储位置
    private String touch_imageFile = "";
    //行走中图片
    private String touch_walkPic = "";
    //被阻挡
    private String touch_blockPic= "";
    //到点
    private String touch_arrivePic= "";
    //任务结束时返回
    private String touch_overTaskPic= "";

    public String getTouch_walkPic() {
        return touch_walkPic;
    }

    public void setTouch_walkPic(String touch_walkPic) {
        this.touch_walkPic = touch_walkPic;
    }

    public String getTouch_blockPic() {
        return touch_blockPic;
    }

    public void setTouch_blockPic(String touch_blockPic) {
        this.touch_blockPic = touch_blockPic;
    }

    public String getTouch_arrivePic() {
        return touch_arrivePic;
    }

    public void setTouch_arrivePic(String touch_arrivePic) {
        this.touch_arrivePic = touch_arrivePic;
    }

    public String getTouch_overTaskPic() {
        return touch_overTaskPic;
    }

    public void setTouch_overTaskPic(String touch_overTaskPic) {
        this.touch_overTaskPic = touch_overTaskPic;
    }

    public int getTouch_type() {
        return touch_type;
    }

    public void setTouch_type(int touch_type) {
        this.touch_type = touch_type;
    }

    public int getTouch_picType() {
        return touch_picType;
    }

    public void setTouch_picType(int touch_picType) {
        this.touch_picType = touch_picType;
    }

    public int getTouch_picPlayTime() {
        return touch_picPlayTime;
    }

    public void setTouch_picPlayTime(int touch_picPlayTime) {
        this.touch_picPlayTime = touch_picPlayTime;
    }

    public String getTouch_fontContent() {
        return touch_fontContent;
    }

    public void setTouch_fontContent(String touch_fontContent) {
        this.touch_fontContent = touch_fontContent;
    }

    public String getTouch_fontColor() {
        return touch_fontColor;
    }

    public void setTouch_fontColor(String touch_fontColor) {
        this.touch_fontColor = touch_fontColor;
    }

    public int getTouch_fontSize() {
        return touch_fontSize;
    }

    public void setTouch_fontSize(int touch_fontSize) {
        this.touch_fontSize = touch_fontSize;
    }

    public int getTouch_fontLayout() {
        return touch_fontLayout;
    }

    public void setTouch_fontLayout(int touch_fontLayout) {
        this.touch_fontLayout = touch_fontLayout;
    }

    public String getTouch_fontBackGround() {
        return touch_fontBackGround;
    }

    public void setTouch_fontBackGround(String touch_fontBackGround) {
        this.touch_fontBackGround = touch_fontBackGround;
    }

    public int getTouch_textPosition() {
        return touch_textPosition;
    }

    public void setTouch_textPosition(int touch_textPosition) {
        this.touch_textPosition = touch_textPosition;
    }

    public String getTouch_imageFile() {
        return touch_imageFile;
    }

    public void setTouch_imageFile(String touch_imageFile) {
        this.touch_imageFile = touch_imageFile;
    }
}
