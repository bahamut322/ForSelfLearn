package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * @Author Swn
 * @Data 2024/1/11
 * @describe 迎宾
 */
public class Table_Greet_Config extends LitePalSupport {
    private String greetPoint = "";
    private String firstPrompt = "开启迎宾模式";
    private String strangerPrompt = "您好呀，我是这里的多功能服务机器人%唤醒词%很高兴见到你";
    private String vipPrompt = "您好呀，很高兴见到你哟";
    private String exitPrompt = "迎宾结束，我要去忙其他的啦~";
    private Long timeStamp = 0L;
    private Table_Big_Screen bigScreenConfig;
    private Table_Touch_Screen touchScreenConfig;
    private List<Table_Face> faceFeats;

    public String getGreetPoint() {
        return greetPoint;
    }

    public void setGreetPoint(String greetPoint) {
        this.greetPoint = greetPoint;
    }

    public String getFirstPrompt() {
        return firstPrompt;
    }

    public void setFirstPrompt(String firstPrompt) {
        this.firstPrompt = firstPrompt;
    }

    public String getStrangerPrompt() {
        return strangerPrompt;
    }

    public void setStrangerPrompt(String strangerPrompt) {
        this.strangerPrompt = strangerPrompt;
    }

    public String getVipPrompt() {
        return vipPrompt;
    }

    public void setVipPrompt(String vipPrompt) {
        this.vipPrompt = vipPrompt;
    }

    public String getExitPrompt() {
        return exitPrompt;
    }

    public void setExitPrompt(String exitPrompt) {
        this.exitPrompt = exitPrompt;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Table_Big_Screen getBigScreenConfig() {
        return bigScreenConfig;
    }

    public void setBigScreenConfig(Table_Big_Screen bigScreenConfig) {
        this.bigScreenConfig = bigScreenConfig;
    }

    public Table_Touch_Screen getTouchScreenConfig() {
        return touchScreenConfig;
    }

    public void setTouchScreenConfig(Table_Touch_Screen touchScreenConfig) {
        this.touchScreenConfig = touchScreenConfig;
    }

    public List<Table_Face> getFaceFeats() {
        return faceFeats;
    }

    public void setFaceFeats(List<Table_Face> faceFeats) {
        this.faceFeats = faceFeats;
    }
}
