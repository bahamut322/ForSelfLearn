package com.sendi.deliveredrobot.entity;

import org.litepal.crud.LitePalSupport;

/**
 * @author swn
 * @describe 讲解配置(讲解途中的内容)
 */
public class ExplainConfigDB extends LitePalSupport {
    private String slogan; //标语
    private int stayTime;
    private String routeListText;
    private String pointListText;
    private String startText; //开始讲解
    private String endText; //讲解完成
    private String interruptionText; //讲解中断
    private Long timeStamp;//时间戳

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public int getStayTime() {
        return stayTime;
    }

    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }

    public String getRouteListText() {
        return routeListText;
    }

    public void setRouteListText(String routeListText) {
        this.routeListText = routeListText;
    }

    public String getPointListText() {
        return pointListText;
    }

    public void setPointListText(String pointListText) {
        this.pointListText = pointListText;
    }

    public String getStartText() {
        return startText;
    }

    public void setStartText(String startText) {
        this.startText = startText;
    }

    public String getEndText() {
        return endText;
    }

    public void setEndText(String endText) {
        this.endText = endText;
    }

    public String getInterruptionText() {
        return interruptionText;
    }

    public void setInterruptionText(String interruptionText) {
        this.interruptionText = interruptionText;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
